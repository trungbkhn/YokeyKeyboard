/*
 * Created by Bogdan Tirca on 4/19/17.
 * Copyright (c) 2017 Giphy Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.giphy.sdk.core.network.response;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.giphy.sdk.core.models.Meta;

public class ErrorResponse implements GenericResponse {
    private final Meta meta;

    public ErrorResponse(@NonNull int serverStatus, @Nullable String message) {
        meta = new Meta(serverStatus, message);
    }

    public Meta getMeta() {
        return meta;
    }
}
