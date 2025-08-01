/*
 * Created by Bogdan Tirca on 10/26/17.
 * Copyright © 2017 Giphy. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.giphy.sdk.core.threading;


import android.os.Handler;
import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * Adapts an Android {@link android.os.Handler Handler} into a JRE {@link java.util.concurrent.Executor Executor}.
 * Runnables will be posted asynchronously.
 */
public class HandlerExecutor implements Executor {
    /** Handler wrapped by this executor. */
    private Handler handler;

    /**
     * Construct a new executor wrapping the specified handler.
     *
     * @param handler Handler to wrap.
     */
    public HandlerExecutor(@NonNull Handler handler) {
        this.handler = handler;
    }

    /**
     * Execute a command, by posting it to the underlying handler.
     *
     * @param command Command to execute.
     */
    public void execute(Runnable command) {
        handler.post(command);
    }
}