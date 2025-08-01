/*
 * Created by Bogdan Tirca on 4/24/17.
 * Copyright (c) 2017 Giphy Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.giphy.sdk.core.network.response;

import com.giphy.sdk.core.models.Meta;
import com.giphy.sdk.core.models.RandomGif;

public class RandomGifResponse implements GenericResponse {
    private RandomGif data;
    private Meta meta;

    public MediaResponse toGifResponse() {
        final MediaResponse mediaResponse = new MediaResponse();
        mediaResponse.setData(data.toGif());
        mediaResponse.setMeta(meta);
        return mediaResponse;
    }

    public RandomGif getData() {
        return data;
    }

    public void setData(RandomGif data) {
        this.data = data;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
