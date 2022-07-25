package ca.utoronto.utm.mcs;

import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Everything you need in order to send and recieve httprequests to 
 * the microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.OutputStream;    // Also given to you to send back your response
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestRouter implements HttpHandler {

	/**
	 * You may add and/or initialize attributes here if you
	 * need.
	 */
	public RequestRouter() {

	}

	@Override
	public void handle(HttpExchange r) throws IOException {
// check if request url isn't malformed
		String[] splitUrl = r.getRequestURI().getPath().split("/");
		String requestURL = "";
		if (splitUrl[1].equals("location")) {
			requestURL = "http://locationmicroservice:8000"+ r.getRequestURI();
		}
		else if (splitUrl[1].equals("user")) {
			requestURL = "http://usermicroservice:8000"+ r.getRequestURI();
		}
		else if (splitUrl[1].equals("trip")) {
			requestURL = "http://tripinfomicroservice:8000"+ r.getRequestURI();
		}

		try {
			HttpClient client = HttpClient.newHttpClient();
			InputStream inputStream = r.getRequestBody();
			String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(requestURL))
					.header("Content-Type", "application/json")
					.method(r.getRequestMethod(), BodyPublishers.ofString(body)).version(HttpClient.Version.HTTP_1_1)
					.timeout(Duration.ofSeconds(5))
					.build();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			r.sendResponseHeaders(response.statusCode(), response.body().length());
			this.writeOutputStream(r, response.body());
		}
		catch (Exception e){
			JSONObject res = new JSONObject();
			try {
				res.put("status", "Request timed out");
			} catch (JSONException ex) {
				throw new RuntimeException(ex);
			}
			String response = res.toString();
			r.sendResponseHeaders(408, response.length());
			this.writeOutputStream(r, response);
		}
	}

	public void writeOutputStream(HttpExchange r, String response) throws IOException {
		OutputStream os = r.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
