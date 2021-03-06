package ch.cyberduck.ui.growl;

/*
 *  Copyright (c) 2010 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Factory;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id: GrowlFactory.java 5451 2009-10-09 08:34:10Z dkocher $
 */
public abstract class GrowlFactory extends Factory<Growl> {

    /**
     * Registered factories
     */
    protected static final Map<Platform, GrowlFactory> factories = new HashMap<Platform, GrowlFactory>();

    /**
     * @param platform
     * @param f
     */
    public static void addFactory(Platform platform, GrowlFactory f) {
        factories.put(platform, f);
    }

    private static Growl l;

    /**
     * @return
     */
    public static Growl createGrowl() {
        if(null == l) {
            if(!factories.containsKey(NATIVE_PLATFORM)) {
                throw new RuntimeException("No implementation for " + NATIVE_PLATFORM);
            }
            l = factories.get(NATIVE_PLATFORM).create();
        }
        return l;
    }
}
