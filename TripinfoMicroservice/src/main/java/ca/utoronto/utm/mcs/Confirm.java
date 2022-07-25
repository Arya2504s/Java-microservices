package ca.utoronto.utm.mcs;

import com.mongodb.util.JSON;
import com.sun.net.httpserver.HttpExchange;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Confirm extends Endpoint {

    /**
     * POST /trip/confirm
     * @body driver, passenger, startTime
     * @return 200, 400
     * Adds trip info into the database after trip has been requested.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        try {
            String[] splitUrl = r.getRequestURI().getPath().split("/");
            if (splitUrl.length != 3) {
                this.sendStatus(r, 400);
                return;
            }

            String[] fields = {"startTime", "driver", "passenger"};
            Class<?>[] fieldClasses = {Integer.class, String.class, String.class};
            try {
                JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
                if(!this.validateFields(body,fields , fieldClasses)){
                    this.sendStatus(r,400);
                    return;
                }
                int startTime;
                String driver, passenger;
                startTime = body.getInt("startTime");
                driver = body.getString("driver");
                passenger = body.getString("passenger");
                JSONObject id = this.dao.createTrip(startTime, driver, passenger);
                JSONObject var = new JSONObject();
                JSONObject var2 = new JSONObject();
                var.put("_id", id);
                var2.put("data", var);
                this.sendResponse(r,var2,200);
            }catch(Exception e){
                this.sendStatus(r, 400);
            }
        }catch(Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
