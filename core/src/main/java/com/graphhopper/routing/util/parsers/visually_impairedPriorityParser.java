package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PMap;

import java.util.TreeMap;

import static com.graphhopper.routing.util.PriorityCode.AVOID;
import static com.graphhopper.routing.util.PriorityCode.VERY_NICE;

public class visually_impairedPriorityParser extends FootPriorityParser {

    public visually_impairedPriorityParser(EncodedValueLookup lookup, PMap properties) {
        this(lookup.getDecimalEncodedValue(properties.getString("name", VehiclePriority.key("visually_impaired"))),
                lookup.getEnumEncodedValue(FootNetwork.KEY, RouteNetwork.class));
    }

    protected visually_impairedPriorityParser(DecimalEncodedValue priorityEnc, EnumEncodedValue<RouteNetwork> footRouteEnc) {
        super(priorityEnc, footRouteEnc);

        safeHighwayTags.add("footway");
        safeHighwayTags.add("pedestrian");
        safeHighwayTags.add("living_street");
        safeHighwayTags.add("residential");
        safeHighwayTags.add("service");
        safeHighwayTags.add("platform");

        safeHighwayTags.remove("steps");
        safeHighwayTags.remove("track");
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        Integer priorityFromRelation = routeMap.get(footRouteEnc.getEnum(false, edgeId, edgeIntAccess));
        priorityWayEncoder.setDecimal(false, edgeId, edgeIntAccess, PriorityCode.getValue(handlePriority(way, priorityFromRelation)));
    }

    /**
     * First get priority from {@link FootPriorityParser#handlePriority(ReaderWay, Integer)} then evaluate visually_impaired specific
     * tags.
     *
     * @return a priority for the given way
     */
    @Override
    public int handlePriority(ReaderWay way, Integer priorityFromRelation) {
        TreeMap<Double, Integer> weightToPrioMap = new TreeMap<>();

        weightToPrioMap.put(100d, super.handlePriority(way, priorityFromRelation));

        if (way.hasTag("visually_impaired", "designated")) {
            weightToPrioMap.put(102d, VERY_NICE.getValue());
        } else if (way.hasTag("visually_impaired", "limited")) {
            weightToPrioMap.put(102d, AVOID.getValue());
        }

        return weightToPrioMap.lastEntry().getValue();
    }
}
