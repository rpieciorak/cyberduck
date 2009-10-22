package ch.cyberduck.ui.cocoa.foundation;

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

import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;

/**
 * @version $Id$
 *          <p/>
 *          This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 *          a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 *          For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a>, <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public abstract class NSAppleScript extends NSObject {
    private static final _Class CLASS = Rococoa.createClass("NSAppleScript", _Class.class);

    public interface _Class extends ObjCClass {
        public abstract NSAppleScript alloc();
    }

    /**
     * Given a URL that locates a script, in either text or compiled form, initialize.  Return nil and a pointer to an error information dictionary if an error occurs.  This is a designated initializer for this class.<br>
     * Given a URL that locates a script, in either text or compiled form, initialize.  Return nil and a pointer to an error information dictionary if an error occurs.  This is a designated initializer for this class.<br>
     * Given a URL that locates a script, in either text or compiled form, initialize.  Return nil and a pointer to an error information dictionary if an error occurs.  This is a designated initializer for this class.<br>
     * Original signature : <code>-(id)initWithContentsOfURL:(NSURL*) error:(NSDictionary**)</code><br>
     * <i>native declaration : line 28</i>
     */
    public abstract NSAppleScript initWithContentsOfURL_error(NSURL url, com.sun.jna.ptr.ByReference errorInfo);

    /**
     * Factory method<br>
     *
     * @see #initWithContentsOfURL_error(com.sun.jna.Pointer, com.sun.jna.ptr.ByReference)
     */
    public static NSAppleScript createWithContentsOfURL_error(NSURL url, com.sun.jna.ptr.ByReference errorInfo) {
        return CLASS.alloc().initWithContentsOfURL_error(url, errorInfo);
    }

    /**
     * Given a string containing the AppleScript source code of a script, initialize.  Return nil if an error occurs.  This is also a designated initializer for this class.<br>
     * Original signature : <code>-(id)initWithSource:(NSString*)</code><br>
     * <i>native declaration : line 31</i>
     */
    public abstract NSAppleScript initWithSource(String source);

    /**
     * Factory method<br>
     *
     * @see #initWithSource(String)
     */
    public static NSAppleScript createWithSource(String source) {
        return CLASS.alloc().initWithSource(source);
    }

    /**
     * Return the source code of the script if it is available, nil otherwise.  It is possible for an NSAppleScript that has been instantiated with -initWithContentsOfURL:error: to be a script for which the source code is not available, but is nonetheless executable.<br>
     * Original signature : <code>-(NSString*)source</code><br>
     * <i>native declaration : line 34</i>
     */
    public abstract String source();

    /**
     * Return yes if the script is already compiled, no otherwise.<br>
     * Original signature : <code>-(BOOL)isCompiled</code><br>
     * <i>native declaration : line 37</i>
     */
    public abstract boolean isCompiled();

    /**
     * Compile the script, if it is not already compiled.  Return yes for success or if the script was already compiled, no and a pointer to an error information dictionary otherwise.<br>
     * Original signature : <code>-(BOOL)compileAndReturnError:(NSDictionary**)</code><br>
     * <i>native declaration : line 40</i>
     */
    public abstract boolean compileAndReturnError(com.sun.jna.ptr.ByReference errorInfo);

    /**
     * Execute the script, compiling it first if it is not already compiled.  Return the result of executing the script, or nil and a pointer to an error information dictionary for failure.<br>
     * Original signature : <code>-(NSAppleEventDescriptor*)executeAndReturnError:(NSDictionary**)</code><br>
     * <i>native declaration : line 43</i>
     */
    public abstract NSAppleEventDescriptor executeAndReturnError(com.sun.jna.ptr.ByReference errorInfo);

    /**
     * Execute an Apple event in the context of the script, compiling the script first if it is not already compiled.  Return the result of executing the event, or nil and a pointer to an error information dictionary for failure.<br>
     * Original signature : <code>-(NSAppleEventDescriptor*)executeAppleEvent:(NSAppleEventDescriptor*) error:(NSDictionary**)</code><br>
     * <i>native declaration : line 46</i>
     */
    public abstract NSAppleEventDescriptor executeAppleEvent_error(NSAppleEventDescriptor event, com.sun.jna.ptr.ByReference errorInfo);

    public static NSAppleScript alloc() {
        return CLASS.alloc();
	}
}
