package com.jstarts.codegrapher.db;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.falkordb.Driver;
import com.falkordb.FalkorDB;
import com.falkordb.Graph;
import com.falkordb.ResultSet;

public class FalkorDBClient implements AutoCloseable {
    private final Driver driver;
    private final Graph graph;

    public FalkorDBClient(Driver driver, Graph graph) {
        this.driver = driver;
        this.graph = graph;
    }

    public static FalkorDBClient connect(String host, int port, String graphId) {
        Driver driver = FalkorDB.driver(host, port);
        Graph graph = driver.graph(graphId);

        return new FalkorDBClient(driver, graph);
    }

    public ResultSet executeQuery(String query) {
        return executeQuery(query, Collections.emptyMap());
    }

    public ResultSet executeQuery(String query, Map<String,Object> params) {
        return graph.query(query, params);
    }

    public <R> R withGraph(Function<Graph, R> operation) {
        return operation.apply(graph);
    }

    @Override
    public void close() {
        try {
            driver.close();
        } catch (Exception e) {
            throw new RuntimeException("Error closing FalkorDB driver: ", e);
        }
    }
    public static <R> R use(String host, int port, String graphId, Function<FalkorDBClient, R> operation) {
        try (FalkorDBClient client = FalkorDBClient.connect(host, port, graphId)) {
            return operation.apply(client);
        } catch (Exception e) {
            throw new RuntimeException("Error using FalkorDBClient", e);
        }
    }




    
}
