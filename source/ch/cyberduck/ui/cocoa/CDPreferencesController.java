package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import org.apache.log4j.Logger;

import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionPool;
import ch.cyberduck.ui.cocoa.odb.Editor;

/**
 * @version $Id$
 */
public class CDPreferencesController extends CDController {
	private static Logger log = Logger.getLogger(CDPreferencesController.class);

	private static CDPreferencesController instance;

	private static NSMutableArray instances = new NSMutableArray();

	public static CDPreferencesController instance() {
		log.debug("instance");
		if(null == instance) {
			instance = new CDPreferencesController();
			if(false == NSApplication.loadNibNamed("Preferences", instance)) {
				log.fatal("Couldn't load Preferences.nib");
			}
		}
		return instance;
	}

	private CDPreferencesController() {
		instances.addObject(this);
	}

	public void awakeFromNib() {
		log.debug("awakeFromNib");
		this.window().center();
		this.transfermodeComboboxClicked(this.transfermodeCombobox);
	}

	public void windowWillClose(NSNotification notification) {
		NSNotificationCenter.defaultCenter().removeObserver(this);
		instances.removeObject(this);
		instance = null;
	}

	private static final String CONNECTMODE_ACTIVE = NSBundle.localizedString("Active", "");
	private static final String CONNECTMODE_PASSIVE = NSBundle.localizedString("Passive", "");

	private static final String TRANSFERMODE_AUTO = NSBundle.localizedString("Auto", "");
	private static final String TRANSFERMODE_BINARY = NSBundle.localizedString("Binary", "");
	private static final String TRANSFERMODE_ASCII = NSBundle.localizedString("ASCII", "");

	private static final String UNIX_LINE_ENDINGS = NSBundle.localizedString("Unix Line Endings (LF)", "");
	private static final String MAC_LINE_ENDINGS = NSBundle.localizedString("Mac Line Endings (CR)", "");
	private static final String WINDOWS_LINE_ENDINGS = NSBundle.localizedString("Windows Line Endings (CRLF)", "");

	private static final String PROTOCOL_FTP = "FTP";
	private static final String PROTOCOL_SFTP = "SFTP";

	private static final String ASK_ME_WHAT_TO_DO = NSBundle.localizedString("Ask me what to do", "");
	private static final String OVERWRITE_EXISTING_FILE = NSBundle.localizedString("Overwrite existing file", "");
	private static final String TRY_TO_RESUME_TRANSFER = NSBundle.localizedString("Try to resume transfer", "");
	private static final String USE_A_SIMILAR_NAME = NSBundle.localizedString("Use similar name", "");

	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------

	private NSPopUpButton editorCombobox;

	public void setEditorCombobox(NSPopUpButton editorCombobox) {
		this.editorCombobox = editorCombobox;
		this.editorCombobox.setAutoenablesItems(false);
		this.editorCombobox.setTarget(this);
		this.editorCombobox.setAction(new NSSelector("editorComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.editorCombobox.removeAllItems();
		java.util.Map editors = Editor.SUPPORTED_EDITORS;
		NSSelector absolutePathForAppBundleWithIdentifierSelector =
			new NSSelector("absolutePathForAppBundleWithIdentifier", new Class[]{String.class});
		java.util.Iterator editorNames = editors.keySet().iterator();
		java.util.Iterator editorIdentifiers = editors.values().iterator();
		while(editorNames.hasNext()) {
			String editor = (String)editorNames.next();
			this.editorCombobox.addItem(editor);
			if(absolutePathForAppBundleWithIdentifierSelector.implementedByClass(NSWorkspace.class)) {
				this.editorCombobox.itemWithTitle(editor).setEnabled(NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier((String)editorIdentifiers.next()) != null);
			}
		}
		this.editorCombobox.setTitle(Preferences.instance().getProperty("editor.name"));
	}
	
	public void editorComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("editor.name", sender.titleOfSelectedItem());
		Preferences.instance().setProperty("editor.bundleIdentifier", (String)Editor.SUPPORTED_EDITORS.get(sender.titleOfSelectedItem()));
	}

	private NSPopUpButton encodingCombobox;

	public void setEncodingCombobox(NSPopUpButton encodingCombobox) {
		this.encodingCombobox = encodingCombobox;
		this.encodingCombobox.setTarget(this);
		this.encodingCombobox.setAction(new NSSelector("encodingComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.encodingCombobox.removeAllItems();
		java.util.SortedMap charsets = java.nio.charset.Charset.availableCharsets();
		String[] items = new String[charsets.size()];
		java.util.Iterator iterator = charsets.values().iterator();
		int i = 0;
		while(iterator.hasNext()) {
			items[i] = ((java.nio.charset.Charset)iterator.next()).name();
			i++;
		}
		this.encodingCombobox.addItemsWithTitles(new NSArray(items));
		this.encodingCombobox.setTitle(Preferences.instance().getProperty("browser.charset.encoding"));
	}

	public void encodingComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("browser.charset.encoding", sender.titleOfSelectedItem());
	}

	private NSButton listCheckbox; //IBOutlet

	public void setListCheckbox(NSButton listCheckbox) {
		this.listCheckbox = listCheckbox;
		this.listCheckbox.setTarget(this);
		this.listCheckbox.setAction(new NSSelector("listCheckboxClicked", new Class[]{NSButton.class}));
		this.listCheckbox.setState(Preferences.instance().getProperty("ftp.sendExtendedListCommand").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void listCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("ftp.sendExtendedListCommand", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("ftp.sendExtendedListCommand", false);
				break;
		}
	}

	private NSButton systCheckbox; //IBOutlet

	public void setSystCheckbox(NSButton systCheckbox) {
		this.systCheckbox = systCheckbox;
		this.systCheckbox.setTarget(this);
		this.systCheckbox.setAction(new NSSelector("systCheckboxClicked", new Class[]{NSButton.class}));
		this.systCheckbox.setState(Preferences.instance().getProperty("ftp.sendSystemCommand").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void systCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("ftp.sendSystemCommand", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("ftp.sendSystemCommand", false);
				break;
		}
	}

	private NSButton chmodUploadCheckbox; //IBOutlet

	public void setChmodUploadCheckbox(NSButton chmodUploadCheckbox) {
		this.chmodUploadCheckbox = chmodUploadCheckbox;
		this.chmodUploadCheckbox.setTarget(this);
		this.chmodUploadCheckbox.setAction(new NSSelector("chmodUploadCheckboxClicked", new Class[]{NSButton.class}));
		this.chmodUploadCheckbox.setState(Preferences.instance().getProperty("queue.upload.changePermissions").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void chmodUploadCheckboxClicked(NSButton sender) {
		boolean enabled = false;
		switch(sender.state()) {
			case NSCell.OnState:
				enabled = true;
				break;
		}
		Preferences.instance().setProperty("queue.upload.changePermissions", enabled);
		this.chmodUploadDefaultCheckbox.setEnabled(enabled);
		this.uownerr.setEnabled(enabled);
		this.uownerw.setEnabled(enabled);
		this.uownerx.setEnabled(enabled);
		this.ugroupr.setEnabled(enabled);
		this.ugroupw.setEnabled(enabled);
		this.ugroupx.setEnabled(enabled);
		this.uotherr.setEnabled(enabled);
		this.uotherw.setEnabled(enabled);
		this.uotherx.setEnabled(enabled);
	}

	private NSButton chmodUploadDefaultCheckbox; //IBOutlet

	public void setChmodUploadDefaultCheckbox(NSButton chmodUploadDefaultCheckbox) {
		this.chmodUploadDefaultCheckbox = chmodUploadDefaultCheckbox;
		this.chmodUploadDefaultCheckbox.setTarget(this);
		this.chmodUploadDefaultCheckbox.setAction(new NSSelector("chmodUploadDefaultCheckboxClicked", new Class[]{NSButton.class}));
		this.chmodUploadDefaultCheckbox.setState(Preferences.instance().getProperty("queue.upload.permissions.useDefault").equals("true") ? NSCell.OnState : NSCell.OffState);
		this.chmodUploadDefaultCheckbox.setEnabled(Preferences.instance().getProperty("queue.upload.changePermissions").equals("true"));
		boolean enabled = Preferences.instance().getProperty("queue.upload.changePermissions").equals("true") 
			&& Preferences.instance().getProperty("queue.upload.permissions.useDefault").equals("true");
		this.uownerr.setEnabled(enabled);
		this.uownerw.setEnabled(enabled);
		this.uownerx.setEnabled(enabled);
		this.ugroupr.setEnabled(enabled);
		this.ugroupw.setEnabled(enabled);
		this.ugroupx.setEnabled(enabled);
		this.uotherr.setEnabled(enabled);
		this.uotherw.setEnabled(enabled);
		this.uotherx.setEnabled(enabled);
	}

	public void chmodUploadDefaultCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("queue.upload.permissions.useDefault", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("queue.upload.permissions.useDefault", false);
				break;
		}
		this.uownerr.setEnabled(sender.state() == NSCell.OnState);
		this.uownerw.setEnabled(sender.state() == NSCell.OnState);
		this.uownerx.setEnabled(sender.state() == NSCell.OnState);
		this.ugroupr.setEnabled(sender.state() == NSCell.OnState);
		this.ugroupw.setEnabled(sender.state() == NSCell.OnState);
		this.ugroupx.setEnabled(sender.state() == NSCell.OnState);
		this.uotherr.setEnabled(sender.state() == NSCell.OnState);
		this.uotherw.setEnabled(sender.state() == NSCell.OnState);
		this.uotherx.setEnabled(sender.state() == NSCell.OnState);
	}

	private NSButton chmodDownloadCheckbox; //IBOutlet

	public void setChmodDownloadCheckbox(NSButton chmodDownloadCheckbox) {
		this.chmodDownloadCheckbox = chmodDownloadCheckbox;
		this.chmodDownloadCheckbox.setTarget(this);
		this.chmodDownloadCheckbox.setAction(new NSSelector("chmodDownloadCheckboxClicked", new Class[]{NSButton.class}));
		this.chmodDownloadCheckbox.setState(Preferences.instance().getProperty("queue.download.changePermissions").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void chmodDownloadCheckboxClicked(NSButton sender) {
		boolean enabled = false;
		switch(sender.state()) {
			case NSCell.OnState:
				enabled = true;
				break;
		}
		Preferences.instance().setProperty("queue.download.changePermissions", enabled);
		this.chmodDownloadDefaultCheckbox.setEnabled(enabled);
		this.downerr.setEnabled(enabled);
		this.downerw.setEnabled(enabled);
		this.downerx.setEnabled(enabled);
		this.dgroupr.setEnabled(enabled);
		this.dgroupw.setEnabled(enabled);
		this.dgroupx.setEnabled(enabled);
		this.dotherr.setEnabled(enabled);
		this.dotherw.setEnabled(enabled);
		this.dotherx.setEnabled(enabled);
	}

	private NSButton chmodDownloadDefaultCheckbox; //IBOutlet

	public void setChmodDownloadDefaultCheckbox(NSButton chmodDownloadDefaultCheckbox) {
		this.chmodDownloadDefaultCheckbox = chmodDownloadDefaultCheckbox;
		this.chmodDownloadDefaultCheckbox.setTarget(this);
		this.chmodDownloadDefaultCheckbox.setAction(new NSSelector("chmodDownloadDefaultCheckboxClicked", new Class[]{NSButton.class}));
		this.chmodDownloadDefaultCheckbox.setState(Preferences.instance().getProperty("queue.download.permissions.useDefault").equals("true") ? NSCell.OnState : NSCell.OffState);
		this.chmodDownloadDefaultCheckbox.setEnabled(Preferences.instance().getProperty("queue.download.changePermissions").equals("true"));
		boolean enabled = Preferences.instance().getProperty("queue.download.changePermissions").equals("true") 
			&& Preferences.instance().getProperty("queue.download.permissions.useDefault").equals("true");
		this.downerr.setEnabled(enabled);
		this.downerw.setEnabled(enabled);
		this.downerx.setEnabled(enabled);
		this.dgroupr.setEnabled(enabled);
		this.dgroupw.setEnabled(enabled);
		this.dgroupx.setEnabled(enabled);
		this.dotherr.setEnabled(enabled);
		this.dotherw.setEnabled(enabled);
		this.dotherx.setEnabled(enabled);
	}

	public void chmodDownloadDefaultCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("queue.download.permissions.useDefault", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("queue.download.permissions.useDefault", false);
				break;
		}
		this.downerr.setEnabled(sender.state() == NSCell.OnState);
		this.downerw.setEnabled(sender.state() == NSCell.OnState);
		this.downerx.setEnabled(sender.state() == NSCell.OnState);
		this.dgroupr.setEnabled(sender.state() == NSCell.OnState);
		this.dgroupw.setEnabled(sender.state() == NSCell.OnState);
		this.dgroupx.setEnabled(sender.state() == NSCell.OnState);
		this.dotherr.setEnabled(sender.state() == NSCell.OnState);
		this.dotherw.setEnabled(sender.state() == NSCell.OnState);
		this.dotherx.setEnabled(sender.state() == NSCell.OnState);
	}

	public NSButton downerr; //IBOutlet
	public NSButton downerw; //IBOutlet
	public NSButton downerx; //IBOutlet
	public NSButton dgroupr; //IBOutlet
	public NSButton dgroupw; //IBOutlet
	public NSButton dgroupx; //IBOutlet
	public NSButton dotherr; //IBOutlet
	public NSButton dotherw; //IBOutlet
	public NSButton dotherx; //IBOutlet

	public void defaultPermissionsDownloadChanged(Object sender) {
		boolean[][] p = new boolean[3][3];

		p[Permission.OWNER][Permission.READ] = (downerr.state() == NSCell.OnState);
		p[Permission.OWNER][Permission.WRITE] = (downerw.state() == NSCell.OnState);
		p[Permission.OWNER][Permission.EXECUTE] = (downerx.state() == NSCell.OnState);

		p[Permission.GROUP][Permission.READ] = (dgroupr.state() == NSCell.OnState);
		p[Permission.GROUP][Permission.WRITE] = (dgroupw.state() == NSCell.OnState);
		p[Permission.GROUP][Permission.EXECUTE] = (dgroupx.state() == NSCell.OnState);

		p[Permission.OTHER][Permission.READ] = (dotherr.state() == NSCell.OnState);
		p[Permission.OTHER][Permission.WRITE] = (dotherw.state() == NSCell.OnState);
		p[Permission.OTHER][Permission.EXECUTE] = (dotherx.state() == NSCell.OnState);

		Permission permission = new Permission(p);
		Preferences.instance().setProperty("queue.download.permissions.default", permission.getMask());
	}

	public NSButton uownerr; //IBOutlet
	public NSButton uownerw; //IBOutlet
	public NSButton uownerx; //IBOutlet
	public NSButton ugroupr; //IBOutlet
	public NSButton ugroupw; //IBOutlet
	public NSButton ugroupx; //IBOutlet
	public NSButton uotherr; //IBOutlet
	public NSButton uotherw; //IBOutlet
	public NSButton uotherx; //IBOutlet

	public void defaultPermissionsUploadChanged(Object sender) {
		boolean[][] p = new boolean[3][3];

		p[Permission.OWNER][Permission.READ] = (uownerr.state() == NSCell.OnState);
		p[Permission.OWNER][Permission.WRITE] = (uownerw.state() == NSCell.OnState);
		p[Permission.OWNER][Permission.EXECUTE] = (uownerx.state() == NSCell.OnState);

		p[Permission.GROUP][Permission.READ] = (ugroupr.state() == NSCell.OnState);
		p[Permission.GROUP][Permission.WRITE] = (ugroupw.state() == NSCell.OnState);
		p[Permission.GROUP][Permission.EXECUTE] = (ugroupx.state() == NSCell.OnState);

		p[Permission.OTHER][Permission.READ] = (uotherr.state() == NSCell.OnState);
		p[Permission.OTHER][Permission.WRITE] = (uotherw.state() == NSCell.OnState);
		p[Permission.OTHER][Permission.EXECUTE] = (uotherx.state() == NSCell.OnState);

		Permission permission = new Permission(p);
		Preferences.instance().setProperty("queue.upload.permissions.default", permission.getMask());
	}

	private NSButton preserveModificationDownloadCheckbox; //IBOutlet
	
	public void setPreserveModificationDownloadCheckbox(NSButton preserveModificationDownloadCheckbox) {
		this.preserveModificationDownloadCheckbox = preserveModificationDownloadCheckbox;
		this.preserveModificationDownloadCheckbox.setTarget(this);
		this.preserveModificationDownloadCheckbox.setAction(new NSSelector("preserveModificationDownloadCheckboxClicked", new Class[]{NSButton.class}));
		this.preserveModificationDownloadCheckbox.setState(Preferences.instance().getProperty("queue.download.preserveDate").equals("true") ? NSCell.OnState : NSCell.OffState);
	}
	
	public void preserveModificationDownloadCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("queue.download.preserveDate", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("queue.download.preserveDate", false);
				break;
		}
	}

	private NSButton preserveModificationUploadCheckbox; //IBOutlet
	
	public void setPreserveModificationUploadCheckbox(NSButton preserveModificationUploadCheckbox) {
		this.preserveModificationUploadCheckbox = preserveModificationUploadCheckbox;
		this.preserveModificationUploadCheckbox.setTarget(this);
		this.preserveModificationUploadCheckbox.setAction(new NSSelector("preserveModificationUploadCheckboxClicked", new Class[]{NSButton.class}));
		this.preserveModificationUploadCheckbox.setState(Preferences.instance().getProperty("queue.upload.preserveDate").equals("true") ? NSCell.OnState : NSCell.OffState);
	}
	
	public void preserveModificationUploadCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("queue.upload.preserveDate", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("queue.upload.preserveDate", false);
				break;
		}
	}
	
	private NSButton horizontalLinesCheckbox; //IBOutlet

	public void setHorizontalLinesCheckbox(NSButton horizontalLinesCheckbox) {
		this.horizontalLinesCheckbox = horizontalLinesCheckbox;
		this.horizontalLinesCheckbox.setTarget(this);
		this.horizontalLinesCheckbox.setAction(new NSSelector("horizontalLinesCheckboxClicked", new Class[]{NSButton.class}));
		this.horizontalLinesCheckbox.setState(Preferences.instance().getProperty("browser.horizontalLines").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void horizontalLinesCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("browser.horizontalLines", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("browser.horizontalLines", false);
				break;
		}
		CDBrowserController.updateBrowserTableAttributes();
	}

	private NSButton verticalLinesCheckbox; //IBOutlet

	public void setVerticalLinesCheckbox(NSButton verticalLinesCheckbox) {
		this.verticalLinesCheckbox = verticalLinesCheckbox;
		this.verticalLinesCheckbox.setTarget(this);
		this.verticalLinesCheckbox.setAction(new NSSelector("verticalLinesCheckboxClicked", new Class[]{NSButton.class}));
		this.verticalLinesCheckbox.setState(Preferences.instance().getProperty("browser.verticalLines").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void verticalLinesCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("browser.verticalLines", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("browser.verticalLines", false);
				break;
		}
		CDBrowserController.updateBrowserTableAttributes();
	}

	private NSButton alternatingRowBackgroundCheckbox; //IBOutlet

	public void setAlternatingRowBackgroundCheckbox(NSButton alternatingRowBackgroundCheckbox) {
		this.alternatingRowBackgroundCheckbox = alternatingRowBackgroundCheckbox;
		this.alternatingRowBackgroundCheckbox.setTarget(this);
		this.alternatingRowBackgroundCheckbox.setAction(new NSSelector("alternatingRowBackgroundCheckboxClicked", new Class[]{NSButton.class}));
		this.alternatingRowBackgroundCheckbox.setState(Preferences.instance().getProperty("browser.alternatingRows").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void alternatingRowBackgroundCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("browser.alternatingRows", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("browser.alternatingRows", false);
				break;
		}
		CDBrowserController.updateBrowserTableAttributes();
	}

	private NSButton columnModificationCheckbox; //IBOutlet

	public void setColumnModificationCheckbox(NSButton columnModificationCheckbox) {
		this.columnModificationCheckbox = columnModificationCheckbox;
		this.columnModificationCheckbox.setTarget(this);
		this.columnModificationCheckbox.setAction(new NSSelector("columnModificationCheckboxClicked", new Class[]{NSButton.class}));
		this.columnModificationCheckbox.setState(Preferences.instance().getProperty("browser.columnModification").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void columnModificationCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("browser.columnModification", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("browser.columnModification", false);
				break;
		}
		CDBrowserController.updateBrowserTableColumns();
	}

	private NSButton columnOwnerCheckbox; //IBOutlet

	public void setColumnOwnerCheckbox(NSButton columnOwnerCheckbox) {
		this.columnOwnerCheckbox = columnOwnerCheckbox;
		this.columnOwnerCheckbox.setTarget(this);
		this.columnOwnerCheckbox.setAction(new NSSelector("columnOwnerCheckboxClicked", new Class[]{NSButton.class}));
		this.columnOwnerCheckbox.setState(Preferences.instance().getProperty("browser.columnOwner").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void columnOwnerCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("browser.columnOwner", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("browser.columnOwner", false);
				break;
		}
		CDBrowserController.updateBrowserTableColumns();
	}

	private NSButton columnPermissionsCheckbox; //IBOutlet

	public void setColumnPermissionsCheckbox(NSButton columnPermissionsCheckbox) {
		this.columnPermissionsCheckbox = columnPermissionsCheckbox;
		this.columnPermissionsCheckbox.setTarget(this);
		this.columnPermissionsCheckbox.setAction(new NSSelector("columnPermissionsCheckboxClicked", new Class[]{NSButton.class}));
		this.columnPermissionsCheckbox.setState(Preferences.instance().getProperty("browser.columnPermissions").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void columnPermissionsCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("browser.columnPermissions", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("browser.columnPermissions", false);
				break;
		}
		CDBrowserController.updateBrowserTableColumns();
	}

	private NSButton columnSizeCheckbox; //IBOutlet

	public void setColumnSizeCheckbox(NSButton columnSizeCheckbox) {
		this.columnSizeCheckbox = columnSizeCheckbox;
		this.columnSizeCheckbox.setTarget(this);
		this.columnSizeCheckbox.setAction(new NSSelector("columnSizeCheckboxClicked", new Class[]{NSButton.class}));
		this.columnSizeCheckbox.setState(Preferences.instance().getProperty("browser.columnSize").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void columnSizeCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("browser.columnSize", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("browser.columnSize", false);
				break;
		}
		CDBrowserController.updateBrowserTableColumns();
	}

	// public-key algorithms
	private static final String SSH_DSS = "ssh-dss";
	private static final String SSH_RSA = "ssh-rsa";

	private NSPopUpButton publickeyCombobox;

	public void setPublickeyCombobox(NSPopUpButton publickeyCombobox) {
		this.publickeyCombobox = publickeyCombobox;
		this.publickeyCombobox.setTarget(this);
		this.publickeyCombobox.setAction(new NSSelector("publickeyComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.publickeyCombobox.removeAllItems();
		this.publickeyCombobox.addItemsWithTitles(new NSArray(new String[]{
			//NSBundle.localizedString("Default", ""),
			SSH_DSS,
			SSH_RSA
		}));

		publickeyCombobox.setTitle(Preferences.instance().getProperty("ssh.publickey"));
	}
	
	public void publickeyComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("ssh.publickey", sender.titleOfSelectedItem());
	}

	//encryption ciphers
	private static final String des_cbc = "3des-cbc";
	private static final String blowfish_cbc = "blowfish-cbc";
	private static final String twofish256_cbc = "twofish256-cbc";
	private static final String twofish196_cbc = "twofish196-cbc";
	private static final String twofish128_cbc = "twofish128-cbc";
	private static final String aes256_cbc = "aes256-cbc";
	private static final String aes196_cbc = "aes196-cbc";
	private static final String aes128_cbc = "aes128-cbc";
	private static final String cast128_cbc = "cast128-cbc";

	private NSPopUpButton csEncryptionCombobox; //IBOutlet

	public void setCsEncryptionCombobox(NSPopUpButton csEncryptionCombobox) {
		this.csEncryptionCombobox = csEncryptionCombobox;
		this.csEncryptionCombobox.setTarget(this);
		this.csEncryptionCombobox.setAction(new NSSelector("csEncryptionComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.csEncryptionCombobox.removeAllItems();
		this.csEncryptionCombobox.addItemsWithTitles(new NSArray(new String[]{
			//NSBundle.localizedString("Default", ""),
			des_cbc,
			blowfish_cbc,
			twofish256_cbc,
			twofish196_cbc,
			twofish128_cbc,
			aes256_cbc,
			aes196_cbc,
			aes128_cbc,
			cast128_cbc
		}));

		this.csEncryptionCombobox.setTitle(Preferences.instance().getProperty("ssh.CSEncryption"));
	}

	public void csEncryptionComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("ssh.CSEncryption", sender.titleOfSelectedItem());
	}

	private NSPopUpButton scEncryptionCombobox; //IBOutlet

	public void setScEncryptionCombobox(NSPopUpButton scEncryptionCombobox) {
		this.scEncryptionCombobox = scEncryptionCombobox;
		this.scEncryptionCombobox.setTarget(this);
		this.scEncryptionCombobox.setAction(new NSSelector("scEncryptionComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.scEncryptionCombobox.removeAllItems();
		this.scEncryptionCombobox.addItemsWithTitles(new NSArray(new String[]{
			//NSBundle.localizedString("Default", ""),
			des_cbc,
			blowfish_cbc,
			twofish256_cbc,
			twofish196_cbc,
			twofish128_cbc,
			aes256_cbc,
			aes196_cbc,
			aes128_cbc,
			cast128_cbc
		}));

		this.scEncryptionCombobox.setTitle(Preferences.instance().getProperty("ssh.SCEncryption"));
	}

	public void scEncryptionComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("ssh.SCEncryption", sender.titleOfSelectedItem());
	}


	//authentication algorithms
	private static final String hmac_sha1 = "hmac-sha1";
	private static final String hmac_sha1_96 = "hmac-sha1-96";
	private static final String hmac_md5 = "hmac-md5";
	private static final String hmac_md5_96 = "hmac-md5-96";

	private NSPopUpButton scAuthenticationCombobox; //IBOutlet

	public void setScAuthenticationCombobox(NSPopUpButton scAuthenticationCombobox) {
		this.scAuthenticationCombobox = scAuthenticationCombobox;
		this.scAuthenticationCombobox.setTarget(this);
		this.scAuthenticationCombobox.setAction(new NSSelector("scAuthenticationComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.scAuthenticationCombobox.removeAllItems();
		this.scAuthenticationCombobox.addItemsWithTitles(new NSArray(new String[]{
			//NSBundle.localizedString("Default", ""),
			hmac_sha1,
			hmac_sha1_96,
			hmac_md5,
			hmac_md5_96
		}));

		this.scAuthenticationCombobox.setTitle(Preferences.instance().getProperty("ssh.SCAuthentication"));
	}

	public void scAuthenticationComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("ssh.SCAuthentication", sender.titleOfSelectedItem());
	}


	private NSPopUpButton csAuthenticationCombobox; //IBOutlet

	public void setCsAuthenticationCombobox(NSPopUpButton csAuthenticationCombobox) {
		this.csAuthenticationCombobox = csAuthenticationCombobox;
		this.csAuthenticationCombobox.setTarget(this);
		this.csAuthenticationCombobox.setAction(new NSSelector("csAuthenticationComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.csAuthenticationCombobox.removeAllItems();
		this.csAuthenticationCombobox.addItemsWithTitles(new NSArray(new String[]{
			//NSBundle.localizedString("Default", ""),
			hmac_sha1,
			hmac_sha1_96,
			hmac_md5,
			hmac_md5_96
		}));

		this.csAuthenticationCombobox.setTitle(Preferences.instance().getProperty("ssh.CSAuthentication"));
	}

	public void csAuthenticationComboboxClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("ssh.CSAuthentication", sender.titleOfSelectedItem());
	}

	private NSButton downloadPathButton; //IBOutlet

	public void setDownloadPathButton(NSButton downloadPathButton) {
		this.downloadPathButton = downloadPathButton;
		this.downloadPathButton.setTarget(this);
		this.downloadPathButton.setAction(new NSSelector("downloadPathButtonClicked", new Class[]{NSButton.class}));
	}

	public void downloadPathButtonClicked(NSButton sender) {
		NSOpenPanel panel = NSOpenPanel.openPanel();
		panel.setCanChooseFiles(false);
		panel.setCanChooseDirectories(true);
		panel.setAllowsMultipleSelection(false);
		panel.beginSheetForDirectory(null, null, null, this.window(), this, new NSSelector("openPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
	}

	public void openPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
		switch(returnCode) {
			case (NSAlertPanel.DefaultReturn):
				{
					NSArray selected = sheet.filenames();
					String filename;
					if((filename = (String)selected.lastObject()) != null) {
						Preferences.instance().setProperty("queue.download.folder", filename);
						this.downloadPathField.setStringValue(Preferences.instance().getProperty("queue.download.folder"));
					}
					break;
				}
			case (NSAlertPanel.AlternateReturn):
				{
					break;
				}
		}
	}

	private NSButton defaultBufferButton; //IBOutlet

	public void setDefaultBufferButton(NSButton defaultBufferButton) {
		this.defaultBufferButton = defaultBufferButton;
		this.defaultBufferButton.setTarget(this);
		this.defaultBufferButton.setAction(new NSSelector("defaultBufferButtonClicked", new Class[]{NSButton.class}));
	}

	public void defaultBufferButtonClicked(NSButton sender) {
		Preferences.instance().setProperty("connection.buffer", Preferences.instance().getProperty("connection.buffer.default"));
		try {
			int bytes = Integer.parseInt(Preferences.instance().getProperty("connection.buffer"));
			int kbit = bytes/1024*8;
			this.bufferField.setStringValue(""+kbit);
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
	}

	private NSTextField concurrentConnectionsField; //IBOutlet

	public void setConcurrentConnectionsField(NSTextField concurrentConnectionsField) {
		this.concurrentConnectionsField = concurrentConnectionsField;
		this.concurrentConnectionsField.setStringValue(Preferences.instance().getProperty("connection.pool.max"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("concurrentConnectionsFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.concurrentConnectionsField);
	}

	public void concurrentConnectionsFieldDidChange(NSNotification sender) {
		try {
			int max = Integer.parseInt(this.concurrentConnectionsField.stringValue());
			Preferences.instance().setProperty("connection.pool.max", max);
			synchronized(SessionPool.instance()) {
				SessionPool.instance().notifyAll();
			}
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
	}

	private NSTextField concurrentConnectionsTimeoutField; //IBOutlet

	public void setConcurrentConnectionsTimeoutField(NSTextField concurrentConnectionsTimeoutField) {
		this.concurrentConnectionsTimeoutField = concurrentConnectionsTimeoutField;
		this.concurrentConnectionsTimeoutField.setStringValue(Preferences.instance().getProperty("connection.pool.timeout"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("concurrentConnectionsTimeoutFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.concurrentConnectionsTimeoutField);
	}

	public void concurrentConnectionsTimeoutFieldDidChange(NSNotification sender) {
		try {
			int timeout = Integer.parseInt(this.concurrentConnectionsTimeoutField.stringValue());
			Preferences.instance().setProperty("connection.pool.timeout", timeout);
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
	}

	private NSTextField bufferField; //IBOutlet

	public void setBufferField(NSTextField bufferField) {
		this.bufferField = bufferField;
		try {
			int bytes = Integer.parseInt(Preferences.instance().getProperty("connection.buffer"));
			int kbit = bytes/1024*8;
			this.bufferField.setStringValue(""+kbit);
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("bufferFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.bufferField);
	}

	public void bufferFieldDidChange(NSNotification sender) {
		try {
			int kbit = Integer.parseInt(this.bufferField.stringValue());
			Preferences.instance().setProperty("connection.buffer", (int)kbit/8*1024); //Bytes
		}
		catch(NumberFormatException e) {
			log.error(e.getMessage());
		}
	}

	private NSTextField userAgentField; //IBOutlet

	public void setUserAgentField(NSTextField userAgentField) {
		this.userAgentField = userAgentField;
		this.userAgentField.setStringValue(Preferences.instance().getProperty("http.agent"));
	}

	private NSTextField anonymousField; //IBOutlet

	public void setAnonymousField(NSTextField anonymousField) {
		this.anonymousField = anonymousField;
		this.anonymousField.setStringValue(Preferences.instance().getProperty("ftp.anonymous.pass"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("anonymousFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.anonymousField);
	}

	public void anonymousFieldDidChange(NSNotification sender) {
		Preferences.instance().setProperty("ftp.anonymous.pass", this.anonymousField.stringValue());
	}

	private NSTextField downloadPathField; //IBOutlet

	public void setDownloadPathField(NSTextField downloadPathField) {
		this.downloadPathField = downloadPathField;
		this.downloadPathField.setStringValue(Preferences.instance().getProperty("queue.download.folder"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("downloadPathFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.downloadPathField);
	}

	public void downloadPathFieldDidChange(NSNotification sender) {
		Preferences.instance().setProperty("queue.download.folder", this.downloadPathField.stringValue());
	}

	private NSTextField extensionsField; //IBOutlet

	public void setExtensionsField(NSTextField extensionsField) {
		this.extensionsField = extensionsField;
		this.extensionsField.setStringValue(Preferences.instance().getProperty("ftp.transfermode.ascii.extensions"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("extensionsFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.extensionsField);
	}

	public void extensionsFieldDidChange(NSNotification sender) {
		Preferences.instance().setProperty("ftp.transfermode.ascii.extensions", this.extensionsField.stringValue());
	}

	private NSTextField loginField; //IBOutlet

	public void setLoginField(NSTextField loginField) {
		this.loginField = loginField;
		this.loginField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("loginFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.loginField);
	}

	public void loginFieldDidChange(NSNotification sender) {
		Preferences.instance().setProperty("connection.login.name", this.loginField.stringValue());
	}

	private NSButton keychainCheckbox; //IBOutlet

	public void setKeychainCheckbox(NSButton keychainCheckbox) {
		this.keychainCheckbox = keychainCheckbox;
		this.keychainCheckbox.setTarget(this);
		this.keychainCheckbox.setAction(new NSSelector("keychainCheckboxClicked", new Class[]{NSButton.class}));
		this.keychainCheckbox.setState(Preferences.instance().getProperty("connection.login.useKeychain").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void keychainCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("connection.login.useKeychain", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("connection.login.useKeychain", false);
				break;
		}
	}

	private NSButton doubleClickCheckbox; //IBOutlet

	public void setDoubleClickCheckbox(NSButton doubleClickCheckbox) {
		this.doubleClickCheckbox = doubleClickCheckbox;
		this.doubleClickCheckbox.setTarget(this);
		this.doubleClickCheckbox.setAction(new NSSelector("doubleClickCheckboxClicked", new Class[]{NSButton.class}));
		this.doubleClickCheckbox.setState(Preferences.instance().getProperty("browser.doubleclick.edit").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void doubleClickCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("browser.doubleclick.edit", "true");
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("browser.doubleclick.edit", "false");
				break;
		}
	}

	private NSButton showHiddenCheckbox; //IBOutlet

	public void setShowHiddenCheckbox(NSButton showHiddenCheckbox) {
		this.showHiddenCheckbox = showHiddenCheckbox;
		this.showHiddenCheckbox.setTarget(this);
		this.showHiddenCheckbox.setAction(new NSSelector("showHiddenCheckboxClicked", new Class[]{NSButton.class}));
		this.showHiddenCheckbox.setState(Preferences.instance().getProperty("browser.showHidden").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void showHiddenCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("browser.showHidden", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("browser.showHidden", false);
				break;
		}
	}

	private NSButton newBrowserCheckbox; //IBOutlet

	public void setNewBrowserCheckbox(NSButton newBrowserCheckbox) {
		this.newBrowserCheckbox = newBrowserCheckbox;
		this.newBrowserCheckbox.setTarget(this);
		this.newBrowserCheckbox.setAction(new NSSelector("newBrowserCheckboxClicked", new Class[]{NSButton.class}));
		this.newBrowserCheckbox.setState(Preferences.instance().getProperty("browser.openByDefault").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void newBrowserCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("browser.openByDefault", true);
//				this.defaultHostCombobox.setEnabled(true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("browser.openByDefault", false);
//				this.defaultHostCombobox.setEnabled(false);
				break;
		}
	}

	private NSButton bringQueueToFrontCheckbox; //IBOutlet

	public void setBringQueueToFrontCheckbox(NSButton bringQueueToFrontCheckbox) {
		this.bringQueueToFrontCheckbox = bringQueueToFrontCheckbox;
		this.bringQueueToFrontCheckbox.setTarget(this);
		this.bringQueueToFrontCheckbox.setAction(new NSSelector("bringQueueToFrontCheckboxClicked", new Class[]{NSButton.class}));
		this.bringQueueToFrontCheckbox.setState(Preferences.instance().getProperty("queue.orderFrontOnTransfer").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void bringQueueToFrontCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("queue.orderFrontOnTransfer", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("queue.orderFrontOnTransfer", false);
				break;
		}
	}

	private NSButton removeFromQueueCheckbox; //IBOutlet

	public void setRemoveFromQueueCheckbox(NSButton removeFromQueueCheckbox) {
		this.removeFromQueueCheckbox = removeFromQueueCheckbox;
		this.removeFromQueueCheckbox.setTarget(this);
		this.removeFromQueueCheckbox.setAction(new NSSelector("removeFromQueueCheckboxClicked", new Class[]{NSButton.class}));
		this.removeFromQueueCheckbox.setState(Preferences.instance().getProperty("queue.removeItemWhenComplete").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void removeFromQueueCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("queue.removeItemWhenComplete", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("queue.removeItemWhenComplete", false);
				break;
		}
	}

	private NSButton processCheckbox; //IBOutlet

	public void setProcessCheckbox(NSButton processCheckbox) {
		this.processCheckbox = processCheckbox;
		this.processCheckbox.setTarget(this);
		this.processCheckbox.setAction(new NSSelector("processCheckboxClicked", new Class[]{NSButton.class}));
		this.processCheckbox.setState(Preferences.instance().getProperty("queue.postProcessItemWhenComplete").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void processCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("queue.postProcessItemWhenComplete", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("queue.postProcessItemWhenComplete", false);
				break;
		}
	}

	private NSPopUpButton duplicateCombobox; //IBOutlet

	public void setDuplicateCombobox(NSPopUpButton duplicateCombobox) {
		this.duplicateCombobox = duplicateCombobox;
		this.duplicateCombobox.setTarget(this);
		this.duplicateCombobox.setAction(new NSSelector("duplicateComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.duplicateCombobox.removeAllItems();
		this.duplicateCombobox.addItemsWithTitles(new NSArray(new String[]{ASK_ME_WHAT_TO_DO, OVERWRITE_EXISTING_FILE, TRY_TO_RESUME_TRANSFER, USE_A_SIMILAR_NAME}));
		if(Preferences.instance().getProperty("queue.fileExists").equals("ask")) {
			this.duplicateCombobox.setTitle(ASK_ME_WHAT_TO_DO);
		}
		if(Preferences.instance().getProperty("queue.fileExists").equals("overwrite")) {
			this.duplicateCombobox.setTitle(OVERWRITE_EXISTING_FILE);
		}
		else if(Preferences.instance().getProperty("queue.fileExists").equals("resume")) {
			this.duplicateCombobox.setTitle(TRY_TO_RESUME_TRANSFER);
		}
		else if(Preferences.instance().getProperty("queue.fileExists").equals("similar")) {
			this.duplicateCombobox.setTitle(USE_A_SIMILAR_NAME);
		}
	}

	public void duplicateComboboxClicked(NSPopUpButton sender) {
		if(sender.selectedItem().title().equals(ASK_ME_WHAT_TO_DO)) {
			Preferences.instance().setProperty("queue.fileExists", "ask");
		}
		if(sender.selectedItem().title().equals(OVERWRITE_EXISTING_FILE)) {
			Preferences.instance().setProperty("queue.fileExists", "overwrite");
		}
		else if(sender.selectedItem().title().equals(TRY_TO_RESUME_TRANSFER)) {
			Preferences.instance().setProperty("queue.fileExists", "resume");
		}
		else if(sender.selectedItem().title().equals(USE_A_SIMILAR_NAME)) {
			Preferences.instance().setProperty("queue.fileExists", "similar");
		}
	}

	private NSPopUpButton lineEndingCombobox; //IBOutlet

	public void setLineEndingCombobox(NSPopUpButton lineEndingCombobox) {
		this.lineEndingCombobox = lineEndingCombobox;
		this.lineEndingCombobox.setTarget(this);
		this.lineEndingCombobox.setAction(new NSSelector("lineEndingComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.lineEndingCombobox.removeAllItems();
		this.lineEndingCombobox.addItemsWithTitles(new NSArray(new String[]{UNIX_LINE_ENDINGS, MAC_LINE_ENDINGS, WINDOWS_LINE_ENDINGS}));
		if(Preferences.instance().getProperty("ftp.line.separator").equals("unix")) {
			this.lineEndingCombobox.setTitle(UNIX_LINE_ENDINGS);
		}
		else if(Preferences.instance().getProperty("ftp.line.separator").equals("mac")) {
			this.lineEndingCombobox.setTitle(MAC_LINE_ENDINGS);
		}
		else if(Preferences.instance().getProperty("ftp.line.separator").equals("win")) {
			this.lineEndingCombobox.setTitle(WINDOWS_LINE_ENDINGS);
		}
	}

	public void lineEndingComboboxClicked(NSPopUpButton sender) {
		if(sender.selectedItem().title().equals(UNIX_LINE_ENDINGS)) {
			Preferences.instance().setProperty("ftp.line.separator", "unix");
		}
		else if(sender.selectedItem().title().equals(MAC_LINE_ENDINGS)) {
			Preferences.instance().setProperty("ftp.line.separator", "mac");
		}
		else if(sender.selectedItem().title().equals(WINDOWS_LINE_ENDINGS)) {
			Preferences.instance().setProperty("ftp.line.separator", "win");
		}
	}


	private NSPopUpButton transfermodeCombobox; //IBOutlet

	public void setTransfermodeCombobox(NSPopUpButton transfermodeCombobox) {
		this.transfermodeCombobox = transfermodeCombobox;
		this.transfermodeCombobox.setTarget(this);
		this.transfermodeCombobox.setAction(new NSSelector("transfermodeComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.transfermodeCombobox.removeAllItems();
		this.transfermodeCombobox.addItemsWithTitles(new NSArray(new String[]{TRANSFERMODE_AUTO, TRANSFERMODE_BINARY, TRANSFERMODE_ASCII}));
		if(Preferences.instance().getProperty("ftp.transfermode").equals("binary")) {
			this.transfermodeCombobox.setTitle(TRANSFERMODE_BINARY);
		}
		else if(Preferences.instance().getProperty("ftp.transfermode").equals("ascii")) {
			this.transfermodeCombobox.setTitle(TRANSFERMODE_ASCII);
		}
		else if(Preferences.instance().getProperty("ftp.transfermode").equals("auto")) {
			this.transfermodeCombobox.setTitle(TRANSFERMODE_AUTO);
		}
	}

	public void transfermodeComboboxClicked(NSPopUpButton sender) {
		if(sender.selectedItem().title().equals(TRANSFERMODE_BINARY)) {
			Preferences.instance().setProperty("ftp.transfermode", "binary");
			this.lineEndingCombobox.setEnabled(false);
			this.extensionsField.setEnabled(false);
		}
		else if(sender.selectedItem().title().equals(TRANSFERMODE_ASCII)) {
			Preferences.instance().setProperty("ftp.transfermode", "ascii");
			this.lineEndingCombobox.setEnabled(true);
			this.extensionsField.setEnabled(false);
		}
		else if(sender.selectedItem().title().equals(TRANSFERMODE_AUTO)) {
			Preferences.instance().setProperty("ftp.transfermode", "auto");
			this.lineEndingCombobox.setEnabled(true);
			this.extensionsField.setEnabled(true);
		}
	}

	private NSPopUpButton connectmodeCombobox; //IBOutlet

	public void setConnectmodeCombobox(NSPopUpButton connectmodeCombobox) {
		this.connectmodeCombobox = connectmodeCombobox;
		this.connectmodeCombobox.setTarget(this);
		this.connectmodeCombobox.setAction(new NSSelector("connectmodeComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.connectmodeCombobox.removeAllItems();
		this.connectmodeCombobox.addItemsWithTitles(new NSArray(new String[]{CONNECTMODE_ACTIVE, CONNECTMODE_PASSIVE}));
		if(Preferences.instance().getProperty("ftp.connectmode").equals("passive")) {
			this.connectmodeCombobox.setTitle(CONNECTMODE_PASSIVE);
		}
		else {
			this.connectmodeCombobox.setTitle(CONNECTMODE_ACTIVE);
		}
	}

	public void connectmodeComboboxClicked(NSPopUpButton sender) {
		if(sender.selectedItem().title().equals(CONNECTMODE_ACTIVE)) {
			Preferences.instance().setProperty("ftp.connectmode", "active");
		}
		else if(sender.selectedItem().title().equals(CONNECTMODE_PASSIVE)) {
			Preferences.instance().setProperty("ftp.connectmode", "passive");
		}
	}

	private NSPopUpButton protocolCombobox; //IBOutlet

	public void setProtocolCombobox(NSPopUpButton protocolCombobox) {
		this.protocolCombobox = protocolCombobox;
		this.protocolCombobox.setTarget(this);
		this.protocolCombobox.setAction(new NSSelector("protocolComboboxClicked", new Class[]{NSPopUpButton.class}));
		this.protocolCombobox.removeAllItems();
		this.protocolCombobox.addItemsWithTitles(new NSArray(new String[]{PROTOCOL_FTP, PROTOCOL_SFTP}));
		if(Preferences.instance().getProperty("connection.protocol.default").equals("ftp")) {
			this.protocolCombobox.setTitle(PROTOCOL_FTP);
		}
		else {
			this.protocolCombobox.setTitle(PROTOCOL_SFTP);
		}
	}

	public void protocolComboboxClicked(NSPopUpButton sender) {
		if(sender.selectedItem().title().equals(PROTOCOL_FTP)) {
			Preferences.instance().setProperty("connection.protocol.default", Session.FTP);
			Preferences.instance().setProperty("connection.port.default", Session.FTP_PORT);
		}
		else {
			Preferences.instance().setProperty("connection.protocol.default", Session.SFTP);
			Preferences.instance().setProperty("connection.port.default", Session.SSH_PORT);
		}
	}

	private NSButton autoUpdateCheckbox;

	public void setAutoUpdateCheckbox(NSButton autoUpdateCheckbox) {
		this.autoUpdateCheckbox = autoUpdateCheckbox;
		this.autoUpdateCheckbox.setTarget(this);
		this.autoUpdateCheckbox.setAction(new NSSelector("autoUpdateCheckboxClicked", new Class[]{NSButton.class}));
		this.autoUpdateCheckbox.setState(Preferences.instance().getProperty("update.check").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	public void autoUpdateCheckboxClicked(NSButton sender) {
		switch(sender.state()) {
			case NSCell.OnState:
				Preferences.instance().setProperty("update.check", true);
				break;
			case NSCell.OffState:
				Preferences.instance().setProperty("update.check", false);
				break;
		}
	}
}
