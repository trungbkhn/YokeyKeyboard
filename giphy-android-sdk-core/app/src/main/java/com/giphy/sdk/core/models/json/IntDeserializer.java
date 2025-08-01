/*
 * Created by Bogdan Tirca on 5/8/17.
 * Copyright (c) 2017 Giphy Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.giphy.sdk.core.models.json;

import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;

public class IntDeserializer implements JsonDeserializer<Integer> {
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
        if(jsonPrimitive.isString()) {
            final String numberText = json.getAsString();
            if (TextUtils.isEmpty(numberText)) {
                return 0;
            }
            return Integer.parseInt(numberText);
        } else if (jsonPrimitive.isNumber()) {
            return json.getAsInt();
        }
        return 0;
    }
}