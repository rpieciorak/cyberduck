package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import ch.cyberduck.core.*;
import ch.cyberduck.core.Validator;

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDValidatorController extends Validator {
    private static Logger log = Logger.getLogger(CDValidatorController.class);
	
	public CDValidatorController(int kind, boolean resume) {
		super(kind, resume);
	}
	
	private NSImageView iconView; // IBOutlet
	public void setIconView(NSImageView iconView) {
		this.iconView = iconView;
	}
	
    private NSTextField alertTextField; // IBOutlet
	public void setAlertTextField(NSTextField alertTextField) {
		this.alertTextField = alertTextField;
	}
	
    private NSButton applyCheckbox; // IBOutlet
	public void setApplyCheckbox(NSButton applyCheckbox) {
		this.applyCheckbox = applyCheckbox;
	}

    private NSButton resumeButton; // IBOutlet
	public void setResumeButton(NSButton resumeButton) {
		this.resumeButton = resumeButton;
	}

	private NSWindow window; // IBOutlet
    public void setWindow(NSWindow window) {
        this.window = window;
        this.window.setDelegate(this);
    }
	
    public NSWindow window() {
        return this.window;
    }
	
	/*
	 * Use the same settings for all succeeding items to check
	 */
	private boolean applyToAll = false;
	/**
		* Include this file in the transfer queue
	 */
    private boolean include = false;
    private boolean done = true;
	
	public boolean prompt(Path path) {
		if(false == applyToAll) {
			this.done = false;
			if (false == NSApplication.loadNibNamed("Validator", this)) {
				log.fatal("Couldn't load Validator.nib");
			}
			this.resumeButton.setEnabled(!path.status.isComplete());
			String file = null;
			if (Queue.KIND_DOWNLOAD == kind)
				file = path.getLocal().getAbsolutePath();
			if (Queue.KIND_UPLOAD == kind)
				file = path.getAbsolute();
			this.alertTextField.setStringValue(NSBundle.localizedString("The file", "")+" '"+file+"' "+NSBundle.localizedString("already exists.", "")); // message
			NSImage img = NSWorkspace.sharedWorkspace().iconForFileType(path.getExtension());
			img.setScalesWhenResized(true);
			img.setSize(new NSSize(64f, 64f));
			this.iconView.setImage(img);
//@todo			while (!SHEET_CLOSED) {
			NSApplication.sharedApplication().beginSheet(this.window(), //sheet
														 CDQueueController.instance().window(),
														 this, //modalDelegate
														 new NSSelector("validateSheetDidEnd",
																		new Class[]{NSWindow.class, int.class, Object.class}), // did end selector
														 path); //contextInfo
			this.window().makeKeyAndOrderFront(null);
		}
		// Waiting for user to make choice
		while (!done) {
			try {
				log.debug("Sleeping...");
				Thread.sleep(1000); //milliseconds
			}
			catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
		path.status.setResume(resume);
		log.debug("return:" + include);
		return include;
	}

    public void resumeActionFired(NSButton sender) {
		log.debug("resumeActionFired");
		this.applyToAll = (applyCheckbox.state() == NSCell.OnState);
		this.resume = true;
		this.include = true;
        NSApplication.sharedApplication().endSheet(this.window, sender.tag());
	}
	
	public void closeSheet(NSButton sender) {
		this.applyToAll = (applyCheckbox.state() == NSCell.OnState);
        NSApplication.sharedApplication().endSheet(this.window, sender.tag());
	}

	/*
	public void overwriteActionFired(NSButton sender) {
		log.debug("overwriteActionFired");
		this.applyToAll = (applyCheckbox.state() == NSCell.OnState);
		this.resume = false;
		this.include = true;
        NSApplication.sharedApplication().endSheet(this.window, sender.tag());
	}
	
    public void skipActionFired(NSButton sender) {
		log.debug("skipActionFired");
		this.applyToAll = (applyCheckbox.state() == NSCell.OnState);
//		this.resume = false;
		this.include = false;
        NSApplication.sharedApplication().endSheet(this.window, sender.tag());
	}
	
	public void cancelActionFired(NSButton sender) {
		log.debug("cancelActionFired");
		this.canceled = true;
//		this.applyToAll = true;
//		this.resume = true;
		this.include = false;
        NSApplication.sharedApplication().endSheet(this.window, sender.tag());
	}
	 */
	
    public void validateSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        this.window().close();
        Path item = (Path) contextInfo;
        switch (returncode) {
			case 0: //Cancel
				log.debug("Canceled");
                item.status.setResume(false);
				this.include = false;
				this.canceled = true;
                break;
            case 1://NSAlertPanel.DefaultReturn //Overwrite
				log.debug("Overwrite");
                item.status.setResume(false);
				this.include = true;
                break;
            case -1://NSAlertPanel.AlternateReturn: //Resume
				log.debug("Resume");
                item.status.setResume(true);
                this.include = true;
                break;
            case 2://NSAlertPanel.OtherReturn: //Skip
				log.debug("Skipped");
                item.status.setResume(false);
                this.include = false;
                break;
        }
        this.done = true;
    }
}