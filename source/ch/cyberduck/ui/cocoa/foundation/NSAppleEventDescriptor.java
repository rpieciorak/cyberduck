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
import org.rococoa.cocoa.foundation.NSInteger;

/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a>, <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public abstract class NSAppleEventDescriptor extends NSObject {
    private static final _Class CLASS = Rococoa.createClass("NSAppleEventDescriptor", _Class.class);

    public interface _Class extends ObjCClass {
        /**
         * Create an autoreleased NSAppleEventDescriptor whose AEDesc type is typeNull.<br>
         * Original signature : <code>+(NSAppleEventDescriptor*)nullDescriptor</code><br>
         * <i>native declaration : line 18</i>
         */
        public abstract NSAppleEventDescriptor nullDescriptor();
        /**
         * <i>native declaration : line 22</i><br>
         * Conversion Error : /// Original signature : <code>+(NSAppleEventDescriptor*)descriptorWithDescriptorType:() bytes:(const void*) length:(NSUInteger)</code><br>
         * + (NSAppleEventDescriptor*)descriptorWithDescriptorType:(null)descriptorType bytes:(const void*)bytes length:(NSUInteger)byteCount; (Argument descriptorType cannot be converted)
         */
        /**
         * <i>native declaration : line 24</i><br>
         * Conversion Error : /// Original signature : <code>+(NSAppleEventDescriptor*)descriptorWithDescriptorType:() data:(NSData*)</code><br>
         * + (NSAppleEventDescriptor*)descriptorWithDescriptorType:(null)descriptorType data:(NSData*)data; (Argument descriptorType cannot be converted)
         */
        /**
         * Original signature : <code>+(NSAppleEventDescriptor*)descriptorWithBoolean:(Boolean)</code><br>
         * <i>native declaration : line 28</i>
         */
        public abstract NSAppleEventDescriptor descriptorWithBoolean(boolean boolean_);
        /**
         * <i>native declaration : line 29</i><br>
         * Conversion Error : /// Original signature : <code>+(NSAppleEventDescriptor*)descriptorWithEnumCode:()</code><br>
         * + (NSAppleEventDescriptor*)descriptorWithEnumCode:(null)enumerator; (Argument enumerator cannot be converted)
         */
        /**
         * Original signature : <code>+(NSAppleEventDescriptor*)descriptorWithInt32:(SInt32)</code><br>
         * <i>native declaration : line 30</i>
         */
        public abstract NSAppleEventDescriptor descriptorWithInt32(int signedInt);
        /**
         * <i>native declaration : line 31</i><br>
         * Conversion Error : /// Original signature : <code>+(NSAppleEventDescriptor*)descriptorWithTypeCode:()</code><br>
         * + (NSAppleEventDescriptor*)descriptorWithTypeCode:(null)typeCode; (Argument typeCode cannot be converted)
         */
        /**
         * Original signature : <code>+(NSAppleEventDescriptor*)descriptorWithString:(NSString*)</code><br>
         * <i>native declaration : line 36</i>
         */
        public abstract NSAppleEventDescriptor descriptorWithString(String string);
        /**
         * <i>native declaration : line 40</i><br>
         * Conversion Error : /**<br>
         *  * Create and return an autoreleased event, list, or record NSAppleEventDescriptor, respectively.<br>
         *  * Original signature : <code>+(NSAppleEventDescriptor*)appleEventWithEventClass:() eventID:() targetDescriptor:(NSAppleEventDescriptor*) returnID:() transactionID:()</code><br>
         *  * /<br>
         * + (NSAppleEventDescriptor*)appleEventWithEventClass:(null)eventClass eventID:(null)eventID targetDescriptor:(NSAppleEventDescriptor*)targetDescriptor returnID:(null)returnID transactionID:(null)transactionID; (Argument eventClass cannot be converted)
         */
        /**
         * Original signature : <code>+(NSAppleEventDescriptor*)listDescriptor</code><br>
         * <i>native declaration : line 41</i>
         */
        public abstract NSAppleEventDescriptor listDescriptor();

        /**
         * Original signature : <code>+(NSAppleEventDescriptor*)recordDescriptor</code><br>
         * <i>native declaration : line 42</i>
         */
        public abstract NSAppleEventDescriptor recordDescriptor();

        public abstract NSAppleEventDescriptor alloc();
    }

    /**
     * Create an autoreleased NSAppleEventDescriptor whose AEDesc type is typeNull.<br>
     * Original signature : <code>+(NSAppleEventDescriptor*)nullDescriptor</code><br>
     * <i>native declaration : line 18</i>
     */
    public static NSAppleEventDescriptor nullDescriptor() {
        return CLASS.nullDescriptor();
    }

    /**
     * Original signature : <code>+(NSAppleEventDescriptor*)descriptorWithBoolean:(Boolean)</code><br>
     * <i>native declaration : line 28</i>
     */
    public static NSAppleEventDescriptor descriptorWithBoolean(boolean boolean_) {
        return CLASS.descriptorWithBoolean(boolean_);
    }

    /**
     * Original signature : <code>+(NSAppleEventDescriptor*)descriptorWithInt32:(SInt32)</code><br>
     * <i>native declaration : line 30</i>
     */
    public static NSAppleEventDescriptor descriptorWithInt32(int signedInt) {
        return CLASS.descriptorWithInt32(signedInt);
    }

    /**
     * Original signature : <code>+(NSAppleEventDescriptor*)descriptorWithString:(NSString*)</code><br>
     * <i>native declaration : line 36</i>
     */
    public static NSAppleEventDescriptor descriptorWithString(String string) {
        return CLASS.descriptorWithString(string);
    }

    /**
     * Original signature : <code>+(NSAppleEventDescriptor*)listDescriptor</code><br>
     * <i>native declaration : line 41</i>
     */
    public static NSAppleEventDescriptor listDescriptor() {
        return CLASS.listDescriptor();
    }

    /**
     * Original signature : <code>+(NSAppleEventDescriptor*)recordDescriptor</code><br>
     * <i>native declaration : line 42</i>
     */
    public static NSAppleEventDescriptor recordDescriptor() {
        return CLASS.recordDescriptor();
    }

    /**
     * Original signature : <code>-(id)initWithAEDescNoCopy:(const AEDesc*)</code><br>
     * <i>native declaration : line 46</i>
     */
    public abstract NSAppleEventDescriptor initWithAEDescNoCopy(com.sun.jna.Pointer aeDesc);

    /**
     * Factory method<br>
     *
     * @see #initWithAEDescNoCopy(com.sun.jna.Pointer)
     */
    public static NSAppleEventDescriptor createWithAEDescNoCopy(com.sun.jna.Pointer aeDesc) {
        return CLASS.alloc().initWithAEDescNoCopy(aeDesc);
    }
    /**
     * <i>native declaration : line 50</i><br>
     * Conversion Error : /**<br>
     *  * Other initializers.<br>
     *  * Original signature : <code>-(id)initWithDescriptorType:() bytes:(const void*) length:(NSUInteger)</code><br>
     *  * /<br>
     * - (id)initWithDescriptorType:(null)descriptorType bytes:(const void*)bytes length:(NSUInteger)byteCount; (Argument descriptorType cannot be converted)
     */
    /**
     * <i>native declaration : line 51</i><br>
     * Conversion Error : /// Original signature : <code>-(id)initWithDescriptorType:() data:(NSData*)</code><br>
     * - (id)initWithDescriptorType:(null)descriptorType data:(NSData*)data; (Argument descriptorType cannot be converted)
     */
    /**
     * <i>native declaration : line 52</i><br>
     * Conversion Error : /// Original signature : <code>-(id)initWithEventClass:() eventID:() targetDescriptor:(NSAppleEventDescriptor*) returnID:() transactionID:()</code><br>
     * - (id)initWithEventClass:(null)eventClass eventID:(null)eventID targetDescriptor:(NSAppleEventDescriptor*)targetDescriptor returnID:(null)returnID transactionID:(null)transactionID; (Argument eventClass cannot be converted)
     */
    /**
     * Original signature : <code>-(id)initListDescriptor</code><br>
     * <i>native declaration : line 53</i>
     */
    public abstract NSAppleEventDescriptor initListDescriptor();

    /**
     * Factory method<br>
     *
     * @see #initListDescriptor()
     */
    public static NSAppleEventDescriptor createListDescriptor() {
        return CLASS.alloc().initListDescriptor();
    }

    /**
     * Original signature : <code>-(id)initRecordDescriptor</code><br>
     * <i>native declaration : line 54</i>
     */
    public abstract NSAppleEventDescriptor initRecordDescriptor();

    /**
     * Factory method<br>
     *
     * @see #initRecordDescriptor()
     */
    public static NSAppleEventDescriptor createRecordDescriptor() {
        return CLASS.alloc().initRecordDescriptor();
    }

    /**
     * Original signature : <code>-(const AEDesc*)aeDesc</code><br>
     * <i>native declaration : line 58</i>
     */
    public abstract com.sun.jna.Pointer aeDesc();

    /**
     * Get the four-character type code or the data from a fully-initialized descriptor.<br>
     * Original signature : <code>-(id)descriptorType</code><br>
     * <i>native declaration : line 62</i>
     */
    public abstract NSObject descriptorType();

    /**
     * Original signature : <code>-(NSData*)data</code><br>
     * <i>native declaration : line 63</i>
     */
    public abstract com.sun.jna.Pointer data();

    /**
     * Original signature : <code>-(Boolean)booleanValue</code><br>
     * <i>native declaration : line 67</i>
     */
    public abstract boolean booleanValue();

    /**
     * Original signature : <code>-(id)enumCodeValue</code><br>
     * <i>native declaration : line 68</i>
     */
    public abstract NSObject enumCodeValue();

    /**
     * Original signature : <code>-(SInt32)int32Value</code><br>
     * <i>native declaration : line 69</i>
     */
    public abstract int int32Value();

    /**
     * Original signature : <code>-(id)typeCodeValue</code><br>
     * <i>native declaration : line 70</i>
     */
    public abstract NSObject typeCodeValue();

    /**
     * Original signature : <code>-(NSString*)stringValue</code><br>
     * <i>native declaration : line 75</i>
     */
    public abstract String stringValue();

    /**
     * Accessors for an event descriptor.<br>
     * Original signature : <code>-(id)eventClass</code><br>
     * <i>native declaration : line 79</i>
     */
    public abstract NSObject eventClass();

    /**
     * Original signature : <code>-(id)eventID</code><br>
     * <i>native declaration : line 80</i>
     */
    public abstract NSObject eventID();

    /**
     * Original signature : <code>-(id)returnID</code><br>
     * <i>native declaration : line 81</i>
     */
    public abstract NSObject returnID();

    /**
     * Original signature : <code>-(id)transactionID</code><br>
     * <i>native declaration : line 82</i>
     */
    public abstract NSObject transactionID();
    /**
     * <i>native declaration : line 85</i><br>
     * Conversion Error : /**<br>
     *  * Set, retrieve, or remove parameter descriptors inside an event descriptor.<br>
     *  * Original signature : <code>-(void)setParamDescriptor:(NSAppleEventDescriptor*) forKeyword:()</code><br>
     *  * /<br>
     * - (void)setParamDescriptor:(NSAppleEventDescriptor*)descriptor forKeyword:(null)keyword; (Argument keyword cannot be converted)
     */
    /**
     * <i>native declaration : line 86</i><br>
     * Conversion Error : /// Original signature : <code>-(NSAppleEventDescriptor*)paramDescriptorForKeyword:()</code><br>
     * - (NSAppleEventDescriptor*)paramDescriptorForKeyword:(null)keyword; (Argument keyword cannot be converted)
     */
    public abstract NSAppleEventDescriptor paramDescriptorForKeyword(int keyword);
    /**
     * <i>native declaration : line 87</i><br>
     * Conversion Error : /// Original signature : <code>-(void)removeParamDescriptorWithKeyword:()</code><br>
     * - (void)removeParamDescriptorWithKeyword:(null)keyword; (Argument keyword cannot be converted)
     */
    /**
     * <i>native declaration : line 90</i><br>
     * Conversion Error : /**<br>
     *  * Set or retrieve attribute descriptors inside an event descriptor.<br>
     *  * Original signature : <code>-(void)setAttributeDescriptor:(NSAppleEventDescriptor*) forKeyword:()</code><br>
     *  * /<br>
     * - (void)setAttributeDescriptor:(NSAppleEventDescriptor*)descriptor forKeyword:(null)keyword; (Argument keyword cannot be converted)
     */
    /**
     * <i>native declaration : line 91</i><br>
     * Conversion Error : /// Original signature : <code>-(NSAppleEventDescriptor*)attributeDescriptorForKeyword:()</code><br>
     * - (NSAppleEventDescriptor*)attributeDescriptorForKeyword:(null)keyword; (Argument keyword cannot be converted)
     */
    /**
     * Return the number of items inside a list or record descriptor.<br>
     * Original signature : <code>-(NSInteger)numberOfItems</code><br>
     * <i>native declaration : line 94</i>
     */
    public abstract NSInteger numberOfItems();

    /**
     * Set, retrieve, or remove indexed descriptors inside a list or record descriptor.<br>
     * Original signature : <code>-(void)insertDescriptor:(NSAppleEventDescriptor*) atIndex:(NSInteger)</code><br>
     * <i>native declaration : line 97</i>
     */
    public abstract void insertDescriptor_atIndex(NSAppleEventDescriptor descriptor, NSInteger index);

    /**
     * Original signature : <code>-(NSAppleEventDescriptor*)descriptorAtIndex:(NSInteger)</code><br>
     * <i>native declaration : line 98</i>
     */
    public abstract NSAppleEventDescriptor descriptorAtIndex(NSInteger index);

    /**
     * Original signature : <code>-(void)removeDescriptorAtIndex:(NSInteger)</code><br>
     * <i>native declaration : line 100</i>
     */
    public abstract void removeDescriptorAtIndex(NSInteger index);
    /**
     * <i>native declaration : line 106</i><br>
     * Conversion Error : /**<br>
     *  * Set, retrieve, or remove keyed descriptors inside a record descriptor.<br>
     *  * Original signature : <code>-(void)setDescriptor:(NSAppleEventDescriptor*) forKeyword:()</code><br>
     *  * /<br>
     * - (void)setDescriptor:(NSAppleEventDescriptor*)descriptor forKeyword:(null)keyword; (Argument keyword cannot be converted)
     */
    /**
     * <i>native declaration : line 107</i><br>
     * Conversion Error : /// Original signature : <code>-(NSAppleEventDescriptor*)descriptorForKeyword:()</code><br>
     * - (NSAppleEventDescriptor*)descriptorForKeyword:(null)keyword; (Argument keyword cannot be converted)
     */
    /**
     * <i>native declaration : line 108</i><br>
     * Conversion Error : /// Original signature : <code>-(void)removeDescriptorWithKeyword:()</code><br>
     * - (void)removeDescriptorWithKeyword:(null)keyword; (Argument keyword cannot be converted)
     */
    /**
     * Return the keyword associated with an indexed descriptor inside a record descriptor.<br>
     * Original signature : <code>-(id)keywordForDescriptorAtIndex:(NSInteger)</code><br>
     * <i>native declaration : line 111</i>
     */
    public abstract NSObject keywordForDescriptorAtIndex(NSInteger index);
}
