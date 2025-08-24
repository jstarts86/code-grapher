package com.jstarts.codegrapher.falkordb;

import com.falkordb.Driver;
import com.falkordb.FalkorDB;
import com.falkordb.Graph;

public class FalkorConfig {

    Driver driver = FalkorDB.driver("localhost", 6379);
    Graph graph = driver.graph("social");
}
