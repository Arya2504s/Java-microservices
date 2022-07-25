package ca.utoronto.utm.mcs;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MongoDao {
	
	public MongoCollection<Document> collection;
	Dotenv dotenv = Dotenv.load();
	private final String addr = dotenv.get("MONGODB_ADDR");
	private final String username = "root";
	private final String password = "123456";
	private final String uri = "mongodb://%s:%s@" + addr + ":27017";
	private final String uriDb = String.format(uri,username, password);
	public MongoDao() {
        // TODO: 
        // Connect to the mongodb database and create the database and collection. 
        // Use Dotenv like in the DAOs of the other microservices.
		try {
			MongoClient mongo = MongoClients.create(this.uriDb);
			MongoDatabase database = mongo.getDatabase("trip");
			System.out.println(database);
			this.collection = database.getCollection("trips");
			System.out.println(this.collection);
			System.out.println("Connection Successful");
		}catch(Exception e){
			System.out.println("Connection unsuccessful");
		}
	}

	// *** implement database operations here *** //

	public JSONArray getUserTrips(String uid, String type) throws Exception  {
		FindIterable<Document> res = this.collection.find(Filters.eq(type,uid));
		try {
			JSONArray result = new JSONArray();
			for (Document doc : res) {
				doc.put("_id",doc.getObjectId("_id").toString());
				doc.remove(type);
				result.put(doc);
			}
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

	public JSONObject createTrip(int startTime, String driver, String passenger) throws JSONException {
		Document doc = new Document();
		doc.put("startTime", startTime);
		doc.put("driver", driver);
		doc.put("passenger", passenger);

		try{
			this.collection.insertOne(doc);
			JSONObject result = new JSONObject();
			result.put("$oid", doc.getObjectId("_id"));
			return result;
		}catch (Exception e) {
			throw e;
		}
	}

	public boolean updateTrip(int endTime, String totalCost, int timeElapsed, int distance, ObjectId id){
		Document doc = new Document();
		doc.put("distance", distance);
		doc.put("endTime", endTime);
		doc.put("timeElapsed", timeElapsed);
		doc.put("totalCost", totalCost);
		try{
			boolean exist = this.tripExists(id);
			if(!exist){
				return false;
			}
		}catch (Exception e) {
			throw e;
		}
		try {
			this.collection.updateOne(Filters.eq("_id", id), new Document("$set", doc));
			return true;
		}catch (Exception e) {
			throw e;
		}
	}

	public boolean tripExists(ObjectId id){
	FindIterable<Document> res = this.collection.find(Filters.eq("_id",id));
		try {
			JSONArray result = new JSONArray();
			for (Document doc : res) {
				doc.put("_id", doc.getObjectId("_id").toString());
				result.put(doc);
			}
			if(result.length() == 0){
				return false;
			}
			return true;
		}catch (Exception e) {
			throw e;
		}
	}

	public ArrayList<String> tripInfo(ObjectId id) {
		FindIterable<Document> res = this.collection.find(Filters.eq("_id", id));
		ArrayList<String> result = new ArrayList<>();
		try {
			for (Document doc : res) {
				result.add(doc.getString("driver"));
				result.add(doc.getString("passenger"));
			}
			return result;
		}catch (Exception e) {
			throw e;
		}
	}

}
