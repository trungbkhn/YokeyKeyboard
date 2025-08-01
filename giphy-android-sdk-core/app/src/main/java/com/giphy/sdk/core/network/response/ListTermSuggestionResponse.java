/*
 * Created by Bogdan Tirca on 4/21/17.
 * Copyright (c) 2017 Giphy Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.giphy.sdk.core.network.response;

import com.giphy.sdk.core.models.Meta;
import com.giphy.sdk.core.models.TermSuggestion;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListTermSuggestionResponse implements GenericResponse {
    private List<TermSuggestion> data;
    private Meta meta;

    public List<TermSuggestion> getData() {
        return data;
    }

    public void setData(List<TermSuggestion> data) {
        this.data = data;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
