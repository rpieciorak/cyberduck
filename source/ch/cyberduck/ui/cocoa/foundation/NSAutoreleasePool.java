package ch.cyberduck.ui.cocoa.foundation;

import org.rococoa.Rococoa;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
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

/**
 * @version $Id:$
 */
public abstract class NSAutoreleasePool implements NSObject {
    public static NSAutoreleasePool push() {
        return Rococoa.create("NSAutoreleasePool", NSAutoreleasePool.class);
    }
    
    public interface _Class extends org.rococoa.NSClass {
        /**
         * <i>native declaration : :16</i><br>
         * Conversion Error : /// Original signature : <code>void addObject(null)</code><br>
         * + (void)addObject:(null)anObject; (Argument anObject cannot be converted)
         */
        NSAutoreleasePool alloc();
    }

    /**
     * <i>native declaration : :18</i><br>
     * Conversion Error : /// Original signature : <code>void addObject(null)</code><br>
     * - (void)addObject:(null)anObject; (Argument anObject cannot be converted)
     */
    public abstract void addObject(org.rococoa.ID anObject);

    /**
     * Original signature : <code>void drain()</code><br>
     * <i>native declaration : :21</i>
     */
    public abstract void drain();
}
