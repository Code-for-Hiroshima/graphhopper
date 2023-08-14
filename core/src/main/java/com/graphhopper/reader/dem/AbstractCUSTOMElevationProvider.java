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
package com.graphhopper.reader.dem;

import com.graphhopper.coll.GHIntObjectHashMap;
//import com.graphhopper.storage.DataAccess;
//import com.graphhopper.util.Downloader;
//import com.graphhopper.util.Helper;

import java.io.File;
//import java.io.FileNotFoundException;
import java.io.IOException;
//import java.math.BigDecimal;
//import java.net.SocketTimeoutException;

/**
 * Common functionality used when working with SRTM hgt data.
 *
 * @author Robin Boldt
 */
public abstract class AbstractCUSTOMElevationProvider extends TileBasedElevationProvider {
    // use a map as an array is not quite useful if we want to hold only parts of the world
    private final GHIntObjectHashMap<HeightTile> cacheData = new GHIntObjectHashMap<>();
    private final double precision = 1e7;
    private final double invPrecision = 1 / precision;

    public AbstractCUSTOMElevationProvider(String baseUrl, String cacheDir, String downloaderName, int minLat, int maxLat, int defaultWidth) {
        super(cacheDir);
        this.baseUrl = baseUrl;
    }

    @Override
    public void release() {
        cacheData.clear();
        if (dir != null) {
            // for memory mapped type we remove temporary files
            if (autoRemoveTemporary)
                dir.clear();
            else
                dir.close();
        }
    }

    int down(double val) {
        int intVal = (int) val;
        if (val >= 0 || intVal - val < invPrecision)
            return intVal;
        return intVal - 1;
    }

    @Override
    public double getEle(double lat, double lon) {
        // Return fast, if there is no data available
        // See https://www2.jpl.nasa.gov/srtm/faq.html
        return 0.0;
    }

 
    protected String getPaddedLonString(int lonInt) {
        lonInt = Math.abs(lonInt);
        String lonString = lonInt < 100 ? "0" : "";
        if (lonInt < 10)
            lonString += "0";
        lonString += lonInt;
        return lonString;
    }

    protected String getPaddedLatString(int latInt) {
        latInt = Math.abs(latInt);
        String latString = latInt < 10 ? "0" : "";
        latString += latInt;
        return latString;
    }

    abstract byte[] readFile(File file) throws IOException;

    /**
     * Return the local file name without file ending, has to be lower case, because DataAccess only supports lower case names.
     */
    abstract String getFileName(double lat, double lon);

    /**
     * Returns the complete URL to download the file
     */
    abstract String getDownloadURL(double lat, double lon);

}
