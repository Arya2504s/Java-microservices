package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Please write your tests in this class.
 */

public class AppTest {

    final static String URL = "http://localhost:8004";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody)).version(HttpClient.Version.HTTP_1_1)
                .build();

        //System.out.println(request);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @BeforeAll
    public static void db() throws JSONException, IOException, InterruptedException {
        Runtime.getRuntime().exec("mvn exec:java");
        JSONObject body = new JSONObject()
                .put("uid", "12")
                .put("is_driver", false);
        HttpResponse<String> res = sendRequest("/location/user", "PUT", body.toString());

        body = new JSONObject()
                .put("uid", "345")
                .put("is_driver", true);
        res = sendRequest("/location/user", "PUT", body.toString());

        //edit users to include location info
        body = new JSONObject()
                .put("longitude", 50.001)
                .put("latitude", 50.001)
                .put("street", "McCowan");
        res = sendRequest("/location/12", "PATCH", body.toString());

        //edit users to include location info
        body = new JSONObject()
                .put("longitude", 50.132)
                .put("latitude",  50.132)
                .put("street", "Markham");
        res = sendRequest("/location/345", "PATCH", body.toString());

        body = new JSONObject()
                .put("roadName", "Markham")
                .put("hasTraffic", false);
        res = sendRequest("/location/road", "PUT", body.toString());

        body = new JSONObject()
                .put("roadName", "McCowan")
                .put("hasTraffic", true);
        res = sendRequest("/location/road", "PUT", body.toString());

        //more streets for routes
        body = new JSONObject()
                .put("roadName", "Random1")
                .put("hasTraffic", false);
        res = sendRequest("/location/road", "PUT", body.toString());

        body = new JSONObject()
                .put("roadName", "Random2")
                .put("hasTraffic", false);
        res = sendRequest("/location/road", "PUT", body.toString());

        //create routes between the roads
        body = new JSONObject()
                .put("roadName1", "Markham")
                .put("roadName2", "McCowan")
                .put("hasTraffic", true)
                .put("time", 30);
        res = sendRequest("/location/hasRoute", "POST", body.toString());

        body = new JSONObject()
                .put("roadName1", "Markham")
                .put("roadName2", "Random1")
                .put("hasTraffic", false)
                .put("time", 3);
        res = sendRequest("/location/hasRoute", "POST", body.toString());

        body = new JSONObject()
                .put("roadName1", "Random1")
                .put("roadName2", "Random2")
                .put("hasTraffic", false)
                .put("time", 5);
        res = sendRequest("/location/hasRoute", "POST", body.toString());

        body = new JSONObject()
                .put("roadName1", "Random2")
                .put("roadName2", "McCowan")
                .put("hasTraffic", false)
                .put("time", 10);
        res = sendRequest("/location/hasRoute", "POST", body.toString());
        assertTrue(true);
    }

    @AfterAll
    public static void teardown() throws IOException, InterruptedException {
        sendRequest("/trip/deleteAll", "DELETE", "{}");
    }

    @Test
    public void tripRequestPass() throws JSONException, IOException, InterruptedException {

        JSONObject body = new JSONObject()
                .put("uid", "12")
                .put("radius", 20);

        HttpResponse<String> response = sendRequest("/trip/request", "POST", body.toString());

        //now make sure the response has the driver we want
        assertTrue(response.statusCode() == HttpURLConnection.HTTP_OK);
        System.out.println(response.body());
        assertEquals("{\"data\":[\"345\"],\"status\":\"OK\"}", response.body().toString());
    }

    @Test
    public void tripRequestFail() throws JSONException, IOException, InterruptedException {

        //no drivers found
        JSONObject body = new JSONObject()
                .put("uid", "123")
                .put("radius", 10000);

        HttpResponse<String> response = sendRequest("/trip/request", "POST", body.toString());

        //now make sure the response has the driver we want
        assertTrue(response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND);
        System.out.println(response.body());
        assertEquals("{\"status\":\"NOT FOUND\"}", response.body().toString());
    }


    @Test
    public void tripConfirmPass() throws JSONException, IOException, InterruptedException {
        JSONObject req = new JSONObject()
                .put("driver", "me")
                .put("passenger", "you")
                .put("startTime", 123456);
        HttpResponse<String> response = sendRequest("/trip/confirm", "POST", req.toString());
        JSONObject res = new JSONObject(response.body());
        res = res.getJSONObject("data");
        assertTrue(response.statusCode()==HttpURLConnection.HTTP_OK);
        assertTrue(response.body().contains("\"status\":\"OK\"") && response.body().contains("\"_id\":{\"$oid\"")
                && res.getJSONObject("_id").get("$oid").getClass() == String.class);
    }

    @Test
    public void tripConfirmFail() throws JSONException, IOException, InterruptedException {
        JSONObject req = new JSONObject()
                .put("driver", "me")
                .put("passenger", "you");
        HttpResponse<String> res = sendRequest("/trip/confirm", "POST", req.toString());
        assertTrue(res.statusCode()==HttpURLConnection.HTTP_BAD_REQUEST);
        assertTrue(res.body().contains("\"status\":\"BAD REQUEST\""));
    }

    @Test
    public void updateTripPass() throws JSONException, IOException, InterruptedException{
        JSONObject req = new JSONObject()
                .put("driver", "arya")
                .put("passenger", "hailey")
                .put("startTime", 10);
        HttpResponse<String> response = sendRequest("/trip/confirm", "POST", req.toString());
        System.out.println(response.body());

        JSONObject res = new JSONObject(response.body());
        String tripId = res.getJSONObject("data").getJSONObject("_id").getString("$oid");

        String url = "/trip/" + tripId;
        req = new JSONObject()
                .put("distance", 3)
                .put("endTime", 255)
                .put("timeElapsed", 245)
                .put("totalCost", "10.25");
        response = sendRequest(url, "PATCH", req.toString());

        System.out.println(response.body());

        assertTrue(response.statusCode()==HttpURLConnection.HTTP_OK);
        assertEquals("{\"status\":\"OK\"}", response.body());
    }

    @Test
    public void updateTripFail() throws IOException, JSONException, InterruptedException {
        JSONObject req = new JSONObject()
                .put("distance", 3)
                .put("endTime", 255)
                .put("timeElapsed", 245)
                .put("totalCost", "10.25");
        HttpResponse<String> res = sendRequest("/trip/62df0745525aaf217536b602", "PATCH", req.toString());

        System.out.println(res.body());

        assertTrue(res.statusCode()==HttpURLConnection.HTTP_NOT_FOUND);
        assertEquals("{\"status\":\"NOT FOUND\"}",res.body());
    }

    @Test
    public void tripsForPassengerPass() throws JSONException, IOException, InterruptedException{
        JSONObject req = new JSONObject()
                .put("driver", "me")
                .put("passenger", "hailey")
                .put("startTime", 345);
        HttpResponse<String> res = sendRequest("/trip/confirm", "POST", req.toString());

        String url = "/trip/passenger/hailey";
        res = sendRequest(url, "GET", req.toString());
        JSONArray trips = new JSONObject(res.body()).getJSONObject("data").getJSONArray("trips");

        String trip1, trip2;
        if (trips.get(0).toString().contains("arya")) {
            trip1 = trips.get(0).toString();
            trip2 = trips.get(1).toString();
        } else {
            trip1 = trips.get(1).toString();
            trip2 = trips.get(0).toString();
        }


        System.out.println(trip1);
        System.out.println(trip2);

        assertTrue(res.statusCode()==HttpURLConnection.HTTP_OK);
        assertTrue(trip1.contains("\"driver\":\"arya\"") && trip1.contains("\"timeElapsed\":245")
                && trip1.contains("\"distance\":3") && trip1.contains("\"endTime\":255")
                && trip1.contains("\"startTime\":10") && trip1.contains("\"totalCost\":\"10.25\"")
                && trip1.contains("\"_id\""));
        assertTrue(trip2.contains("\"driver\":\"me\"") && trip2.contains("\"startTime\":345")  && trip2.contains("\"_id\""));
        assertTrue(res.body().contains("\"status\":\"OK\""));
    }

    @Test
    public void tripsForPassengerFail() throws JSONException, IOException, InterruptedException{

        String url = "/trip/passenger/urmom";
        HttpResponse<String> res = sendRequest(url, "GET", "{}");

        assertTrue(res.statusCode()==HttpURLConnection.HTTP_NOT_FOUND);
        assertTrue(res.body().contains("\"status\":\"NOT FOUND\""));

    }

    @Test
    public void tripsForDriverPass()throws JSONException, IOException, InterruptedException{
        JSONObject req = new JSONObject()
                .put("driver", "arya")
                .put("passenger", "sally")
                .put("startTime", 790);
        HttpResponse<String> res = sendRequest("/trip/confirm", "POST", req.toString());

        String url = "/trip/driver/arya";
        res = sendRequest(url, "GET", req.toString());
        JSONArray trips = new JSONObject(res.body()).getJSONObject("data").getJSONArray("trips");

        String trip1, trip2;
        if (trips.get(0).toString().contains("hailey")) {
            trip1 = trips.get(0).toString();
            trip2 = trips.get(1).toString();
        } else {
            trip1 = trips.get(1).toString();
            trip2 = trips.get(0).toString();
        }

        System.out.println(trip1);
        System.out.println(trip2);

        assertTrue(res.statusCode()==HttpURLConnection.HTTP_OK);
        assertTrue(trip1.contains("\"passenger\":\"hailey\"") && trip1.contains("\"timeElapsed\":245")
                && trip1.contains("\"distance\":3") && trip1.contains("\"endTime\":255")
                && trip1.contains("\"startTime\":10") && trip1.contains("\"totalCost\":\"10.25\"")
                && trip1.contains("\"_id\""));
        assertTrue(trip2.contains("\"passenger\":\"sally\"") && trip2.contains("\"startTime\":790")  && trip2.contains("\"_id\""));
        assertTrue(res.body().contains("\"status\":\"OK\""));
    }

    @Test
    public void tripsForDriverFail() throws JSONException, IOException, InterruptedException{
        String url = "/trip/driver/urmom";
        HttpResponse<String> res = sendRequest(url, "GET", "{}");

        assertTrue(res.statusCode()==HttpURLConnection.HTTP_NOT_FOUND);
        assertTrue(res.body().contains("\"status\":\"NOT FOUND\""));
    }

    @Test
    public void driverTimePass() throws IOException, JSONException, InterruptedException {
        JSONObject req = new JSONObject()
                .put("driver", "345")
                .put("passenger", "12")
                .put("startTime", 0);
        HttpResponse<String> response = sendRequest("/trip/confirm", "POST", req.toString());
        JSONObject res = new JSONObject(response.body());
        res = res.getJSONObject("data");

        //getting id
        String oid = res.getJSONObject("_id").getString("$oid");
        System.out.println(oid);

        //making request with id
        String endpoint = "/trip/driverTime/%s";
        endpoint = String.format(endpoint, oid);

        response = sendRequest(endpoint, "GET", req.toString());

        System.out.println(response.body() +" driver time pass");
        //now make sure the response has the driver we want
        assertTrue(response.statusCode() == HttpURLConnection.HTTP_OK);
        assertTrue(response.body().contains("{\"data\":{\"arrival_time\":18}") && response.body().contains("\"status\":\"OK\""));


    }

    @Test
    public void driverTimeFail() throws IOException, JSONException, InterruptedException {
        //making request with id

        String endpoint = "/trip/driverTime/23";

        HttpResponse<String> response = sendRequest(endpoint, "GET", new JSONObject().toString());

        //now make sure the response has the driver we want
        assertTrue(response.statusCode() == HttpURLConnection.HTTP_BAD_REQUEST);
        System.out.println(response.body());
        assertEquals("{\"status\":\"BAD REQUEST\"}", response.body().toString());
    }

}
