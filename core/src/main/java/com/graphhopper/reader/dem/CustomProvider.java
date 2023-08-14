package com.graphhopper.reader.dem;

import java.io.*;

public class CustomProvider extends AbstractCUSTOMElevationProvider {
    private static double ret=0.0;
    private static int count=0;
    public CustomProvider(String dir) {
        super("", dir, "", Integer.MIN_VALUE, Integer.MAX_VALUE, 3601);
    }

    @Override
    public double getEle(double lat, double lon) {
        try{
            ret = Elevation.getOnlyElevation(lat, lon).doubleValue();
        }catch (final IOException e){
            return Double.NaN;
        }
        count+=1;
        System.out.println(String.format("[%d] %f %f %.1f",count,lat,lon,ret));
        return ret;//Double.NaN;
    }

    @Override
    byte[] readFile(File file) throws IOException {
        return null;//os.toByteArray();
    }

    @Override
    String getFileName(double lat, double lon) {
        int latInt = (int) Math.floor(lat);
        int lonInt = (int) Math.floor(lon);
        return cacheDir + "/" + (lat > 0 ? "N" : "S") + getPaddedLatString(latInt) + (lon > 0 ? "E" : "W") + getPaddedLonString(lonInt) + ".hgt.zip";
    }

    @Override
    String getDownloadURL(double lat, double lon) {
        return getFileName(lat, lon);
    }

}
