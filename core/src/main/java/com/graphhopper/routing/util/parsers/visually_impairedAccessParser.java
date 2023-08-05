package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.VehicleAccess;
import com.graphhopper.routing.util.WayAccess;
import com.graphhopper.util.PMap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class visually_impairedAccessParser extends FootAccessParser {
    private final Set<String> excludeSurfaces = new HashSet<>();
    private final Set<String> excludeSmoothness = new HashSet<>();
    private final int maxInclinePercent = 6;
    private boolean traffic_signals = false;    

    public visually_impairedAccessParser(EncodedValueLookup lookup, PMap properties) {
        this(lookup.getBooleanEncodedValue(properties.getString("name", VehicleAccess.key("visually_impaired"))));
        blockPrivate(properties.getBool("block_private", true));
        blockFords(properties.getBool("block_fords", false));
    }

    protected visually_impairedAccessParser(BooleanEncodedValue accessEnc) {
        super(accessEnc);

        restrictions.add("visually_impaired");

        barriers.add("handrail");
        barriers.add("wall");
        barriers.add("turnstile");
        barriers.add("kissing_gate");
        barriers.add("stile");

        allowedHighwayTags.remove("steps");
        allowedHighwayTags.remove("track");

        excludeSurfaces.add("cobblestone");
        excludeSurfaces.add("gravel");
        excludeSurfaces.add("sand");

        excludeSmoothness.add("bad");
        excludeSmoothness.add("very_bad");
        excludeSmoothness.add("horrible");
        excludeSmoothness.add("very_horrible");
        excludeSmoothness.add("impassable");

        allowedSacScale.clear();
    }

    /**
     * Avoid some more ways than for pedestrian like hiking trails.
     */
    @Override
    public WayAccess getAccess(ReaderWay way) {
        if (way.hasTag("surface", excludeSurfaces)) {
            if (!way.hasTag("sidewalk", sidewalkValues)) {
                return WayAccess.CAN_SKIP;
            } else {
                String sidewalk = way.getTag("sidewalk");
                if (way.hasTag("sidewalk:" + sidewalk + ":surface", excludeSurfaces)) {
                    return WayAccess.CAN_SKIP;
                }
            }
        }

        if (way.hasTag("smoothness", excludeSmoothness)) {
            if (!way.hasTag("sidewalk", sidewalkValues)) {
                return WayAccess.CAN_SKIP;
            } else {
                String sidewalk = way.getTag("sidewalk");
                if (way.hasTag("sidewalk:" + sidewalk + ":smoothness", excludeSmoothness)) {
                    return WayAccess.CAN_SKIP;
                }
            }
        }

        if (way.hasTag("incline")) {
            String tagValue = way.getTag("incline");
            if (tagValue.endsWith("%") || tagValue.endsWith("°")) {
                try {
                    double incline = Double.parseDouble(tagValue.substring(0, tagValue.length() - 1));
                    if (tagValue.endsWith("°")) {
                        incline = Math.tan(incline * Math.PI / 180) * 100;
                    }

                    if (-maxInclinePercent > incline || incline > maxInclinePercent) {
                        return WayAccess.CAN_SKIP;
                    }
                } catch (NumberFormatException ex) {
                }
            }
        }

        if (way.hasTag("kerb", "raised"))
            return WayAccess.CAN_SKIP;

        if (way.hasTag("kerb")) {
            String tagValue = way.getTag("kerb");
            if (tagValue.endsWith("cm") || tagValue.endsWith("mm")) {
                try {
                    float kerbHeight = Float.parseFloat(tagValue.substring(0, tagValue.length() - 2));
                    if (tagValue.endsWith("mm")) {
                        kerbHeight /= 100;
                    }

                    int maxKerbHeightCm = 3;
                    if (kerbHeight > maxKerbHeightCm) {
                        return WayAccess.CAN_SKIP;
                    }
                } catch (NumberFormatException ex) {
                }
            }
        }

        traffic_signals=false;
        Arrays.asList(
            "sound",
            "vibration",
            "arrow",
            "minimap",
            "floor_vibration",
            "countdown",
            "floor_light"
        ).forEach(s -> {
            if(way.hasTag("traffic_signals:"+s,"yes"))
                traffic_signals=true;
        });

        Set<String> tacttile_paving = new HashSet<>();
        tacttile_paving.add("yes");
        tacttile_paving.add("contrasted");
        tacttile_paving.add("primitive");

        Set<String> footway = new HashSet<>();
        footway.add("crossing");

        if (!(traffic_signals || way.hasTag("tactile_paving",tacttile_paving) || way.hasTag("footway",footway)) )
            return WayAccess.CAN_SKIP;        
        return super.getAccess(way);
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way) {
        WayAccess access = getAccess(way);
        if (access.canSkip())
            return;

        accessEnc.setBool(false, edgeId, edgeIntAccess, true);
        accessEnc.setBool(true, edgeId, edgeIntAccess, true);
    }
}