package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Driver extends Endpoint {

    /**
     * GET /trip/driver/:uid
     * @param uid
     * @return 200, 400, 404
     * Get all trips driver with the given uid has.
     */

    public void handleGet(HttpExchange r) throws IOException,JSONException {
        // TODO
        try{
            String[] splitUrl = r.getRequestURI().getPath().split("/");
            if (splitUrl.length != 4) {
                this.sendStatus(r, 400);
                return;
            }
            // check if uid given is integer, return 400 if not
            String uidString = splitUrl[3];
            int uid;
            try {
                uid = Integer.parseInt(uidString);
            } catch (Exception e) {
                e.printStackTrace();
                this.sendStatus(r, 400);
                return;
            }

            JSONArray rs;
            rs = this.dao.getUserTrips(uidString, "driver");
            if(0 == rs.length()){
                this.sendStatus(r, 404);
                return;
            }
            JSONObject var = new JSONObject();
            JSONObject trips = new JSONObject();
            trips.put("trips", rs);
            var.put("data", trips);
            this.sendResponse(r, var, 200);
            return;
        }catch(Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }

}
