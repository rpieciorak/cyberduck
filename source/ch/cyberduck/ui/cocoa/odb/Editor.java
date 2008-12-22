package ch.cyberduck.ui.cocoa.odb;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.BrowserBackgroundAction;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDController;

import org.apache.log4j.Logger;

import java.io.File;
import java.text.MessageFormat;
import java.util.Enumeration;

/**
 * @version $Id$
 */
public abstract class Editor extends CDController {
    private static Logger log = Logger.getLogger(Editor.class);

    private CDBrowserController controller;

    /**
     * The edited path
     */
    protected Path edited;

    /**
     * The editor application
     */
    protected String bundleIdentifier;

    /**
     * @param controller
     * @param bundleIdentifier
     */
    public Editor(CDBrowserController controller, String bundleIdentifier, Path path) {
        this.controller = controller;
        this.bundleIdentifier = bundleIdentifier;
        this.edited = path;
        final Local folder = new Local(new File(new Local(Preferences.instance().getProperty("editor.tmp.directory")).getAbsolute(),
                edited.getParent().getAbsolute()));
        this.edited.setLocal(new Local(folder, edited.getName()));
    }

    public void open() {
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
                TransferOptions options = new TransferOptions();
                options.closeSession = false;
                Transfer download = new DownloadTransfer(edited) {
                    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
                        return TransferAction.ACTION_RENAME;
                    }

                    protected boolean shouldOpenWhenComplete() {
                        return false;
                    }
                };
                download.start(new TransferPrompt() {
                    public TransferAction prompt() {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                }, options);
            }

            public String getActivity() {
                return MessageFormat.format(NSBundle.localizedString("Downloading {0}", "Status", ""),
                        edited.getName());
            }

            public void cleanup() {
                if(edited.getStatus().isComplete()) {
                    final Permission permissions = edited.getLocal().attributes.getPermission();
                    if(null != permissions) {
                        permissions.getOwnerPermissions()[Permission.READ] = true;
                        permissions.getOwnerPermissions()[Permission.WRITE] = true;
                        edited.getLocal().writePermissions(permissions, false);
                    }
                    // Important, should always be run on the main thread; otherwise applescript crashes
                    Editor.this.edit();
                }
            }
        });
    }

    /**
     * @return True if the editor is open
     */
    public boolean isOpen() {
        final Enumeration apps = NSWorkspace.sharedWorkspace().launchedApplications().objectEnumerator();
        while(apps.hasMoreElements()) {
            NSDictionary app = (NSDictionary) apps.nextElement();
            final Object identifier = app.objectForKey("NSApplicationBundleIdentifier");
            if(identifier.equals(bundleIdentifier)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     */
    protected abstract void edit();

    /**
     *
     */
    protected void delete() {
        log.debug("delete");
        edited.getLocal().delete();
        this.invalidate();
    }

    /**
     * The file has been closed in the editor while the upload was in progress
     */
    private boolean deferredDelete;


    protected void setDeferredDelete(boolean deferredDelete) {
        this.deferredDelete = deferredDelete;
    }

    public boolean isDeferredDelete() {
        return deferredDelete;
    }

    /**
     * Upload the edited file to the server
     */
    protected void save() {
        log.debug("save");
        edited.getStatus().reset();
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
                TransferOptions options = new TransferOptions();
                options.closeSession = false;
                Transfer upload = new UploadTransfer(edited) {
                    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                };
                upload.start(new TransferPrompt() {
                    public TransferAction prompt() {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                }, options);
            }

            public String getActivity() {
                return MessageFormat.format(NSBundle.localizedString("Uploading {0}", "Status", ""),
                        edited.getName());
            }

            public void cleanup() {
                if(edited.getStatus().isComplete()) {
                    controller.reloadData(true);
                    if(Editor.this.isDeferredDelete()) {
                        Editor.this.delete();
                    }
                    Editor.this.setDeferredDelete(false);
                }
            }
        });
    }
}