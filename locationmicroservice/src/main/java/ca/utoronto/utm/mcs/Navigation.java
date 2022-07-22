package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

public class Navigation extends Endpoint {
    
    /**
     * GET /location/navigation/:driverUid?passengerUid=:passengerUid
     * @param driverUid, passengerUid
     * @return 200, 400, 404, 500
     * Get the shortest path from a driver to passenger weighted by the
     * travel_time attribute on the ROUTE_TO relationship.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // check if request url isn't malformed
        String[] splitUrl = r.getRequestURI().getPath().split("/");
        String query = r.getRequestURI().getQuery();
        if (query == null) {
            this.sendStatus(r, 400);
            return;
        }
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            try {
                String name = param.split("=")[0];
                String value = param.split("=")[1];
                map.put(name, value);
            } catch (Exception e) {
                this.sendStatus(r, 400);
                return;
            }
        }
        if (splitUrl.length != 4 || params.length != 1 || !map.containsKey("passengerUid")) {
            this.sendStatus(r, 400);
            return;
        }

        // check if uid and radius is integer, return 400 if not
        String driverUid = splitUrl[3];
        String passengerUid;
        try {
            passengerUid = map.get("passengerUid");
        } catch (Exception e){
            e.printStackTrace();
            this.sendStatus(r, 400);
            return;
        }

        try {
            Result result1 = this.dao.getUserLocationByUid(driverUid);
            Result result2 = this.dao.getUserLocationByUid(passengerUid);
            String driverStreet = null;
            for (Record rec: result1.list()){
                driverStreet = rec.get("n.street").toString();
            }
            String passengerStreet = null;
            for (Record rec: result2.list()){
                passengerStreet = rec.get("n.street").toString();
            }
            System.out.println(passengerStreet);
            System.out.println(driverStreet);
            if (passengerStreet == null || driverStreet == null) {
                this.sendStatus(r, 404);
                return;
            }

            Result result = this.dao.getNavigationPath(driverStreet, passengerStreet);
            if (!result.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }

            JSONObject resp = new JSONObject();
            JSONObject data = new JSONObject();
            for (Record rec: result.list()) {
                System.out.println(rec);
                data.put("total_time", rec.get("totalCost"));
                List<Object> roadNames = rec.get("roadNames").asList();
                List<Object> roadTrafficsObject = rec.get("roadTraffics").asList();
                List<Boolean> roadTraffics = roadTrafficsObject.stream().map(traffic -> Boolean.valueOf(traffic.toString())).toList();
                List<Object> travelTimesObject = rec.get("costs").asList();
                List<Integer> travelTimes = travelTimesObject.stream().map(time -> (int)Double.parseDouble(time.toString())).toList();
                JSONObject[] routes = new JSONObject[roadNames.size()];
                for (int i=0; i<roadNames.size(); i++){
                    JSONObject route = new JSONObject();
                    route.put("street", roadNames.get(i));
                    route.put("has_traffic", roadTraffics.get(i));
                    route.put("time", travelTimes.get(i));
                    routes[i] = route;
                }
                data.put("route", routes);
            }
            resp.put("data", data);
            this.sendResponse(r, resp, 200);
        } catch(Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }
    }
}
