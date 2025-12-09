package org.codelikethewind;

import java.util.Random;

public class CallRecordGenerator {
    public static String getCallRecord(String cityCode, Boolean isDropped) {
        String cellId;

        String latitude;
        String longitude;

        if (cityCode.equalsIgnoreCase("CHI")){
            cellId = "CHI";
            latitude = "41.8781";
            longitude = "-87.6298";
        }
        else if (cityCode.equalsIgnoreCase("NYC")){
            cellId = "NYC";
            latitude = "40.7128";
            longitude = "-74.0060";
        }
        else{
            cellId = "ATX";
            latitude = "30.2672";
            longitude = "-97.7431";
        }

        Random rand = new Random();

        int signalStrengthInt = -110 + rand.nextInt(46); // -110 to -65
        String signalStrength = String.valueOf(signalStrengthInt);

        String callRecordJson = String.format(
                "{\"cell_id\": \"%s\", \"lat\": %s, \"lng\": %s, \"signal_strength\": %s, \"is_dropped\": %s}",
                cellId, latitude, longitude, signalStrength, isDropped.toString()
        );

        return callRecordJson;
    }


}
