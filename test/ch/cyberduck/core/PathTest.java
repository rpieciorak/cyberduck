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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

public class PathTest extends TestCase {
    public PathTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNormalize() throws Exception {
        Path path = PathFactory.createPath(SessionFactory.createSession(new Host("localhost")));
        path.setPath("/path/to/remove/..");
        assertEquals("/path/to", path.getAbsolute());
        path.setPath("/path/to/remove/.././");
        assertEquals( "/path/to", path.getAbsolute());
        path.setPath("/path/remove/../to/remove/.././");
        assertEquals( "/path/to", path.getAbsolute());
//        path.setPath("../path/to");
//        assertEquals( "/path/to", path.getAbsolute());
//        path.setPath("/../path/to");
//        assertEquals( "/path/to", path.getAbsolute());
        path.setPath("/path/to/remove/remove/../../");
        assertEquals( "/path/to", path.getAbsolute());
        path.setPath("/path/././././to");
        assertEquals( "/path/to", path.getAbsolute());
        path.setPath("./.path/to");
        assertEquals( "/.path/to", path.getAbsolute());
        path.setPath(".path/to");
        assertEquals( "/.path/to", path.getAbsolute());
        path.setPath("/path/.to");
        assertEquals( "/path/.to", path.getAbsolute());
        path.setPath("/path//to");
        assertEquals( "/path/to", path.getAbsolute());
        path.setPath("/path///to////");
        assertEquals( "/path/to", path.getAbsolute());
    }

    public void test1067() throws Exception {
        Path path = PathFactory.createPath(SessionFactory.createSession(new Host("localhost")));
        path.setPath("\\\\directory");
        assertEquals("\\\\directory", path.getAbsolute());
        assertEquals("/", path.getParent().getAbsolute());
    }

    public static Test suite() {
        return new TestSuite(PathTest.class);
    }
}
