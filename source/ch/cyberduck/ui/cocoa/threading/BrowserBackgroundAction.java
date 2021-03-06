package ch.cyberduck.ui.cocoa.threading;

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

import ch.cyberduck.core.Session;
import ch.cyberduck.core.threading.BackgroundActionRegistry;
import ch.cyberduck.ui.cocoa.BrowserController;

/**
 * @version $Id$
 */
public abstract class BrowserBackgroundAction extends AlertRepeatableBackgroundAction {
    
    private BrowserController controller;

    public BrowserBackgroundAction(BrowserController controller) {
        super(controller);
        this.controller = controller;
    }

    public BrowserController getController() {
        return controller;
    }

    @Override
    public Session getSession() {
        return controller.getSession();
    }

    @Override
    public boolean prepare() {
        controller.invoke(new WindowMainAction(controller) {
            public void run() {
                controller.getStatusSpinner().startAnimation(null);
                controller.updateStatusLabel(BrowserBackgroundAction.this.getActivity());
            }
        });
        return super.prepare();
    }

    @Override
    public void cancel() {
        if(this.isRunning()) {
            this.getSession().interrupt();
        }
        super.cancel();
    }

    @Override
    public void finish() {
        super.finish();
        controller.invoke(new WindowMainAction(controller) {
            public void run() {
                controller.getStatusSpinner().stopAnimation(null);
                controller.updateStatusLabel();
            }
        });
    }

    @Override
    public boolean isCanceled() {
        if(null == this.getSession()) {
            return true;
        }
        return super.isCanceled();
    }
}