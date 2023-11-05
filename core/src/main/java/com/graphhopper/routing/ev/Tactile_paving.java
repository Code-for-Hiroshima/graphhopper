/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.graphhopper.routing.ev;

import com.graphhopper.util.Helper;

public enum Tactile_paving {
    MISSING, SIDEWALK, CROSSING, ACCESS_AISLE, LINK, TRAFFIC_ISLAND, ALLEY;

    public static final String KEY = "tactile_paving";

    public static EnumEncodedValue<Tactile_paving> create() {
        return new EnumEncodedValue<>(KEY, Tactile_paving.class);
    }

    @Override
    public String toString() {
        return Helper.toLowerCase(super.toString());
    }

    public static Tactile_paving find(String name) {
        if (name == null || name.isEmpty())
            return MISSING;

        try {
            return Tactile_paving.valueOf(Helper.toUpperCase(name));
        } catch (IllegalArgumentException ex) {
            return MISSING;
        }
    }
}
