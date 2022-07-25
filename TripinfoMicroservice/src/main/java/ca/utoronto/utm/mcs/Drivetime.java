package ca.utoronto.utm.mcs;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * other microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import com.sun.net.httpserver.HttpExchange;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Drivetime extends Endpoint {

    /**
     * GET /trip/driverTime/:_id
     * @param _id
     * @return 200, 400, 404, 500
     * Get time taken to get from driver to passenger on the trip with
     * the given _id. Time should be obtained from navigation endpoint
     * in location microservice.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        try{

            String[] splitUrl = r.getRequestURI().getPath().split("/");
            if (splitUrl.length != 4) {
                this.sendStatus(r, 400);
                return;
            }

            String trip_id = splitUrl[3];
            if (!ObjectId.isValid(trip_id)) {
                System.out.println(trip_id);
                System.out.println("error id");
                this.sendStatus(r, 400);
                return;
            }
            if(!this.dao.tripExists(new ObjectId(trip_id))){
                this.sendStatus(r, 404);
            }
            
            ArrayList<String> tripInfo = this.dao.tripInfo(new ObjectId(trip_id));
            String driver = tripInfo.get(0);
            String passenger = tripInfo.get(1);
            String endpoint = "http://locationmicroservice:8000/location/navigation/:%s?passengerUid=%s";
            endpoint = String.format(endpoint, driver, passenger);
            System.out.println(endpoint);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if(status != 200){
                this.sendStatus(r, status);
                return;
            }

            //Read JSON response and print
            JSONObject myResponse = new JSONObject(response.body());
            JSONObject data = myResponse.getJSONObject("data");
            JSONObject var = new JSONObject();
            var.put("arrival_time", data.getInt("total_time"));
            JSONObject var2 = new JSONObject();
            var2.put("data", var);
            this.sendResponse(r, var2, 200);

        }catch (Exception e){
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
