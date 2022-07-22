package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
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
    }

    @AfterAll
    public static void teardown() throws IOException, InterruptedException {

    }

    @Test
    public void registerPass() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String body = "{\"name\": \"someone\", \"email\": \"someemail\", \"password\": \"pass\"}";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8001/user/register"))
                .header("Content-Type", "application/json")
                .method("POST", BodyPublishers.ofString(body))
                .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println("registerPass: " + response.statusCode() + response.body());
            assertTrue(response.statusCode()==200);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "error");
        }
    }

    @Test
    public void registerFail() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String body = "{\"name\": \"someone\"}";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8001/user/register"))
                .header("Content-Type", "application/json")
                .method("POST", BodyPublishers.ofString(body))
                .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println("registerFail: " + response.statusCode() + response.body());
            assertTrue(response.statusCode()==400);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "error");
        }
    }

    @Test
    public void loginPass() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String body1 = "{\"name\": \"someone\", \"email\": \"emailemail\", \"password\": \"pass\"}";
            HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8001/user/register"))
                .header("Content-Type", "application/json")
                .method("POST", BodyPublishers.ofString(body1))
                .build();
            client.send(request1, BodyHandlers.ofString());

            String body2 = "{\"email\": \"emailemail\", \"password\": \"pass\"}";
            HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8001/user/login"))
                .header("Content-Type", "application/json")
                .method("POST", BodyPublishers.ofString(body2))
                .build();
            HttpResponse<String> response = client.send(request2, BodyHandlers.ofString());
            System.out.println("loginPass: " + response.statusCode() + response.body());
            assertTrue(response.statusCode()==200);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "error");
        }
    }

    @Test
    public void loginFail() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String body = "{\"email\": \"someemail\"}";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://0.0.0.0:8001/user/login"))
                .header("Content-Type", "application/json")
                .method("POST", BodyPublishers.ofString(body))
                .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println("loginFail: " + response.statusCode() + response.body());
            assertTrue(response.statusCode()==400);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "error");
        }
    }
}
