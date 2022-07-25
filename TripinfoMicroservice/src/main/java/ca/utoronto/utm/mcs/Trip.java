package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Trip extends Endpoint {

    /**
     * PATCH /trip/:_id
     * @param _id
     * @body distance, endTime, timeElapsed, totalCost
     * @return 200, 400, 404
     * Adds extra information to the trip with the given id when the 
     * trip is done. 
     */

    @Override
    public void handlePatch(HttpExchange r) throws IOException, JSONException {
        // TODO
        try {
            String[] splitUrl = r.getRequestURI().getPath().split("/");
            if (splitUrl.length != 3) {
                this.sendStatus(r, 400);
                return;
            }

            ObjectId tripId;
            String trip_id = splitUrl[2];
            try {
                tripId = new ObjectId(trip_id);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                this.sendStatus(r, 400);
                return;
            }
            String[] fields = {"distance", "endTime", "timeElapsed","totalCost"};
            Class<?>[] fieldClasses = {Integer.class, Integer.class, Integer.class, String.class};
            JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
            if(!this.validateFields(body,fields , fieldClasses)){
                this.sendStatus(r,400);
                return;
            }
            int distance, endTime, timeElapsed;
            String totalCost;
            distance = body.getInt("distance");
            endTime = body.getInt("endTime");
            timeElapsed = body.getInt("timeElapsed");
            totalCost = body.getString("totalCost");
            boolean res = this.dao.updateTrip(endTime, totalCost, timeElapsed,distance, tripId);
            if(!res){
                this.sendStatus(r,404);
                return;
            }
            this.sendStatus(r, 200);
        }catch(Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
