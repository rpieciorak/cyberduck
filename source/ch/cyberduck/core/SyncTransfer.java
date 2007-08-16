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

import ch.cyberduck.core.io.BandwidthThrottle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.util.*;

/**
 * @version $Id$
 */
public class SyncTransfer extends Transfer {

    public SyncTransfer(Path root) {
        super(root);
    }

    public SyncTransfer(Collection roots) {
        super(roots);
    }

    public SyncTransfer(NSDictionary dict, Session s) {
        super(dict, s);
    }

    public NSMutableDictionary getAsDictionary() {
        NSMutableDictionary dict = super.getAsDictionary();
        dict.setObjectForKey(String.valueOf(TransferFactory.KIND_SYNC), "Kind");
        return dict;
    }

    /**
     * The delegate for files to upload
     */
    private Transfer _delegateUpload;

    /**
     * The delegate for files to download
     */
    private Transfer _delegateDownload;

    protected void init() {
        log.debug("init");
        _delegateUpload = new UploadTransfer(this.roots);
        _delegateDownload = new DownloadTransfer(this.roots);
    }

    public void setBandwidth(float bytesPerSecond) {
        ;
    }

    public float getBandwidth() {
        return BandwidthThrottle.UNLIMITED;
    }

    public String getName() {
        return this.getRoot().getName() + " \u2194 " /*left-right arrow*/ + this.getRoot().getLocal().getName();
    }

    public double getSize() {
        final double size = _delegateDownload.getSize() + _delegateUpload.getSize();
        if (0 == size) {
            return super.getSize();
        }
        return size;
    }

    public double getTransferred() {
        final double transferred = _delegateDownload.getTransferred() + _delegateUpload.getTransferred();
        if (0 == transferred) {
            return super.getTransferred();
        }
        return transferred;
    }

    private Action action = Action.forString(
            Preferences.instance().getProperty("queue.sync.action.default")
    );

    /**
     * @param action
     */
    public void setAction(Action action) {
        this.action = action;
        this._comparisons.clear();
    }

    /**
     * @return
     */
    public Action getAction() {
        return this.action;
    }

    /**
     *
     */
    public static class Action {
        public boolean equals(Object other) {
            if (null == other) {
                return false;
            }
            return this == other;
        }

        public static Action forString(String s) {
            if(s.equals(ACTION_DOWNLOAD.toString())) {
                return ACTION_DOWNLOAD;
            }
            if(s.equals(ACTION_UPLOAD.toString())) {
                return ACTION_UPLOAD;
            }
            return ACTION_MIRROR;
        }
    }

    public static final Action ACTION_DOWNLOAD = new Action() {
        public String toString() {
            return "download";
        }
    };
    public static final Action ACTION_UPLOAD = new Action() {
        public String toString() {
            return "upload";
        }
    };
    public static final Action ACTION_MIRROR = new Action() {
        public String toString() {
            return "mirror";
        }
    };

    public TransferFilter filter(final TransferAction action) {
        log.debug("filter:" + action);
        if (action.equals(TransferAction.ACTION_OVERWRITE)) {
            // When synchronizing, either cancel or overwrite. Resume is not supported
            return new TransferFilter() {
                /**
                 * Download delegate filter
                 */
                private TransferFilter _delegateFilterDownload
                        = _delegateDownload.filter(TransferAction.ACTION_OVERWRITE);

                /**
                 * Upload delegate filter
                 */
                private TransferFilter _delegateFilterUpload
                        = _delegateUpload.filter(TransferAction.ACTION_OVERWRITE);

                public void prepare(Path p) {
                    final Comparison compare = SyncTransfer.this.compare(p);
                    if (compare.equals(COMPARISON_REMOTE_NEWER)) {
                        _delegateFilterDownload.prepare(p);
                    } else if (compare.equals(COMPARISON_LOCAL_NEWER)) {
                        _delegateFilterUpload.prepare(p);
                    }
                }

                public boolean accept(AbstractPath p) {
                    final Comparison compare = SyncTransfer.this.compare((Path) p);
                    if (!COMPARISON_EQUAL.equals(compare)) {
                        if (compare.equals(COMPARISON_REMOTE_NEWER)) {
                            if (SyncTransfer.this.action.equals(ACTION_UPLOAD)) {
                                log.info("Skipping " + p);
                                return false;
                            }
                            // Ask the download delegate for inclusion
                            return _delegateFilterDownload.accept(p);
                        }
                        else if (compare.equals(COMPARISON_LOCAL_NEWER)) {
                            if (SyncTransfer.this.action.equals(ACTION_DOWNLOAD)) {
                                log.info("Skipping " + p);
                                return false;
                            }
                            // Ask the upload delegate for inclusion
                            return _delegateFilterUpload.accept(p);
                        }
                    }
                    return false;
                }
            };
        }
        if (action.equals(TransferAction.ACTION_CALLBACK)) {
            TransferAction result = prompt.prompt(this);
            return this.filter(result); //break out of loop
        }
        return super.filter(action);
    }

    private final Cache _cache = new Cache();

    public AttributedList childs(final Path parent) {
        if (!_cache.containsKey(parent)) {
            Set childs = new HashSet();
            childs.addAll(_delegateDownload.childs(parent));
            childs.addAll(_delegateUpload.childs(parent));
            _cache.put(parent, new AttributedList(childs));
        }
        return _cache.get(parent);
    }

    public boolean isCached(Path file) {
        return _cache.containsKey(file);
    }

    public boolean isSelectable(Path item) {
        return !this.compare(item).equals(COMPARISON_EQUAL);
    }

    public TransferAction action(final boolean resumeRequested, final boolean reloadRequested) {
        log.debug("action:" + resumeRequested + "," + reloadRequested);
        // Always prompt for synchronization
        return TransferAction.ACTION_CALLBACK;
    }

    protected void _transferImpl(final Path p) {
        final Comparison compare = this.compare(p);
        if (compare.equals(COMPARISON_REMOTE_NEWER)) {
            _delegateDownload._transferImpl(p);
        } else if (compare.equals(COMPARISON_LOCAL_NEWER)) {
            _delegateUpload._transferImpl(p);
        }
    }

    protected void clear() {
        _comparisons.clear();

        _delegateDownload.clear();
        _delegateUpload.clear();

        _cache.clear();

        super.clear();
    }

    protected void reset() {
        _delegateDownload.reset();
        _delegateUpload.reset();

        super.reset();
    }

    private final Map _comparisons = new HashMap();

    /**
     *
     */
    public static class Comparison {
        public boolean equals(Object other) {
            if (null == other) {
                return false;
            }
            return this == other;
        }
    }

    /**
     * Remote file is newer or local file does not exist
     */
    public static final Comparison COMPARISON_REMOTE_NEWER = new Comparison();
    /**
     * Local file is newer or remote file does not exist
     */
    public static final Comparison COMPARISON_LOCAL_NEWER = new Comparison();
    /**
     * Files are identical or directories
     */
    public static final Comparison COMPARISON_EQUAL = new Comparison();
    /**
     * Files differ in size
     */
    public static final Comparison COMPARISON_UNEQUAL = new Comparison();

    /**
     * @param p The path to compare
     * @return COMPARISON_REMOTE_NEWER, COMPARISON_LOCAL_NEWER or COMPARISON_EQUAL
     */
    public Comparison compare(Path p) {
        if (!_comparisons.containsKey(p)) {
            log.debug("compare:" + p);
            Comparison result = null;
            if (SyncTransfer.this.exists(p) && SyncTransfer.this.exists(p.getLocal())) {
                if (p.attributes.isDirectory()) {
                    result = COMPARISON_EQUAL;
                }
                if (p.attributes.isFile()) {
                    result = this.compareSize(p);
                    if (result.equals(COMPARISON_UNEQUAL)) {
                        if (Preferences.instance().getBoolean("queue.sync.timestamp.ignore")) {
                            if(this.getAction().equals(ACTION_DOWNLOAD)) {
                                result = COMPARISON_REMOTE_NEWER;
                            }
                            else if(this.getAction().equals(ACTION_UPLOAD)) {
                                result = COMPARISON_LOCAL_NEWER;
                            }
                            else if(this.getAction().equals(ACTION_MIRROR)) {
                                result = this.compareTimestamp(p);
                            }
                        }
                        else {
                            //both files have a different size; compare using timestamp
                            result = this.compareTimestamp(p);
                        }
                    }
                }
            } else if (SyncTransfer.this.exists(p)) {
                // only the remote file exists
                result = COMPARISON_REMOTE_NEWER;
            } else if (SyncTransfer.this.exists(p.getLocal())) {
                // only the local file exists
                result = COMPARISON_LOCAL_NEWER;
            } else {
                // both files don't exist yet
                result = COMPARISON_EQUAL;
            }
            _comparisons.put(p, result);
        }
        return (Comparison) _comparisons.get(p);
    }

    /**
     * @param p
     * @return
     */
    private Comparison compareSize(Path p) {
        log.debug("compareSize:" + p);
        if (p.attributes.getSize() == -1) {
            p.readSize();
        }
        //fist make sure both files are larger than 0 bytes
        if (p.attributes.getSize() == 0 && p.getLocal().attributes.getSize() == 0) {
            return COMPARISON_EQUAL;
        }
        if (p.attributes.getSize() == 0) {
            return COMPARISON_LOCAL_NEWER;
        }
        if (p.getLocal().attributes.getSize() == 0) {
            return COMPARISON_REMOTE_NEWER;
        }
        //different file size - further comparison check
        return COMPARISON_UNEQUAL;
    }

    /**
     * @param p
     * @return
     */
    private Comparison compareTimestamp(Path p) {
        log.debug("compareTimestamp:" + p);
        if (p.attributes.getModificationDate() == -1) {
            p.readTimestamp();
        }
        Calendar remote = this.asCalendar(
                p.attributes.getModificationDate(),
//                        -this.getHost().getTimezone().getRawOffset()
                p.getHost().getTimezone(),
                Calendar.SECOND);
        Calendar local = this.asCalendar(p.getLocal().attributes.getModificationDate(),
                TimeZone.getDefault(),
                Calendar.SECOND);
        if (local.before(remote)) {
            return COMPARISON_REMOTE_NEWER;
        }
        if (local.after(remote)) {
            return COMPARISON_LOCAL_NEWER;
        }
        //same timestamp
        return COMPARISON_EQUAL;
    }

    /**
     * @param timestamp
     * @param timezone
     * @param precision
     * @return
     */
    private Calendar asCalendar(final long timestamp, final TimeZone timezone, final int precision) {
        log.debug("asCalendar:" + timestamp);
        Calendar c = Calendar.getInstance(timezone);
        c.setTimeInMillis(timestamp);
        if (precision == Calendar.MILLISECOND) {
            return c;
        }
        c.clear(Calendar.MILLISECOND);
        if (precision == Calendar.SECOND) {
            return c;
        }
        c.clear(Calendar.SECOND);
        if (precision == Calendar.MINUTE) {
            return c;
        }
        c.clear(Calendar.MINUTE);
        if (precision == Calendar.HOUR) {
            return c;
        }
        c.clear(Calendar.HOUR);
        return c;
    }
}