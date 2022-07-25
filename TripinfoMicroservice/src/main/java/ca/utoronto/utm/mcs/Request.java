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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Request extends Endpoint {

    /**
     * POST /trip/request
     * @body uid, radius
     * @return 200, 400, 404, 500
     * Returns a list of drivers within the specified radius 
     * using location microservice. List should be obtained
     * from navigation endpoint in location microservice
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException,JSONException{
        System.out.println("here in post");
        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));

        //check if required body parameters are there
        if (body.has("uid") && body.has("radius")) {
            String[] fields = {"uid", "radius"};
            Class<?>[] fieldClasses = {String.class, Integer.class};
            if (!this.validateFields(body, fields, fieldClasses)) {
                this.sendStatus(r, 400);
                return;
            }
            String uid = body.getString("uid");
            int radius = body.getInt("radius");

            //now need to send request to location to see if there is a driver nearby
            String endpoint = "http://locationmicroservice:8000/location/nearbyDriver/%s?radius=%s";
            endpoint = String.format(endpoint, uid, radius);
            System.out.println(endpoint);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = null;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (InterruptedException e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
            }
            int status = response.statusCode();
            if(status != 200){
                this.sendStatus(r, status);
                return;
            }

            //Read JSON response and print
            JSONObject myResponse = new JSONObject(response.body());
            JSONObject var = new JSONObject();
            var.put("data",  myResponse.getJSONObject("data").names());
            this.sendResponse(r, var, 200);
        }
        else{
            this.sendStatus(r, 404);
        }
    }
}
