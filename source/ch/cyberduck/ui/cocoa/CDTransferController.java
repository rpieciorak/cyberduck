package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import org.apache.log4j.Logger;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.Message;
import java.util.Observable;
import java.util.Observer;

/**
* @version $Id$
 */
public class CDTransferController implements Observer {
    private static Logger log = Logger.getLogger(CDTransferController.class);

    private Path file;
//@todo open new session for transfer
    //private Session session;

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSWindow window;
    public void setWindow(NSWindow window) {
	this.window = window;
    }
    
    private NSTextField urlField;
    public void setUrlField(NSTextField urlField) {
	this.urlField = urlField;
    }
    
    private NSTextField fileField;
    public void setFileField(NSTextField fileField) {
	this.fileField = fileField;
    }

    private NSTextField progressField;
    public void setProgressField(NSTextField progressField) {
	this.progressField = progressField;
    }

    private NSProgressIndicator progressBar;
    public void setProgressField(NSProgressIndicator progressBar) {
	this.progressBar = progressBar;
	this.progressBar.setIndeterminate(true);
	this.progressBar.setUsesThreadedAnimation(true);
    }

    private NSButton stopButton;
    public void setStopButton(NSButton stopButton) {
	this.stopButton = stopButton;
    }

    private NSButton resumeButton;
    public void setResumeButton(NSButton resumeButton) {
	this.resumeButton = resumeButton;
    }

    public NSImageView iconView;
    public void setIconView(NSImageView iconView) {
	this.iconView = iconView;
    }

    public NSImageView fileIconView;
    public void setFileIconView(NSImageView fileIconView) {
	this.fileIconView = fileIconView;
    }
    
    public CDTransferController(Path file) {
	super();
	this.file = file;
	//register for events
	file.status.addObserver(this);
        if (false == NSApplication.loadNibNamed("Transfer", this)) {
            log.error("Couldn't load Transfer.nib");
            return;
        }
	this.init();
    }

    private void init() {
	log.debug("init");
	this.fileIconView.setImage(NSWorkspace.sharedWorkspace().iconForFileType(file.getExtension()));
	this.urlField.setStringValue(file.getAbsolute()); //@todo url
	this.fileField.setStringValue(file.getLocal().toString());
	this.window().setTitle(file.getName());
	this.progressBar.setMinValue(0);
	this.progressBar.setMaxValue(file.status.getSize());
	this.progressField.setStringValue("");
    }

    public void update(Observable o, Object arg) {
	log.debug("update:"+o+","+arg);
	if(o instanceof Status) {
	    if(arg instanceof Message) {
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.DATA)) {
		    this.progressBar.setIndeterminate(false);
		    this.progressBar.setDoubleValue((double)file.status.getCurrent());
		    this.progressField.setStringValue(msg.getDescription());
		    return;
		}
		if(msg.getTitle().equals(Message.PROGRESS)) {
		    //@todo
		    return;
		}
		if(msg.getTitle().equals(Message.ERROR)) {
		    NSAlertPanel.beginAlertSheet(
				   "Error", //title
				   "OK",// defaultbutton
				   null,//alternative button
				   null,//other button
				   this.window(), //docWindow
				   null, //modalDelegate
				   null, //didEndSelector
				   null, // dismiss selector
				   null, // context
				   msg.getDescription() // message
				   );
		    this.stopButton.setEnabled(false);
		    this.resumeButton.setEnabled(true);
		    return;
		}
		if(msg.getTitle().equals(Message.START)) {
		    this.resumeButton.setTitle("Resume");
		    this.progressBar.startAnimation(null);
		    return;
		}
		if(msg.getTitle().equals(Message.STOP)) {
		    this.progressBar.stopAnimation(this);
		    this.stopButton.setEnabled(false);
		    this.resumeButton.setEnabled(true);
		    return;
		}
		if(msg.getTitle().equals(Message.COMPLETE)) {
		    this.progressBar.setDoubleValue((double)file.status.getCurrent());
		    this.resumeButton.setTitle("Reload");
		    this.stopButton.setEnabled(false);
		    this.resumeButton.setEnabled(true);
		    return;
		}
	    }
	}
    }

    public void download() {
	iconView.setImage(NSImage.imageNamed("download.tiff"));
	this.window().makeKeyAndOrderFront(null);
	this.file.download();
    }

    public void upload() {
	iconView.setImage(NSImage.imageNamed("upload.tiff"));
	this.window().makeKeyAndOrderFront(null);
//@todo	this.file.upload();
    }

    public NSWindow window() {
	return this.window;
    }

    public void resumeButtonClicked(NSButton sender) {
	if(sender.title().equals("Resume"))
	    this.file.status.setResume(true);
	this.stopButton.setEnabled(true);
	this.resumeButton.setEnabled(false);
	this.file.download();
    }

    public void stopButtonClicked(NSButton sender) {
	this.stopButton.setEnabled(false);
	this.resumeButton.setEnabled(true);
	this.file.status.setCanceled(true);
    }

    public boolean windowShouldClose(Object sender) {
	if(!this.file.status.isStopped()) {
	    NSAlertPanel.beginCriticalAlertSheet(
					       "Cancel transfer?", //title
					       "Stop",// defaultbutton
					       "Cancel",//alternative button
					       null,//other button
					       this.window(),
					       this, //delegate
					       new NSSelector
					       (
	     "confirmSheetDidEnd",
	     new Class[]
	     {
		 NSWindow.class, int.class, NSWindow.class
	     }
	     ),// end selector
					       null, // dismiss selector
					       this, // context
					       "Closing this window will stop the file transfer" // message
					       );
	    return false;	    
	}
	return true;
    }

    public void confirmSheetDidEnd(NSWindow sheet, int returncode, NSWindow main)  {
	sheet.orderOut(null);
	if(returncode == NSAlertPanel.DefaultReturn) {
	    this.stopButtonClicked(null);
	    this.window().close();
	}
	if(returncode == NSAlertPanel.AlternateReturn) {
	    //
	}
    }
}

