package com.jstarts.codegrapher.falkordb;

import com.falkordb.Driver;
import com.falkordb.FalkorDB;
import com.falkordb.Graph;
import com.falkordb.Record;
import com.falkordb.ResultSet;

import java.util.Collections;
import java.util.Map;

public class FalkorConfig {

    private final Driver driver;
    private final Graph graph;

    public FalkorConfig(String host, int port, String graphId) {
        this.driver = FalkorDB.driver(host, port);
        this.graph = driver.graph(graphId);
    }

    public void close() {
        try {
            if (driver != null) {
                driver.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing FalkorDB driver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String query) {
        return executeQuery(query, Collections.emptyMap());
    }

    public ResultSet executeQuery(String query, Map<String, Object> params) {
        return graph.query(query, params);
    }

    public static void main(String[] args) {
        FalkorConfig falkorConfig = null;
        try {
            falkorConfig = new FalkorConfig("localhost", 6379, "social");

            falkorConfig.executeQuery("MATCH (n) DETACH DELETE n");

            System.out.println("Create nodes");
            falkorConfig.executeQuery("CREATE (:person {name: 'John Doe', age: 30})");
            falkorConfig.executeQuery("CREATE (:person {name: 'Jane Doe', age: 28})");

            System.out.println("Create edge");
            falkorConfig.executeQuery("MATCH (a:person {name: 'John Doe'}), (b:person {name: 'Jane Doe'}) CREATE (a)-[:KNOWS]->(b)");

            System.out.println("Query the Graph");
            ResultSet resultSet = falkorConfig.executeQuery("MATCH (a)-[r]->(b) RETURN a, r, b");

            for (Record record : resultSet) {
                System.out.println(record.toString());
            }
        } catch (Exception e) {
            System.err.println("An error occurred while connecting to or querying FalkorDB: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Please ensure the FalkorDB/Redis server is running on localhost:6379.");
        }
        finally {
            if (falkorConfig != null) {
                falkorConfig.close();
            }
        }
    }
}
