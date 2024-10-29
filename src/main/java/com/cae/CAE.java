package com.cae;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.yaml.snakeyaml.Yaml;

public class CAE {
    private static final String JDBC_DRIVER = "dm.jdbc.driver.DmDriver";
    private Connection conn;
    private Yaml yaml;
    private PreparedStatement stmt;

    public CAE(String filePath) {
        // �1�7�1�7�0�0 YAML �1�7�1�7�1�7�1�7�1�7�0�4�1�7
        yaml = new Yaml();
        try (FileReader reader = new FileReader(new File(filePath))) {
            Object dataConfig = yaml.load(reader);
            if (dataConfig == null) {
                System.out.println("Open config file: " + filePath + " failed.");
                System.exit(1);
            }

            // �1�7�1�7�1�7�1�7 JDBC �1�7�1�7�1�7�1�7
            Class.forName(JDBC_DRIVER);

            // �0�2�1�7�1�7 dataConfig �1�7�1�7�0�5�1�7�1�7 Map
            if (!(dataConfig instanceof Map)) {
                System.out.println("Config file format is incorrect. Expected a Map.");
                System.exit(1);
            }

            Map<String, Object> configMap = (Map<String, Object>) dataConfig;

            // �1�7�1�7�0�0�1�7�1�7�1�7�1�3�1�7�1�7�1�7�1�7�1�7
            Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) configMap.get("database"));

            // �1�7�1�7�0�0�1�7�1�7�0�0�1�7�1�7�1�7�1�7�1�7�1�7�0�4
            String server = databaseConfig.map(m -> m.get("server")).orElse(null);
            String username = databaseConfig.map(m -> m.get("username")).orElse(null);
            String password = databaseConfig.map(m -> m.get("passwd")).orElse(null);

            if (server == null || username == null || password == null) {
                System.out.println("Missing required configuration in the config file.");
                System.exit(1);
            }

            // �1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7
            String url = "jdbc:dm://" + server;
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(true);
            System.out.println("========== JDBC: connect to server success! ==========");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[FAIL]conn database�1�7�1�7" + e.getMessage());
        }
    }

    public boolean Query(String sql,ResultSetWrapper rsWrapper) {

        System.out.println("-------------- Query --------------");

        if (!isValidSQLCommand(sql, "select")) {
            System.out.println("illegal statement.");
            return false;
        }

        try {
            stmt = conn.prepareStatement(sql);
            //�0�4�1�7�ӄ1�7�0�9
            rsWrapper.setRs(stmt.executeQuery());
            System.out.println("query success!");
            return true;
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean Update(String sql) {
        System.out.println("-------------- Update --------------");

        if (!isValidSQLCommand(sql, "update")) {
            System.out.println("illegal statement.");
            return false;
        }

        try {
            stmt = conn.prepareStatement(sql);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("update success!");
                // �1�7�1�9�1�7�1�7�1�7�1�7
                stmt.close();
                return true;
            } else {
                System.out.println("no rows affected.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean Delete(String sql) {
        System.out.println("-------------- Delete --------------");

        if (!isValidSQLCommand(sql, "delete")) {
            System.out.println("illegal statement.");
            return false;
        }

        try {
            stmt = conn.prepareStatement(sql);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("delete success!");
                // �1�7�1�9�1�7�1�7�1�7�1�7
                stmt.close();
                return true;
            } else {
                System.out.println("no rows affected.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean Insert(String sql) {
        System.out.println("-------------- Insert --------------");
        if (!isValidSQLCommand(sql, "insert")) {
            System.out.println("illegal statement.");
            return false;
        }

        try {
            stmt = conn.prepareStatement(sql);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("insert success!");
                // �1�7�1�9�1�7�1�7�1�7�1�7
                stmt.close();
                return true;
            } else {
                System.out.println("no rows affected.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* �1�7�1�7�0�5�1�7�1�7�1�7�1�7�1�7
     * @param rs �1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7
     * @throws SQLException �1�7�4�4 */
    public void Display(ResultSetWrapper rsWrapper) {
        if (rsWrapper.getRs() == null) {
            System.err.println("ResultSet is null. No data to display.");
            return;
        }
        try {
            List<Integer> colTypes = new ArrayList<>();
            // �0�0�1�7�0�5�1�7�1�7�1�7�1�7�0�6�1�7�1�7�1�7�1�7
            ResultSetMetaData rsmd = rsWrapper.getRs().getMetaData();
            // �0�0�1�7�0�5�1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7�1�7
            int numCols = rsmd.getColumnCount();
            // �1�7�1�7�0�0�1�7�1�7�1�7�1�7�1�7�1�7   // �1�7�1�7�0�5�1�7�҄1�7�0�5
            for (int i = 1; i <= numCols; i++) {
                int type = rsWrapper.getRs().getMetaData().getColumnType(i);
                colTypes.add(type);
                System.out.printf("%-30s", rsmd.getColumnLabel(i));
            }
            System.out.println("");


            // �1�7�1�7�1�7�1�7�0�7�0�5�1�7�ބ1�7�0�4
            while (rsWrapper.getRs().next()) {
                for (int i = 1; i <= numCols; i++) {
                    int type = colTypes.get(i - 1);
                    switch (type) {
                        case java.sql.Types.BIT:
                        case java.sql.Types.TINYINT:
                        case java.sql.Types.SMALLINT:
                        case java.sql.Types.INTEGER:
                        case java.sql.Types.BIGINT:
                            System.out.printf("%-30d", rsWrapper.getRs().getInt(i));
                            break;
                        case java.sql.Types.FLOAT:
                            System.out.printf("%-30.2f", rsWrapper.getRs().getFloat(i));
                            break;
                        case java.sql.Types.DOUBLE:
                            System.out.printf("%-30.2f", rsWrapper.getRs().getDouble(i));
                            break;
                        default:
                            System.out.printf("%-30s", rsWrapper.getRs().getString(i));
                    }
                }
                System.out.println();
            }
                System.out.println("");
            }catch (SQLException e) {
                System.err.println("Error displaying ResultSet: " + e.getMessage());
                e.printStackTrace();
            }
        }

    private boolean isValidSQLCommand(String sql, String type) {
        String trimmedSQL = sql.trim().toLowerCase();
        return trimmedSQL.startsWith(type);
    }

//    public void connectTest(String filePath) {
//        try (FileReader reader = new FileReader(new File(filePath))) {
//            Object dataConfig = yaml.load(reader);
//            if (dataConfig == null) {
//                System.out.println("Open config File: test failed.");
//            }
//            // �0�2�1�7�1�7 dataConfig �1�7�1�7�0�5�1�7�1�7 Map
//            if (!(dataConfig instanceof Map)) {
//                System.out.println("Config file format is incorrect. Expected a Map.");
//                return;
//            }
//
//            Map<String, Object> configMap = (Map<String, Object>) dataConfig;
//
//            // �1�7�1�7�0�0�1�7�1�7�1�7�1�3�1�7�1�7�1�7�1�7�1�7
//            Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) configMap.get("database"));
//
//            // �1�7�1�7�0�0�1�7�1�7�0�0�1�7�1�7�1�7�1�7�1�7�1�7�0�4
//            String server = databaseConfig.map(m -> m.get("server")).orElse(null);
//            String username = databaseConfig.map(m -> m.get("username")).orElse(null);
//
//            if (server == null || username == null) {
//                System.out.println("Missing required configuration in the config file.");
//                return;
//            }
//
//            System.out.println("Server: " + server);
//            System.out.println("Username: " + username);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    // �1�7�1�9�1�7�1�7�1�7�1�7�1�7�1�7  �1�7�1�9�1�7�1�7�1�7�1�7�1�4�1�7�1�7�1�7
    public void setClose(ResultSetWrapper rsWrapper) {
        if (rsWrapper != null) {
            try {
                // �1�7�1�9�1�7�1�7�1�7�1�7
                stmt.close();
                //�1�7�1�9�0�9�1�7�1�7�1�7�1�7
                rsWrapper.getRs().close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //�1�7�1�9�1�7�1�7�1�7�1�7�1�7
    public void connClose() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("========== JDBC: disconnect from server success! ==========");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}

