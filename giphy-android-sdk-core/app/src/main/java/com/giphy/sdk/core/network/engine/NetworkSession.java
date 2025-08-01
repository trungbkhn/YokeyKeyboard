/*
 * Created by Bogdan Tirca on 4/19/17.
 * Copyright (c) 2017 Giphy Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.giphy.sdk.core.network.engine;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.giphy.sdk.core.network.api.CompletionHandler;
import com.giphy.sdk.core.network.response.GenericResponse;
import com.giphy.sdk.core.threading.ApiTask;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * A generic interface that describes all the params of a low level GET request.
 */
public interface NetworkSession {
    <T extends GenericResponse> ApiTask<T> queryStringConnection(@NonNull final Uri serverUrl,
                                                                @NonNull final String path,
                                                                @NonNull final String method,
                                                                @NonNull final Class<T> responseClass,
                                                                @Nullable final Map<String, String> queryStrings,
                                                                @Nullable final Map<String, String> headers);
}
