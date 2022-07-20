package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

public class Nearby extends Endpoint {
    
    /**
     * GET /location/nearbyDriver/:uid?radius=:radius
     * @param uid, radius
     * @return 200, 400, 404, 500
     * Get drivers that are within a certain radius around a user.
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
        if (splitUrl.length != 4 || params.length != 1 || !map.containsKey("radius")) {
            this.sendStatus(r, 400);
            return;
        }

        // check if uid and radius is integer, return 400 if not
        String uidString = splitUrl[3];
        int radius;
        try {
            radius = Integer.parseInt(map.get("radius"));
        } catch (Exception e){
            e.printStackTrace();
            this.sendStatus(r, 400);
            return;
        }

        try {
            Result result = this.dao.getUserLocationByUid(uidString);
            Integer userLon = null, userLat = null;
            String street = null;
            for (Record rec: result.list()){
                userLon = Integer.parseInt(rec.get("n.longitude").toString());
                userLat = Integer.parseInt(rec.get("n.latitude").toString());
                street = rec.get("n.street").toString();
            }
            if (userLon == null || userLat == null || street == null) {
                this.sendStatus(r, 404);
                return;
            }
            System.out.println(userLon);
            System.out.println(userLat);
            System.out.println(street);

            result = this.dao.getAllDrivers();

            JSONObject resp = new JSONObject();
            JSONObject data = new JSONObject();
            for (Record rec : result.list()) {
                int driverLong = Integer.parseInt(rec.get("n.longitude").toString());
                int driverLat = Integer.parseInt(rec.get("n.latitude").toString());
                String driverStreet = rec.get("n.street").toString();
                String driverId = rec.get("n.uid").toString();


            }

            this.sendResponse(r, resp, 200);


        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }

        this.sendStatus(r, 200);
    }
}
