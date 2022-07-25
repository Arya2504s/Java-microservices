package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONException;
import java.io.IOException;
import org.json.JSONObject;

public class Register extends Endpoint {

    /**
     * POST /user/register
     * @body name, email, password
     * @return 200, 400, 500
     * Register a user into the system using the given information.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        try {
          String body = Utils.convert(r.getRequestBody());
          JSONObject deserialized = new JSONObject(body);
          String name = "", email = "", password = "";
          String response = "";

          String[] field = {"name", "email", "password"};
          Class<?>[] bodyTypes = {String.class, String.class, String.class};
          if (validateFields(deserialized, field, bodyTypes)) {
             name = deserialized.getString("name");
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
          if (!resultHasNext) {
            int uid = this.dao.createUser(name, email, password);
            // making the response
            JSONObject resp = new JSONObject();
            resp.put("uid", String.valueOf(uid));
            this.sendResponse(r, resp, 200);
          } else {
            this.sendStatus(r, 409);
              return;
          }
        } catch (Exception e){
          e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }
    }
}
