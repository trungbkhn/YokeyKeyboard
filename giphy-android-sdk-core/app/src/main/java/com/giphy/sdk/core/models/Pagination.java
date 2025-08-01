/*
 * Created by Bogdan Tirca on 5/4/17.
 * Copyright (c) 2017 Giphy Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.giphy.sdk.core.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Pagination implements Parcelable {
    @SerializedName("total_count")
    private int totalCount;
    private int count;
    private int offset;

    public Pagination() {}

    public Pagination(Parcel in) {
        totalCount = in.readInt();
        count = in.readInt();
        offset = in.readInt();
    }

    /**
     * @return total number of results
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * @return number of results in the current response
     */
    public int getCount() {
        return count;
    }

    /**
     * @return offset used for current response
     */
    public int getOffset() {
        return offset;
    }

    public static final Creator<Pagination> CREATOR = new Creator<Pagination>() {
        @Override
        public Pagination createFromParcel(Parcel in) {
            return new Pagination(in);
        }

        @Override
        public Pagination[] newArray(int size) {
            return new Pagination[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(totalCount);
        parcel.writeInt(count);
        parcel.writeInt(offset);
    }
}
