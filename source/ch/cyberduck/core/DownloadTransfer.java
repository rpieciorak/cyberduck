package ch.cyberduck.core;

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
import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import ch.cyberduck.core.io.BandwidthThrottle;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @version $Id$
 */
public class DownloadTransfer extends Transfer {

    public DownloadTransfer(Path root) {
        super(root);
    }

    public DownloadTransfer(List roots) {
        super(roots);
    }

    protected void setRoots(List downloads) {
        final List normalized = new Collection();
        for(Iterator iter = downloads.iterator(); iter.hasNext();) {
            final Path download = (Path) iter.next();
            boolean duplicate = false;
            for(Iterator normalizedIter = normalized.iterator(); normalizedIter.hasNext();) {
                Path n = (Path) normalizedIter.next();
                if(download.isChild(n)) {
                    // The selected file is a child of a directory
                    // already included for deletion
                    duplicate = true;
                    break;
                }
                if(download.getLocal().getName().equals(n.getLocal().getName())) {
                    // The selected file has the same name; if downloaded as a root element
                    // it would overwrite the earlier
                    final String parent = download.getLocal().getParent().getAbsolute();
                    final String filename = download.getName();
                    String proposal;
                    int no = 0;
                    int index = filename.lastIndexOf(".");
                    do {
                        no++;
                        if(index != -1 && index != 0) {
                            proposal = filename.substring(0, index)
                                    + "-" + no + filename.substring(index);
                        } else {
                            proposal = filename + "-" + no;
                        }
                        download.getLocal().setPath(parent, proposal);
                    }
                    while(download.getLocal().exists());
                    log.info("Changed local name to:" + download.getName());
                }
            }
            // Prunes the list of selected files. Files which are a child of an already included directory
            // are removed from the returned list.
            if(!duplicate) {
                normalized.add(download);
            }
        }
        super.setRoots(normalized);
    }

    public DownloadTransfer(NSDictionary dict, Session s) {
        super(dict, s);
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = super.getAsDictionary();
        dict.setObjectForKey(String.valueOf(TransferFactory.KIND_DOWNLOAD), "Kind");
        return dict;
    }

    protected void init() {
        log.debug("init");
        this.bandwidth = new BandwidthThrottle(
                Preferences.instance().getFloat("queue.download.bandwidth.bytes"));
    }

    /**
     *
     */
    private abstract class DownloadTransferFilter extends TransferFilter {
        public void prepare(Path p) {
            if(DownloadTransfer.this.exists(p)) {
                if(p.attributes.getSize() == -1) {
                    p.readSize();
                }
                if(p.attributes.getModificationDate() == -1) {
                    p.readTimestamp();
                }
                if(p.attributes.getPermission() == null) {
                    p.readPermission();
                }
            }
            // Read file size
            if(p.attributes.isFile()) {
                if(p.attributes.isSymbolicLink()) {
                    if(null != p.getSymbolicLinkPath()) {
                        Path symlink = PathFactory.createPath(p.getSession(), p.getSymbolicLinkPath());
                        if(symlink.attributes.getSize() == -1) {
                            symlink.readSize();
                        }
                        size += symlink.attributes.getSize();
                    }
                } else {
                    size += p.attributes.getSize();
                }
                if(p.status.isResume()) {
                    transferred += p.getLocal().attributes.getSize();
                }
            }
            if(!p.getLocal().getParent().exists()) {
                // Create download folder if missing
                p.getLocal().getParent().mkdir(true);
            }
        }
    }

    /**
     * A compiled representation of a regular expression.
     */
    private Pattern DOWNLOAD_SKIP_PATTERN = null;

    {
        try {
            DOWNLOAD_SKIP_PATTERN = Pattern.compile(
                    Preferences.instance().getProperty("queue.download.skip.regex"));
        }
        catch(PatternSyntaxException e) {
            log.warn(e.getMessage());
        }
    }

    private final PathFilter childFilter = new PathFilter() {
        public boolean accept(AbstractPath child) {
            if(Preferences.instance().getBoolean("queue.download.skip.enable")
                    && DOWNLOAD_SKIP_PATTERN.matcher(child.getName()).matches()) {
                return false;
            }
            ((Path) child).getLocal().setPath(
                    ((Path) child.getParent()).getLocal().getAbsolute(),
                    ((Path) child).getLocal().getName()
            );
            return true;
        }
    };

    public AttributedList childs(final Path parent) {
        if(!this.exists(parent)) {
            // Cannot fetch file listing of non existant file
            return new AttributedList(Collections.EMPTY_LIST);
        }
        return parent.childs(new NullComparator(), childFilter);
    }

    public boolean isCached(Path file) {
        return file.isCached();
    }

    private final DownloadTransferFilter ACTION_OVERWRITE = new DownloadTransferFilter() {
        public boolean accept(final AbstractPath p) {
            if(p.attributes.isDirectory()) {
                return !DownloadTransfer.this.exists(((Path) p).getLocal());
            }
            return true;
        }

        public void prepare(final Path p) {
            if(DownloadTransfer.this.exists(p.getLocal())) {
                if(0 > NSWorkspace.sharedWorkspace().performFileOperation(NSWorkspace.RecycleOperation,
                        p.getLocal().getParent().getAbsolute(), "", new NSArray(p.getLocal().getName()))) {
                    log.warn("Failed to move " + p.getLocal().getAbsolute() + " to Trash");
                }
            }
            if(p.attributes.isFile()) {
                p.status.setResume(false);
            }
            super.prepare(p);
        }
    };

    private final DownloadTransferFilter ACTION_RESUME = new DownloadTransferFilter() {
        public boolean accept(final AbstractPath p) {
            if(((Path) p).status.isComplete()) {
                return false;
            }
            if(p.attributes.isDirectory()) {
                return !DownloadTransfer.this.exists(((Path) p).getLocal());
            }
            return true;
        }

        public void prepare(final Path p) {
            if(p.attributes.isFile()) {
                p.status.setResume(DownloadTransfer.this.exists(p.getLocal()) && p.getLocal().attributes.getSize() > 0);
            }
            super.prepare(p);
        }
    };

    private final DownloadTransferFilter ACTION_RENAME = new DownloadTransferFilter() {
        public boolean accept(final AbstractPath p) {
            return true;
        }

        public void prepare(final Path p) {
            if(p.attributes.isFile()) {
                p.status.setResume(false);
            }
            if(DownloadTransfer.this.exists(p.getLocal())) {
                final String parent = p.getLocal().getParent().getAbsolute();
                final String filename = p.getName();
                String proposal;
                int no = 0;
                int index = filename.lastIndexOf(".");
                while(p.getLocal().exists()) {
                    no++;
                    if(index != -1 && index != 0) {
                        proposal = filename.substring(0, index)
                                + "-" + no + filename.substring(index);
                    } else {
                        proposal = filename + "-" + no;
                    }
                    p.getLocal().setPath(parent, proposal);
                }
                log.info("Changed local name to:" + p.getName());
            }
            super.prepare(p);
        }
    };

    private final DownloadTransferFilter ACTION_SKIP = new DownloadTransferFilter() {
        public boolean accept(final AbstractPath p) {
            return !DownloadTransfer.this.exists(((Path) p).getLocal());
        }
    };

    public TransferFilter filter(final TransferAction action) {
        log.debug("filter:" + action);
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            return ACTION_OVERWRITE;
        }
        if(action.equals(TransferAction.ACTION_RESUME)) {
            return ACTION_RESUME;
        }
        if(action.equals(TransferAction.ACTION_RENAME)) {
            return ACTION_RENAME;
        }
        if(action.equals(TransferAction.ACTION_SKIP)) {
            return ACTION_SKIP;
        }
        if(action.equals(TransferAction.ACTION_CALLBACK)) {
            for(Iterator iter = this.getRoots().iterator(); iter.hasNext();) {
                Path root = (Path) iter.next();
                if(DownloadTransfer.this.exists(root.getLocal())) {
                    if(root.getLocal().attributes.isDirectory()) {
                        if(0 == root.getLocal().childs().size()) {
                            // Do not prompt for existing empty directories
                            continue;
                        }
                    }
                    if(root.getLocal().attributes.isFile()) {
                        if(root.getLocal().attributes.getSize() == 0) {
                            // Do not prompt for zero sized files
                            continue;
                        }
                    }
                    // Prompt user to choose a filter
                    TransferAction result = prompt.prompt(this);
                    return this.filter(result); //break out of loop
                }
            }
            // No files exist yet therefore it is most straightforward to use the overwrite action
            return this.filter(TransferAction.ACTION_OVERWRITE);
        }
        return super.filter(action);
    }

    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
        log.debug("action:" + resumeRequested + "," + reloadRequested);
        if(resumeRequested) {
            // Force resume
            return TransferAction.ACTION_RESUME;
        }
        if(reloadRequested) {
            return TransferAction.forName(
                    Preferences.instance().getProperty("queue.download.reload.fileExists")
            );
        }
        // Use default
        return TransferAction.forName(
                Preferences.instance().getProperty("queue.download.fileExists")
        );
    }

    protected void _transferImpl(final Path p) {
        p.download(bandwidth, new AbstractStreamListener() {
            public void bytesReceived(int bytes) {
                transferred += bytes;
            }
        });
    }
}