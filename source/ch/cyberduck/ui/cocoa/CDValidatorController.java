package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.Validator;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class CDValidatorController
        extends CDSheetController implements Validator {
    private static Logger log = Logger.getLogger(CDValidatorController.class);

    /**
     * The list of files displayed in the table to be included or excluded by the user
     */
    protected List workList = new ArrayList();

    public Collection result() {
        if(this.hasPrompt()) {
            this.statusIndicator.stopAnimation(null);
            this.setEnabled(true);
            this.waitForSheetEnd();
        }
        return workList;
    }

    private boolean canceled = false;

    public boolean isCanceled() {
        return this.canceled;
    }

    public CDValidatorController() {
        super(CDQueueController.instance());
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION || returncode == ALTERNATE_OPTION) { //overwrite || resume
            for(Iterator i = workList.iterator(); i.hasNext();) {
                final Path p = (Path) i.next();
                if(p.isSkipped()) {
                    this.workList.remove(p);
                    continue;
                }
                p.status.setResume(returncode == ALTERNATE_OPTION);
            }
        }
        if(returncode == SKIP_OPTION || returncode == CANCEL_OPTION) { //skip
            this.workList.clear();
        }
        if(returncode == CANCEL_OPTION) {
            this.canceled = true;
        }
    }

    /**
     * State variable to know if we are about to
     * display a sheet
     */
    protected boolean hasPrompt = false;

    /**
     * @return true if the sheet dialog is displayed
     */
    protected boolean hasPrompt() {
        return this.hasPrompt;
    }

    /**
     * @see CDQueueController
     * @param p
     */
    public void prompt(Path p) {
        if(!this.hasPrompt()) {
            // We should not call parent.hasSheet() because the parent
            // is a singleton class that may have a sheet from another
            // ongoing transfer
            this.beginSheet(false);
            this.hasPrompt = true;
        }
        this.workList.add(p);
        this.fireDataChanged();
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSTextField remoteURLField; // IBOutlet

    public void setRemoteURLField(NSTextField f) {
        this.remoteURLField = f;
        this.remoteURLField.setHidden(true);
    }

    private NSTextField remoteSizeField; // IBOutlet

    public void setRemoteSizeField(NSTextField f) {
        this.remoteSizeField = f;
        this.remoteSizeField.setHidden(true);
    }

    private NSTextField remoteModificationField; // IBOutlet

    public void setRemoteModificationField(NSTextField f) {
        this.remoteModificationField = f;
        this.remoteModificationField.setHidden(true);
    }

    private NSTextField localURLField; // IBOutlet

    public void setLocalURLField(NSTextField f) {
        this.localURLField = f;
        this.localURLField.setHidden(true);
    }

    private NSTextField localSizeField; // IBOutlet

    public void setLocalSizeField(NSTextField f) {
        this.localSizeField = f;
        this.localSizeField.setHidden(true);
    }

    private NSTextField localModificationField; // IBOutlet

    public void setLocalModificationField(NSTextField f) {
        this.localModificationField = f;
        this.localModificationField.setHidden(true);
    }


    private NSProgressIndicator statusIndicator; // IBOutlet

    public void setStatusIndicator(NSProgressIndicator f) {
        this.statusIndicator = f;
        this.statusIndicator.setUsesThreadedAnimation(true);
        this.statusIndicator.setDisplayedWhenStopped(false);
        this.statusIndicator.startAnimation(null);
    }

    protected NSTableView fileTableView; // IBOutlet
    private CDTableDelegate fileTableViewDelegate;

    public void setFileTableView(NSTableView view) {
        this.fileTableView = view;
        this.fileTableView.setHeaderView(null);
        this.fileTableView.setDataSource(this);
        this.fileTableView.setDelegate(this.fileTableViewDelegate = new CDAbstractTableDelegate() {

            public String tableViewToolTipForCell(NSTableView tableView, NSCell cell, NSMutableRect rect,
                                                  NSTableColumn tc, int row, NSPoint mouseLocation) {
                if(row < numberOfRowsInTableView(tableView)) {
                    return super.tooltipForPath((Path) workList.get(row));
                }
                return null;
            }

            public void enterKeyPressed(final Object sender) {
                ;
            }

            public void deleteKeyPressed(final Object sender) {
                ;
            }

            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
                ;
            }

            public void tableRowDoubleClicked(final Object sender) {
                ;
            }

            public void selectionDidChange(NSNotification notification) {
                if(fileTableView.selectedRow() != -1) {
                    Path p = (Path) workList.get(fileTableView.selectedRow());
                    if(p != null) {
                        if(p.getLocal().exists()) {
                            localURLField.setAttributedStringValue(
                                    new NSAttributedString(p.getLocal().getAbsolute(),
                                            TRUNCATE_MIDDLE_ATTRIBUTES));
                            localURLField.setHidden(false);
                            localSizeField.setAttributedStringValue(
                                    new NSAttributedString(Status.getSizeAsString(p.getLocal().attributes.getSize()),
                                            TRUNCATE_MIDDLE_ATTRIBUTES));
                            localSizeField.setHidden(false);
                            localModificationField.setAttributedStringValue(
                                    new NSAttributedString(CDDateFormatter.getLongFormat(p.getLocal().attributes.getTimestamp()),
                                            TRUNCATE_MIDDLE_ATTRIBUTES));
                            localModificationField.setHidden(false);
                        }
                        else {
                            localURLField.setHidden(true);
                            localSizeField.setHidden(true);
                            localModificationField.setHidden(true);
                        }
                        if(p.getRemote().exists()) {
                            remoteURLField.setAttributedStringValue(
                                    new NSAttributedString(p.getRemote().getHost().getURL() + p.getRemote().getAbsolute(),
                                            TRUNCATE_MIDDLE_ATTRIBUTES));
                            remoteURLField.setHidden(false);
                            remoteSizeField.setAttributedStringValue(
                                    new NSAttributedString(Status.getSizeAsString(p.getRemote().attributes.getSize()),
                                            TRUNCATE_MIDDLE_ATTRIBUTES));
                            remoteSizeField.setHidden(false);
                            remoteModificationField.setAttributedStringValue(
                                    new NSAttributedString(CDDateFormatter.getLongFormat(p.getRemote().attributes.getTimestamp()),
                                            TRUNCATE_MIDDLE_ATTRIBUTES));
                            remoteModificationField.setHidden(false);
                        }
                        else {
                            remoteURLField.setHidden(true);
                            remoteSizeField.setHidden(true);
                            remoteModificationField.setHidden(true);
                        }
                    }
                }
                else {
                    remoteURLField.setHidden(true);
                    remoteSizeField.setHidden(true);
                    remoteModificationField.setHidden(true);
                    localURLField.setHidden(true);
                    localSizeField.setHidden(true);
                    localModificationField.setHidden(true);
                }
            }
        });
        this.fileTableView.setRowHeight(17f);
        // selection properties
        this.fileTableView.setAllowsMultipleSelection(true);
        this.fileTableView.setAllowsEmptySelection(true);
        this.fileTableView.setAllowsColumnResizing(true);
        this.fileTableView.setAllowsColumnSelection(false);
        this.fileTableView.setAllowsColumnReordering(true);
        this.fileTableView.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
        if(Preferences.instance().getBoolean("browser.horizontalLines") && Preferences.instance().getBoolean("browser.verticalLines"))
        {
            this.fileTableView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
        }
        else if(Preferences.instance().getBoolean("browser.verticalLines")) {
            this.fileTableView.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
        }
        else if(Preferences.instance().getBoolean("browser.horizontalLines")) {
            this.fileTableView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
        }
        else {
            this.fileTableView.setGridStyleMask(NSTableView.GridNone);
        }
        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(INCLUDE_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setEditable(false);
            NSButtonCell cell = new NSButtonCell();
            cell.setControlSize(NSCell.SmallControlSize);
            cell.setButtonType(NSButtonCell.SwitchButton);
            cell.setAllowsMixedState(false);
            cell.setTarget(this);
            c.setDataCell(cell);
            c.dataCell().setAlignment(NSText.CenterTextAlignment);
            this.fileTableView.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(ICON_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setEditable(false);
            c.setDataCell(new NSImageCell());
            c.dataCell().setAlignment(NSText.CenterTextAlignment);
            this.fileTableView.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Filename", "A column in the browser"));
            c.setIdentifier(FILENAME_COLUMN);
            c.setMinWidth(100f);
            c.setWidth(220f);
            c.setMaxWidth(800f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask | NSTableColumn.UserResizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setEditable(false);
            c.setDataCell(new NSTextFieldCell());
            this.fileTableView.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(SIZE_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(50f);
            c.setWidth(80f);
            c.setMaxWidth(100f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask | NSTableColumn.UserResizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setEditable(false);
            c.setDataCell(new NSTextFieldCell());
            this.fileTableView.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier(WARNING_COLUMN);
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setEditable(false);
            c.setDataCell(new NSImageCell());
            c.dataCell().setAlignment(NSText.CenterTextAlignment);
            this.fileTableView.addTableColumn(c);
        }
        this.fileTableView.sizeToFit();
    }

    protected NSButton skipButton; // IBOutlet

    public void setSkipButton(NSButton skipButton) {
        this.skipButton = skipButton;
    }

    protected NSButton resumeButton; // IBOutlet

    public void setResumeButton(NSButton resumeButton) {
        this.resumeButton = resumeButton;
    }

    protected NSButton overwriteButton; // IBOutlet

    public void setOverwriteButton(NSButton overwriteButton) {
        this.overwriteButton = overwriteButton;
    }

    protected void setEnabled(boolean enabled) {
        this.overwriteButton.setEnabled(enabled);
        this.resumeButton.setEnabled(enabled);
        this.skipButton.setEnabled(enabled);
    }

    /**
     * Notify the view that the model has changed
     */
    protected void fireDataChanged() {
        this.fileTableView.noteNumberOfRowsChanged();
    }

    protected static final String INCLUDE_COLUMN = "INCLUDE";
    protected static final String ICON_COLUMN = "ICON";
    protected static final String WARNING_COLUMN = "WARNING";
    protected static final String FILENAME_COLUMN = "FILENAME";
    protected static final String SIZE_COLUMN = "SIZE";
    // virtual column to implement keyboard selection
    protected static final String TYPEAHEAD_COLUMN = "TYPEAHEAD";

    /**
     * @see NSTableView.DataSource
     */
    public void tableViewSetObjectValueForLocation(NSTableView view, Object object, NSTableColumn tableColumn, int row) {
        if(row < this.numberOfRowsInTableView(view)) {
            String identifier = (String) tableColumn.identifier();
            if(identifier.equals(INCLUDE_COLUMN)) {
                Path p = (Path) this.workList.get(row);
                p.setSkipped(((Integer) object).intValue() == NSCell.OffState);
            }
        }
    }

    protected static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");
    protected static final NSImage ALERT_ICON = NSImage.imageNamed("alert.tiff");
    protected static final NSImage NOT_FOUND_ICON = NSImage.imageNamed("notfound.tiff");

    /**
     * @see NSTableView.DataSource
     */
    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        String identifier = (String) tableColumn.identifier();
        Path p = (Path) this.workList.get(row);
        if(identifier.equals(INCLUDE_COLUMN)) {
            if(p.isSkipped())
                return new Integer(NSCell.OffState);
            return new Integer(NSCell.OnState);
        }
        if(identifier.equals(ICON_COLUMN)) {
            if(p.attributes.isDirectory()) {
                return FOLDER_ICON;
            }
            if(p.attributes.isFile()) {
                NSImage icon = CDIconCache.instance().get(p.getExtension());
                icon.setSize(new NSSize(16f, 16f));
                return icon;
            }
            return NOT_FOUND_ICON;
        }
        if(identifier.equals(FILENAME_COLUMN)) {
            return new NSAttributedString(p.getRemote().getName(),
                    CDTableCell.PARAGRAPH_DICTIONARY_LEFT_ALIGNEMENT);
        }
        if(identifier.equals(TYPEAHEAD_COLUMN)) {
            return p.getRemote().getName();
        }
        return null;
    }

    /**
     * @see NSTableView.DataSource
     */
    public int numberOfRowsInTableView(NSTableView view) {
        return this.workList.size();
    }
}