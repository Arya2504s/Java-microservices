package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*
Please write your tests for the TripInfo Microservice in this class.
*/

public class AppTest {

    final static String API_URL = "http://localhost:8004";

    @AfterEach
    public void cleanUpEach() throws InterruptedException, IOException {
        System.out.println("After Each cleanUpEach() method called");
        sendRequest("/trip/deleteAll", "DELETE", "{}");
        System.out.println("deleted");
    }

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        System.out.println(request);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @BeforeEach
    public void populateDatabase() throws JSONException, IOException, InterruptedException {

        //populating neo4j db
        //add a user that is not a driver
        JSONObject body = new JSONObject()
                .put("uid", "123")
                .put("is_driver", false);
        HttpResponse<String> res = sendRequest("/location/user", "PUT", body.toString());

        //add a driver
        body = new JSONObject()
                .put("uid", "1")
                .put("is_driver", true);
        res = sendRequest("/location/user", "PUT", body.toString());

        //edit users to include location info
        body = new JSONObject()
                .put("longitude", 79.0358)
                .put("latitude", 42.0057)
                .put("street", "Dundas St. W.");
        res = sendRequest("/location/123", "PATCH", body.toString());

        //edit users to include location info
        body = new JSONObject()
                .put("longitude", 79.3832)
                .put("latitude",  43.6532)
                .put("street", "Queen St. E.");
        res = sendRequest("/location/1", "PATCH", body.toString());

        //need to add nodes for the streets
        body = new JSONObject()
                .put("roadName", "Queen St. E.")
                .put("hasTraffic", false);
        res = sendRequest("/location/road", "PUT", body.toString());

        body = new JSONObject()
                .put("roadName", "Dundas St. W.")
                .put("hasTraffic", true);
        res = sendRequest("/location/road", "PUT", body.toString());

        //more streets for routes
        body = new JSONObject()
                .put("roadName", "That St.")
                .put("hasTraffic", false);
        res = sendRequest("/location/road", "PUT", body.toString());

        body = new JSONObject()
                .put("roadName", "This St.")
                .put("hasTraffic", false);
        res = sendRequest("/location/road", "PUT", body.toString());

        //create routes between the roads
        body = new JSONObject()
                .put("roadName1", "Queen St. E.")
                .put("roadName2", "Dundas St. W.")
                .put("hasTraffic", true)
                .put("time", 30);
        res = sendRequest("/location/hasRoute", "POST", body.toString());

        body = new JSONObject()
                .put("roadName1", "Queen St. E.")
                .put("roadName2", "This St.")
                .put("hasTraffic", false)
                .put("time", 3);
        res = sendRequest("/location/hasRoute", "POST", body.toString());

        body = new JSONObject()
                .put("roadName1", "This St.")
                .put("roadName2", "That St.")
                .put("hasTraffic", false)
                .put("time", 5);
        res = sendRequest("/location/hasRoute", "POST", body.toString());

        body = new JSONObject()
                .put("roadName1", "That St.")
                .put("roadName2", "Dundas St. W.")
                .put("hasTraffic", false)
                .put("time", 10);
        res = sendRequest("/location/hasRoute", "POST", body.toString());
    }

    @AfterEach
    public void clearDatabase() throws JSONException, IOException, InterruptedException {

        //clearing neo4j db
        System.out.println("After Each cleanUpEach() method called");
        sendRequest("/location/deleteAll", "DELETE", new JSONObject().toString());

        //clearing mongodb
        sendRequest("/trip/deleteAll", "DELETE", new JSONObject().toString());
        System.out.println("deleted");

    }


    @Test
    public void tripRequestPass() throws JSONException, IOException, InterruptedException {

        JSONObject body = new JSONObject()
                .put("uid", "123")
                .put("radius", 1000);

        HttpResponse<String> response = sendRequest("/trip/request", "POST", body.toString());

        //now make sure the response has the driver we want
        assertTrue(response.statusCode() == HttpURLConnection.HTTP_OK);
        System.out.println(response.body());
        assertEquals("{\"data\":[\"1\"],\"status\":\"OK\"}", response.body().toString());
    }

    @Test
    public void tripRequestFail() throws JSONException, IOException, InterruptedException {

        //no drivers found
        JSONObject body = new JSONObject()
                .put("uid", "123")
                .put("radius", 10);

        HttpResponse<String> response = sendRequest("/trip/request", "POST", body.toString());

        //now make sure the response has the driver we want
        assertTrue(response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND);
        System.out.println(response.body());
        assertEquals("{\"status\":\"NOT FOUND\"}", response.body().toString());
    }

    @Test
    public void tripConfirmPass() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject()
                .put("driver", "michaelscott")
                .put("passenger", "jimhalpert")
                .put("startTime", 123456);
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        JSONObject res = new JSONObject(confirmRes.body());
        res = res.getJSONObject("data");
        assertTrue(confirmRes.statusCode()==HttpURLConnection.HTTP_OK);
        assertTrue(confirmRes.body().contains("\"status\":\"OK\"") && confirmRes.body().contains("\"_id\":{\"$oid\"")
                && res.getJSONObject("_id").get("$oid").getClass() == String.class);
    }

    @Test
    public void tripConfirmFail() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject()
                .put("driver", "michaelscott")
                .put("passenger", "jimhalpert");
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        assertTrue(confirmRes.statusCode()==HttpURLConnection.HTTP_BAD_REQUEST);
        assertTrue(confirmRes.body().contains("\"status\":\"BAD REQUEST\"") && confirmRes.body().contains("\"data\":{}"));
    }

    @Test
    public void patchTripPass() throws JSONException, IOException, InterruptedException{
        JSONObject confirmReq = new JSONObject()
                .put("driver", "michaelscott")
                .put("passenger", "jimhalpert")
                .put("startTime", 123456);
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        System.out.println(confirmRes.body());

        JSONObject res = new JSONObject(confirmRes.body());
        String tripId = res.getJSONObject("data").getJSONObject("_id").getString("$oid");

        String url = "/trip/" + tripId;
        confirmReq = new JSONObject()
                .put("distance", 3)
                .put("endTime", 12345)
                .put("timeElapsed", "15:00")
                .put("totalCost", 10.25)
                .put("driverPayout", 10.25);
        confirmRes = sendRequest(url, "PATCH", confirmReq.toString());

        System.out.println(confirmRes.body());

        assertTrue(confirmRes.statusCode()==HttpURLConnection.HTTP_OK);
        assertEquals("{\"status\":\"OK\"}", confirmRes.body());
    }

    @Test
    public void patchTripFail() throws IOException, JSONException, InterruptedException {
        JSONObject confirmReq = new JSONObject()
                .put("distance", 3)
                .put("endTime", 12345)
                .put("timeElapsed", "15:00")
                .put("totalCost", 10.25)
                .put("driverPayout", 10.25);
        HttpResponse<String> confirmRes = sendRequest("/trip/thisTripIdDoesNotExist", "PATCH", confirmReq.toString());

        System.out.println(confirmRes.body());

        assertTrue(confirmRes.statusCode()==HttpURLConnection.HTTP_NOT_FOUND);
        assertEquals("{\"status\":\"NOT FOUND\"}",confirmRes.body());
    }

    @Test
    public void tripsForPassengerPass() throws JSONException, IOException, InterruptedException{
        JSONObject confirmReq = new JSONObject()
                .put("driver", "michaelscott")
                .put("passenger", "jimhalpert")
                .put("startTime", 123456);
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        System.out.println(confirmRes.body());



        confirmReq = new JSONObject()
                .put("driver", "dwightschrute")
                .put("passenger", "jimhalpert")
                .put("startTime", 1234);
        confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        System.out.println(confirmRes.body());

        JSONObject res = new JSONObject(confirmRes.body());
        String tripId = res.getJSONObject("data").getJSONObject("_id").getString("$oid");

        String url = "/trip/" + tripId;
        confirmReq = new JSONObject()
                .put("distance", 3)
                .put("endTime", 12345)
                .put("timeElapsed", "15:00")
                .put("totalCost", 12.25)
                .put("discount", 2)
                .put("driverPayout", 10.25);
        confirmRes = sendRequest(url, "PATCH", confirmReq.toString());

        System.out.println(confirmRes.body());


        url = "/trip/passenger/jimhalpert";
        confirmRes = sendRequest(url, "GET", confirmReq.toString());
        JSONArray trips = new JSONObject(confirmRes.body()).getJSONObject("data").getJSONArray("trips");

        String trip1, trip2;
        if (trips.get(0).toString().contains("dwightscrute")) {
            trip1 = trips.get(0).toString();
            trip2 = trips.get(1).toString();
        } else {
            trip1 = trips.get(1).toString();
            trip2 = trips.get(0).toString();
        }


        System.out.println(trip1);
        System.out.println(trip2);

        assertTrue(confirmRes.statusCode()==HttpURLConnection.HTTP_OK);
        assertTrue(trip1.contains("\"driver\":\"dwightschrute\"") && trip1.contains("\"timeElapsed\":\"15:00\"")
                && trip1.contains("\"distance\":3")   && trip1.contains("\"discount\":2") && trip1.contains("\"endTime\":12345")
                && trip1.contains("\"startTime\":1234") && trip1.contains("\"totalCost\":12.25"));
        assertTrue(trip2.contains("\"driver\":\"michaelscott\"") && trip2.contains("\"startTime\":123456"));
        assertTrue(confirmRes.body().contains("\"status\":\"OK\""));
    }

    @Test
    public void tripsForPassengerFail() throws JSONException, IOException, InterruptedException{
        JSONObject confirmReq = new JSONObject()
                .put("driver", "michaelscott")
                .put("passenger", "jimhalpert")
                .put("startTime", 123456);
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        System.out.println(confirmRes.body());

        JSONObject res = new JSONObject(confirmRes.body());
        String tripId = res.getJSONObject("data").getJSONObject("_id").getString("$oid");

        String url = "/trip/" + tripId;
        confirmReq = new JSONObject()
                .put("distance", 3)
                .put("endTime", 12345)
                .put("timeElapsed", "15:00")
                .put("totalCost", 10.25)
                .put("driverPayout", 10.25);
        confirmRes = sendRequest(url, "PATCH", confirmReq.toString());

        System.out.println(confirmRes.body());

        url = "/trip/passenger/michaelscott";
        confirmRes = sendRequest(url, "GET", confirmReq.toString());

        assertTrue(confirmRes.statusCode()==HttpURLConnection.HTTP_NOT_FOUND);
        assertTrue(confirmRes.body().contains("\"data\":{\"trips\":[]}") && confirmRes.body().contains("\"status\":\"NOT FOUND\""));

    }

    @Test
    public void tripsForDriverPass()throws JSONException, IOException, InterruptedException{
        JSONObject confirmReq = new JSONObject()
                .put("driver", "michaelscott")
                .put("passenger", "jimhalpert")
                .put("startTime", 56789);
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        System.out.println(confirmRes.body());


        confirmReq = new JSONObject()
                .put("driver", "michaelscott")
                .put("passenger", "pambeesly")
                .put("startTime", 43567);
        confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        System.out.println(confirmRes.body());

        JSONObject res = new JSONObject(confirmRes.body());
        String tripId = res.getJSONObject("data").getJSONObject("_id").getString("$oid");

        String url = "/trip/" + tripId;
        confirmReq = new JSONObject()
                .put("distance", 5)
                .put("endTime", 89234)
                .put("timeElapsed", "1:03:45")
                .put("totalCost", 50.2)
                .put("discount", 3)
                .put("driverPayout", 43.9);
        confirmRes = sendRequest(url, "PATCH", confirmReq.toString());

        System.out.println(confirmRes.body());


        url = "/trip/driver/michaelscott";
        confirmRes = sendRequest(url, "GET", confirmReq.toString());
        JSONArray trips = new JSONObject(confirmRes.body()).getJSONObject("data").getJSONArray("trips");

        String trip1, trip2;
        if (trips.get(0).toString().contains("pambeesly")) {
            trip1 = trips.get(0).toString();
            trip2 = trips.get(1).toString();
        } else {
            trip1 = trips.get(1).toString();
            trip2 = trips.get(0).toString();
        }

        System.out.println(trip1);
        System.out.println(trip2);

        assertTrue(confirmRes.statusCode()==HttpURLConnection.HTTP_OK);
        assertTrue(trip1.contains("\"passenger\":\"pambeesly\"") && trip1.contains("\"timeElapsed\":\"1:03:45\"")
                && trip1.contains("\"distance\":5") && trip1.contains("\"endTime\":89234")
                && trip1.contains("\"startTime\":43567") && trip1.contains("\"driverPayout\":43.9"));
        assertTrue(trip2.contains("\"passenger\":\"jimhalpert\"") && trip2.contains("\"startTime\":56789"));
        assertTrue(confirmRes.body().contains("\"status\":\"OK\""));
    }

    @Test
    public void tripsForDriverFail() throws JSONException, IOException, InterruptedException{
        JSONObject confirmReq = new JSONObject()
                .put("driver", "michaelscott")
                .put("passenger", "jimhalpert")
                .put("startTime", 123456);
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        System.out.println(confirmRes.body());

        System.out.println(confirmRes.body());

        String url = "/trip/driver/jimhalpert";
        confirmRes = sendRequest(url, "GET", confirmReq.toString());

        assertTrue(confirmRes.statusCode()==HttpURLConnection.HTTP_NOT_FOUND);
        assertTrue(confirmRes.body().contains("\"data\":{\"trips\":[]}") && confirmRes.body().contains("\"status\":\"NOT FOUND\""));
    }

    @Test
    public void driverTimePass() throws IOException, JSONException, InterruptedException {
        //need to have a document in the db with driver 1 and passenger 123
        JSONObject confirmReq = new JSONObject()
                .put("driver", "1")
                .put("passenger", "123")
                .put("startTime", 123456);
        HttpResponse<String> response = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        JSONObject res = new JSONObject(response.body());
        res = res.getJSONObject("data");

        //getting id
        String oid = res.getJSONObject("_id").getString("$oid");

        //making request with id
        String endpoint = "/trip/driverTime/%s";
        endpoint = String.format(endpoint, oid);

        response = sendRequest(endpoint, "GET", confirmReq.toString());

        //now make sure the response has the driver we want
        assertTrue(response.statusCode() == HttpURLConnection.HTTP_OK);
        System.out.println(response.body());
        assertEquals("{\"data\":{\"arrival_time\":18},\"status\":\"OK\"}", response.body().toString());

    }

    @Test
    public void driverTimeFail() throws IOException, JSONException, InterruptedException {
        //making request with id

        String endpoint = "/trip/driverTime/23";

        HttpResponse<String> response = sendRequest(endpoint, "GET", new JSONObject().toString());

        //now make sure the response has the driver we want
        assertTrue(response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND);
        System.out.println(response.body());
        assertEquals("{\"data\":{},\"status\":\"NOT FOUND\"}", response.body().toString());
    }
}
