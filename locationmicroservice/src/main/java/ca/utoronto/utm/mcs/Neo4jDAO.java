package ca.utoronto.utm.mcs;

import org.neo4j.driver.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.neo4j.driver.Record;

public class Neo4jDAO {

    private final Session session;
    private final Driver driver;
    private final String username = "neo4j";
    private final String password = "123456";

    public Neo4jDAO() {
        Dotenv dotenv = Dotenv.load();
        String addr = dotenv.get("NEO4J_ADDR");
        String uriDb = "bolt://" + addr + ":7687";

        this.driver = GraphDatabase.driver(uriDb, AuthTokens.basic(this.username, this.password));
        this.session = this.driver.session();
    }

    // *** implement database operations here *** //

    public Result addUser(String uid, boolean is_driver) {
        String query = "CREATE (n: user {uid: '%s', is_driver: %b, longitude: 0, latitude: 0, street: ''}) RETURN n";
        query = String.format(query, uid, is_driver);
        return this.session.run(query);
    }

    public Result deleteUser(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) DETACH DELETE n RETURN n";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result getUserLocationByUid(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) RETURN n.longitude, n.latitude, n.street";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result getUserByUid(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) RETURN n";
        query = String.format(query, uid);
        return this.session.run(query);
    }


    public Result updateUserIsDriver(String uid, boolean isDriver) {
        String query = "MATCH (n:user {uid: '%s'}) SET n.is_driver = %b RETURN n";
        query = String.format(query, uid, isDriver);
        return this.session.run(query);
    }

    public Result updateUserLocation(String uid, double longitude, double latitude, String street) {
        String query = "MATCH(n: user {uid: '%s'}) SET n.longitude = %f, n.latitude = %f, n.street = \"%s\" RETURN n";
        query = String.format(query, uid, longitude, latitude, street);
        return this.session.run(query);
    }

    public Result getRoad(String roadName) {
        String query = "MATCH (n :road) where n.name='%s' RETURN n";
        query = String.format(query, roadName);
        return this.session.run(query);
    }

    public Result createRoad(String roadName, boolean has_traffic) {
        String query = "CREATE (n: road {name: '%s', has_traffic: %b}) RETURN n";
        query = String.format(query, roadName, has_traffic);
        return this.session.run(query);
    }

    public Result updateRoad(String roadName, boolean has_traffic) {
        String query = "MATCH (n:road {name: '%s'}) SET n.has_traffic = %b RETURN n";
        query = String.format(query, roadName, has_traffic);
        return this.session.run(query);
    }

    public Result createRoute(String roadname1, String roadname2, int travel_time, boolean has_traffic) {
        String query = "MATCH (r1:road {name: '%s'}), (r2:road {name: '%s'}) CREATE (r1) -[r:ROUTE_TO {travel_time: %d, has_traffic: %b}]->(r2) RETURN r.travel_time";
        query = String.format(query, roadname1, roadname2, travel_time, has_traffic);
        return this.session.run(query);
    }

    public Result deleteRoute(String roadname1, String roadname2) {
        String query = "MATCH (r1:road {name: '%s'})-[r:ROUTE_TO]->(r2:road {name: '%s'}) DELETE r RETURN COUNT(r) AS numDeletedRoutes";
        query = String.format(query, roadname1, roadname2);
        return this.session.run(query);
    }

    public Result getAllDriversInRadius(Double radius, Double longitude, Double latitude) {
        String query = "MATCH (n: user {is_driver: true })"
            + " WITH point({longitude: n.longitude, latitude: n.latitude}) AS driverPoint,"
            + " point({longitude: %f, latitude: %f}) AS userPoint, n"
            + " WHERE round(point.distance(driverPoint, userPoint)) < %f"
            + " RETURN n.uid, driverPoint, userPoint, n.longitude, n.latitude, n.street";
        query = String.format(query, longitude, latitude, radius);
        return this.session.run(query);
    }

    public Result getNavigationPath(String startStreet, String destinationStreet){
        try {
            String query = "CALL gds.graph.exists('routeGraph')";
            Result result = this.session.run(query);
            Boolean exists = false;
            for (Record res: result.list()){
                exists = Boolean.valueOf(res.get("exists").toString());
                System.out.println(exists);
            }
            if (!exists){
                query = "CALL gds.graph.create('routeGraph', 'road', 'ROUTE_TO', {relationshipProperties:['travel_time']})";
                this.session.run(query);
                System.out.println("Creating routeGraph for the first time");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String query = "MATCH (source: road {name: %s}), (target: road {name: %s})"
            + " CALL gds.shortestPath.dijkstra.stream('routeGraph',"
            + " {sourceNode: source, targetNode: target, relationshipWeightProperty: 'travel_time'})"
            + " YIELD index, sourceNode, targetNode, totalCost, nodeIds, costs, path"
            + " RETURN index, totalCost, [nodeId IN nodeIds | gds.util.asNode(nodeId).name] AS roadNames, "
            + " [nodeId IN nodeIds | gds.util.asNode(nodeId).has_traffic] AS roadTraffics, costs";
        query = String.format(query, startStreet, destinationStreet);
        System.out.println(query);
        return this.session.run(query);
    }
}
