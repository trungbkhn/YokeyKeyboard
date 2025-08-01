/*
 * Created by Bogdan Tirca on 4/25/17.
 * Copyright (c) 2017 Giphy Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.giphy.sdk.core.network.response;

import com.giphy.sdk.core.models.Category;
import com.giphy.sdk.core.models.Meta;
import com.giphy.sdk.core.models.Pagination;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListCategoryResponse implements GenericResponse {
    private List<Category> data;

    public Pagination pagination;
    public Meta meta;

    public List<Category> getData() {
        return data;
    }

    public void setData(List<Category> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
