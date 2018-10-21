/*
 * Copyright 2018 David Tainton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package assistant.travel.com.helpers;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */

public class DirectionParser {

    private String totalTime;
    private int totalTimeInSeconds;
    private String totalDistance;
    private int totalDistanceInMeters;
    private List<String> instructions;
    private List<String> distances;
    private List<LatLng> locations;

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(String totalDistance) {
        this.totalDistance = totalDistance;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public List<String> getDistances() {
        return distances;
    }

    public void setDistances(List<String> distances) {
        this.distances = distances;
    }

    public List<LatLng> getLocations() {
        return locations;
    }

    public void setLocations(List<LatLng> locations) {
        this.locations = locations;
    }

    public int getTotalTimeInSeconds() {
        return totalTimeInSeconds;
    }

    public void setTotalTimeInSeconds(int totalTimeInSeconds) {
        this.totalTimeInSeconds = totalTimeInSeconds;
    }

    public int getTotalDistanceInMeters() {
        return totalDistanceInMeters;
    }

    public void setTotalDistanceInMeters(int totalDistanceInMeters) {
        this.totalDistanceInMeters = totalDistanceInMeters;
    }


    // Receives a JSONObject and returns a list of lists containing latitude and longitude
    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

        instructions = new ArrayList<>();
        distances = new ArrayList<>();
        locations = new ArrayList<>();

        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            //Traversing all routes
            for (int i = 0; i < jRoutes.length(); i++) {

                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                //Traversing all legs
                for (int j = 0; j < jLegs.length(); j++) {

                    totalDistance = ((JSONObject)((JSONObject) jLegs.get(j)).get("distance")).get("text").toString();
                    totalDistanceInMeters = Integer.parseInt(((JSONObject)((JSONObject) jLegs.get(j)).get("distance")).get("value").toString());
                    totalTime = ((JSONObject)((JSONObject) jLegs.get(j)).get("duration")).get("text").toString();
                    totalTimeInSeconds = Integer.parseInt(((JSONObject)((JSONObject) jLegs.get(j)).get("duration")).get("value").toString());
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    // Traversing all steps
                    for (int k = 0; k < jSteps.length(); k++) {

                        instructions.add(((JSONObject) jSteps.get(k)).get("html_instructions").toString());
                        distances.add(((JSONObject) ((JSONObject) jSteps.get(k)).get("distance")).get("text").toString());

                        String lat = ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lat").toString();
                        String lng = ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lng").toString();
                        locations.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));

                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List list = decodePoly(polyline);

                        // Traversing all points
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routes;
    }

    /**
     * Method to decode polyline points
     * Ref : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

}
