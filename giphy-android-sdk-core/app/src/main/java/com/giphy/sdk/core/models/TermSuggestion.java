/*
 * Created by Bogdan Tirca on 5/8/17.
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

public class TermSuggestion implements Parcelable {
    @SerializedName("name")
    private String term;

    public TermSuggestion() {}

    public TermSuggestion(Parcel in) {
        term = in.readString();
    }

    /**
     * @return term suggestion
     */
    public String getTerm() {
        return term;
    }

    public static final Creator<TermSuggestion> CREATOR = new Creator<TermSuggestion>() {
        @Override
        public TermSuggestion createFromParcel(Parcel in) {
            return new TermSuggestion(in);
        }

        @Override
        public TermSuggestion[] newArray(int size) {
            return new TermSuggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(term);
    }
}
