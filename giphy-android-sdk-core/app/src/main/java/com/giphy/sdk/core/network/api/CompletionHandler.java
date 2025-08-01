/*
 * Created by Bogdan Tirca on 4/20/17.
 * Copyright (c) 2017 Giphy Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.giphy.sdk.core.network.api;

/**
 * Completion handler callback. It's the main interface for getting the results or errors from
 * the network requests
 * @param <T>
 */
public interface CompletionHandler <T> {
    public void onComplete(T result, Throwable e);
}
