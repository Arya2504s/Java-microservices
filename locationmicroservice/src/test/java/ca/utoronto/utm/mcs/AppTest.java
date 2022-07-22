package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Please write your tests in this class. 
 */
 
public class AppTest {
    @BeforeAll
    public static void init() throws IOException {
        Runtime.getRuntime().exec("mvn exec:java");

        try {
            HttpClient client = HttpClient.newHttpClient();
            String body = "{\"uid\": \"passenger\", \"is_driver\": false}";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/user"))
                .header("Content-Type", "application/json")
                .method("PUT", BodyPublishers.ofString(body))
                .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println(response.statusCode());

            client = HttpClient.newHttpClient();
            body = "{\"uid\": \"driver\", \"is_driver\": true}";
            request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/user"))
                .header("Content-Type", "application/json")
                .method("PUT", BodyPublishers.ofString(body))
                .build();
            response = client.send(request, BodyHandlers.ofString());
            System.out.println(response.statusCode());

            client = HttpClient.newHttpClient();
            body = "{\"longitude\": 1.25, \"latitude\": 1.25, \"street\": \"street1\"}";
            request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/passenger"))
                .header("Content-Type", "application/json")
                .method("PATCH", BodyPublishers.ofString(body))
                .build();
            response = client.send(request, BodyHandlers.ofString());
            System.out.println(response.statusCode());

            client = HttpClient.newHttpClient();
            body = "{\"longitude\": 1.24, \"latitude\": 1.24, \"street\": \"street2\"}";
            request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/driver"))
                .header("Content-Type", "application/json")
                .method("PATCH", BodyPublishers.ofString(body))
                .build();
            response = client.send(request, BodyHandlers.ofString());
            System.out.println(response.statusCode());

            client = HttpClient.newHttpClient();
            body = "{\"roadName\": \"street1\", \"hasTraffic\": true}";
            request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/road"))
                .header("Content-Type", "application/json")
                .method("PUT", BodyPublishers.ofString(body))
                .build();
            response = client.send(request, BodyHandlers.ofString());
            System.out.println(response.statusCode());

            client = HttpClient.newHttpClient();
            body = "{\"roadName\": \"street2\", \"hasTraffic\": true}";
            request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/road"))
                .header("Content-Type", "application/json")
                .method("PUT", BodyPublishers.ofString(body))
                .build();
            response = client.send(request, BodyHandlers.ofString());
            System.out.println(response.statusCode());

            client = HttpClient.newHttpClient();
            body = "{\"roadName1\": \"street2\", \"roadName2\": \"street1\","
                + " \"hasTraffic\": true, \"time\": 10}";
            request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/hasRoute"))
                .header("Content-Type", "application/json")
                .method("POST", BodyPublishers.ofString(body))
                .build();
            response = client.send(request, BodyHandlers.ofString());
            System.out.println(response.statusCode());

            client = HttpClient.newHttpClient();
            body = "{\"roadName1\": \"street1\", \"roadName2\": \"street2\","
                + " \"hasTraffic\": true, \"time\": 7}";
            request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/hasRoute"))
                .header("Content-Type", "application/json")
                .method("POST", BodyPublishers.ofString(body))
                .build();
            response = client.send(request, BodyHandlers.ofString());
            System.out.println(response.statusCode());

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void teardown() throws IOException, InterruptedException {
//        try {
//            HttpClient client = HttpClient.newHttpClient();
//            String body = "{\"uid\": \"passenger\"}";
//            HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://0.0.0.0:8200/location/user"))
//                .header("Content-Type", "application/json")
//                .method("DELETE", BodyPublishers.ofString(body))
//                .build();
//            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
//
//            client = HttpClient.newHttpClient();
//            body = "{\"uid\": \"driver\"}";
//            request = HttpRequest.newBuilder()
//                .uri(URI.create("http://0.0.0.0:8200/location/user"))
//                .header("Content-Type", "application/json")
//                .method("DELETE", BodyPublishers.ofString(body))
//                .build();
//            response = client.send(request, BodyHandlers.ofString());
//
//            client = HttpClient.newHttpClient();
//            body = "{\"roadName1\": \"street2\", \"roadName2\": \"street1\"}";
//            request = HttpRequest.newBuilder()
//                .uri(URI.create("http://0.0.0.0:8200/location/route"))
//                .header("Content-Type", "application/json")
//                .method("DELETE", BodyPublishers.ofString(body))
//                .build();
//            response = client.send(request, BodyHandlers.ofString());
//        } catch (Exception e){
//            e.printStackTrace();
//        }
    }

    @Test
    public void nearbyDriverPass() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/nearbyDriver/passenger?radius=100"))
                .header("Content-Type", "application/json")
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println("nearByPass: " + response.statusCode() + response.body());
            assertTrue(response.statusCode()==200);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "error");
        }
    }

    @Test
    public void nearbyDriverFail() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/nearbyDriver/passenger"))
                .header("Content-Type", "application/json")
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println("nearByFail: " + response.statusCode() + response.body());
            assertTrue(response.statusCode() == 400);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "error");
        }
    }

    @Test
    public void navigationPass() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/navigation/driver?passengerUid=passenger"))
                .header("Content-Type", "application/json")
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println("navigationPass: " + response.statusCode() + response.body());
            assertTrue(response.statusCode() == 200);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "error");
        }
    }

    @Test
    public void navigationFail() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8000/location/navigation/notdriver?passengerUid=fakepassenger"))
                .header("Content-Type", "application/json")
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println("navigationFail: " + response.statusCode() + response.body());
            assertTrue(response.statusCode() == 404);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "error");
        }
    }
}
