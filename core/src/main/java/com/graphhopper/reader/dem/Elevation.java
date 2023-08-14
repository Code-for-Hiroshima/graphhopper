package com.graphhopper.reader.dem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Elevation {

    private static final int CONST_NO_DATA = -1;
    private static final String BASE_URL = "https://cyberjapandata.gsi.go.jp/xyz/";
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * @param lat
     * @param lon
     * @return result
     * @throws IOException
     */
    public static BigDecimal getOnlyElevation(final double lat, final double lon) throws IOException {
        try {
            final Map<String, String> resultMap = Elevation.fetchElevation(lat, lon);
            final BigDecimal elevation = new BigDecimal(resultMap.get("elevation"));
            return elevation;
        } catch (final NumberFormatException e) {
            final BigDecimal elevation = new BigDecimal(CONST_NO_DATA);
            return elevation;
        }
    }

    /**
     * @param lat
     * @param lon
     * @return Map<String, String>
     * @throws IOException
     */
    public static Map<String, String> fetchElevation(final double lat, final double lon) throws IOException {

        final double lng_rad = Math.toRadians(lon);
        final double lat_rad = Math.toRadians(lat);
        final double R = 128 / Math.PI;
        final double X = R * (lng_rad + Math.PI);
        final double Y = -1 * R / 2 * Math.log((1 + Math.sin(lat_rad)) / (1 - Math.sin(lat_rad))) + 128;
        final Map<String, String> resultMap = new HashMap<>();

        double elevation = Elevation.getElevation(X, Y, 15, "dem5a", 1);
        if (elevation == Elevation.CONST_NO_DATA)
            elevation = Elevation.getElevation(X, Y, 15, "dem5b", 1);
        if (elevation == Elevation.CONST_NO_DATA)
            elevation = Elevation.getElevation(X, Y, 14, "dem", 0);
        if (elevation == Elevation.CONST_NO_DATA) {
            resultMap.put("elevation", "-----");
        } else {
            resultMap.put("elevation", String.format("%.2f", elevation));
        }
        return resultMap;
    }

    /**
     * @param X
     * @param Y
     * @param zoom
     * @param demSource
     * @param dataRound
     * @return result
     * @throws IOException
     */
    private static Double getElevation(
            final double X, final double Y, final int zoom,
            final String demSource, final int dataRound) throws IOException {

        final double PixelX = X * Math.pow(2, zoom);
        final int TileX = (int) (PixelX / 256);
        final double PixelY = Y * Math.pow(2, zoom);
        final int TileY = (int) (PixelY / 256);
        final int PixelXint = (int) PixelX;
        final int px = PixelXint % 256;
        final int PixelYint = (int) PixelY;
        final int py = PixelYint % 256;

        final String BaseDir = System.getenv("HOME"); 
        final String DirName = String.format("%s/%d/%d", demSource, zoom, TileX);
        final String FileName = String.format("%d.txt", TileY);
        final String sFileName = Elevation.BASE_URL + String.format("%s/%s", DirName, FileName);
        final Request request = new Request.Builder().url(sFileName).build();

        String[] lines;

        Path p2 = Paths.get(String.format("%s/tmp/%s/%s", BaseDir, DirName, FileName));
        if (Files.exists(p2)) {
            File f = new File(String.format("%s/tmp/%s/%s", BaseDir, DirName, FileName));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String buff = br.lines().collect(Collectors.joining(System.lineSeparator()));
            br.close();
            lines = buff.split("\n");
        } else {
            try (Response response = Elevation.client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return (double) Elevation.CONST_NO_DATA;
                }
                final ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    return (double) Elevation.CONST_NO_DATA;
                }
                lines = responseBody.string().split("\n");
            }
        }

        if (lines.length <= py) {
            return (double) Elevation.CONST_NO_DATA;
        }
        final String[] values = lines[py].split(",");
        if (values.length <= px) {
            return (double) Elevation.CONST_NO_DATA;
        }
        Elevation.fileClass(BaseDir, DirName, FileName, lines);
        final double elevation = Double.parseDouble(values[px]);
        return Math.round(elevation * Math.pow(10, dataRound)) / Math.pow(10, dataRound);

    }

    private static void fileClass(String BaseDir, String DirName, String FileName, String[] lines) throws IOException {
        Path p1 = Paths.get(String.format("%s/tmp/%s", BaseDir, DirName));
        if (Files.notExists(p1)) {
            Files.createDirectories(p1);
        }

        Path p2 = Paths.get(String.format("%s/tmp/%s/%s", BaseDir, DirName, FileName));
        if (Files.notExists(p2)) {
            FileWriter file = new FileWriter(String.format("%s/tmp/%s/%s", BaseDir, DirName, FileName));
            PrintWriter pw = new PrintWriter(new BufferedWriter(file));
            for (String string : lines) {
                pw.println(string);
            }
            pw.close();
            System.out.print(String.format("%s/tmp/%s/%s\n", BaseDir, DirName, FileName));
        }
    }

}
