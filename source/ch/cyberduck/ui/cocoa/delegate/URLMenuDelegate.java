package ch.cyberduck.ui.cocoa.delegate;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.Action;
import ch.cyberduck.ui.cocoa.IconCache;
import ch.cyberduck.ui.cocoa.TableCellAttributes;
import ch.cyberduck.ui.cocoa.application.NSColor;
import ch.cyberduck.ui.cocoa.application.NSFont;
import ch.cyberduck.ui.cocoa.application.NSMenu;
import ch.cyberduck.ui.cocoa.application.NSMenuItem;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id:$
 */
public abstract class URLMenuDelegate extends AbstractMenuDelegate {
    private static Logger log = Logger.getLogger(URLMenuDelegate.class);

    protected static final NSDictionary URL_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(NSFont.userFontOfSize(NSFont.smallSystemFontSize()), NSColor.darkGrayColor(),
                    TableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
            NSArray.arrayWithObjects(NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    /**
     * @return Path selected in the browser or current working directory.
     */
    protected abstract List<Path> getSelected();

    protected abstract String getLabel();

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        List<Path> selected = this.getSelected();
        if(selected.isEmpty()) {
            return new NSInteger(1);
        }
        // Number of URLs for a single path
        int urls = this.getURLs(selected.iterator().next()).size();
        return new NSInteger(urls * 2);
    }

    protected List<AbstractPath.DescriptiveUrl> getURLs(Path selected) {
        return new ArrayList<AbstractPath.DescriptiveUrl>(selected.getURLs());
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        item.setTarget(this.id());
        List<Path> selected = this.getSelected();
        if(0 == index.intValue()) {
            this.setShortcut(item, this.getKeyEquivalent(), this.getModifierMask());
        }
        else {
            this.clearShortcut(item);
        }
        if(selected.isEmpty()) {
            item.setTitle(MessageFormat.format(Locale.localizedString("{0} URL"), Locale.localizedString("Unknown")));
            item.setAction(null);
            item.setImage(null);
        }
        else {
            final StringBuilder builder = new StringBuilder();
            for(Iterator<Path> iter = selected.iterator(); iter.hasNext();) {
                List<AbstractPath.DescriptiveUrl> urls = this.getURLs(iter.next());
                AbstractPath.DescriptiveUrl url = urls.get(index.intValue() / 2);
                builder.append(url.getUrl());
                if(iter.hasNext()) {
                    builder.append("\n");
                }
            }
            String s = builder.toString();
            boolean label = index.intValue() % 2 == 0;
            if(label) {
                item.setEnabled(true);
                item.setImage(IconCache.iconNamed("site.tiff", 16));
                item.setAction(this.getDefaultAction());
                Iterator<Path> iter = selected.iterator();
                AbstractPath.DescriptiveUrl url = this.getURLs(iter.next()).get(index.intValue() / 2);
                item.setRepresentedObject(s);
                item.setTitle(url.getHelp());
            }
            else {
                // Dummy menu item to preview URL only
                if(StringUtils.isNotBlank(s)) {
                    item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(s, URL_FONT_ATTRIBUTES));
                }
                else {
                    item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(Locale.localizedString("Unknown"), URL_FONT_ATTRIBUTES));
                }
            }
        }
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    @Action
    public abstract void urlClicked(final NSMenuItem sender);

    @Override
    public boolean validateMenuItem(NSMenuItem item) {
        if(this.getSelected().isEmpty()) {
            return false;
        }
        final Selector action = item.action();
        if(action.equals(this.getDefaultAction())) {
            return StringUtils.isNotBlank(item.representedObject());
        }
        return true;
    }

    @Override
    protected ID getTarget() {
        return this.id();
    }

    @Override
    protected Selector getDefaultAction() {
        return Foundation.selector("urlClicked:");
    }
}