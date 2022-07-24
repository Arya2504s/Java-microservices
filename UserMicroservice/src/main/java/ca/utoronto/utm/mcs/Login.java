package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import java.sql.ResultSet;
import org.json.JSONException;
import java.io.IOException;
import org.json.JSONObject;

public class Login extends Endpoint {

    /**
     * POST /user/login
     * @body email, password
     * @return 200, 400, 401, 404, 500
     * Login a user into the system if the given information matches the 
     * information of the user in the database.
     */
    
    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            String email = "", password = "";

            String[] field = {"email", "password"};
            Class<?>[] bodyTypes = {String.class, String.class};
            if (validateFields(deserialized, field, bodyTypes)) {
                email = deserialized.getString("email");
                password = deserialized.getString("password");
            }  else {
                this.sendStatus(r, 400);
                return;
            }

            // make query and get required data, return 500 if error
            ResultSet rs;
            boolean resultHasNext;
            rs = this.dao.getUserDataFromEmail(email);
            resultHasNext = rs.next();
            if (resultHasNext) {
                // making the response
                JSONObject resp = new JSONObject();
                if (!rs.getString("password").equals(password)) {
                    this.sendStatus(r, 401);
                    return;
                }
                resp.put("uid", String.valueOf(rs.getInt("uid")));
                this.sendResponse(r, resp, 200);
            } else {
                this.sendStatus(r, 404);
                return;
            }
        } catch (Exception e){
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }
    }
}
