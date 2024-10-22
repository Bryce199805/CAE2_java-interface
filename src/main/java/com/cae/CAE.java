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

// ===== Private Method

    private void displayResultSetV2(ResultSetWrapper rsWrapper, List<Integer> colTypes) {
        if (rsWrapper == null) {
            System.err.println("ResultSet is null. No data to display.");
            return;
        }
        try {
            // ȡ�ý����Ԫ����
            ResultSetMetaData rsmd = rsWrapper.getRs().getMetaData();
            // ȡ�ý����������������
            int numCols = rsmd.getColumnCount();
            // ��ʾ�б�ͷ
            for (int i = 1; i <= numCols; i++) {
                if (i > 1) {
                    System.out.print(",");
                }
                System.out.print(rsmd.getColumnLabel(i));
            }
            System.out.println("");

            // ʵ����ʾ��������߼�
            while (rsWrapper.getRs().next()) {
                // ����ÿһ�����ݲ���ӡ
                for (int i = 0; i <= numCols; i++) {
                    switch (colTypes.get(i)) {
                        case 0: // string
                            System.out.print(rsWrapper.getRs().getString(i) + " ");
                            break;
                        case 1: // int
                            System.out.print(rsWrapper.getRs().getInt(i) + " ");
                            break;
                        case 2: // float
                            System.out.print(rsWrapper.getRs().getFloat(i) + " ");
                            break;
                        case 3: // double
                            System.out.print(rsWrapper.getRs().getDouble(i) + " ");
                            break;
                    }
                    //System.out.println();
                }
                System.out.println("");
            }
        } catch (SQLException e) {
            System.err.println("Error displaying ResultSet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidSQLCommand(String sql, String type) {
        String trimmedSQL = sql.trim().toLowerCase();
        return trimmedSQL.startsWith(type);
    }

// ===== Public Method

    public CAE(String filePath) {
        // ��ȡ YAML �����ļ�
        yaml = new Yaml();
        try (FileReader reader = new FileReader(new File(filePath))) {
            Object dataConfig = yaml.load(reader);

            if (dataConfig == null) {
                System.out.println("Open config file: " + filePath + " failed.");
                System.exit(1);
            }

            // ���� JDBC ����
            Class.forName(JDBC_DRIVER);

            // ȷ�� dataConfig ��һ�� Map
            if (!(dataConfig instanceof Map)) {
                System.out.println("Config file format is incorrect. Expected a Map.");
                System.exit(1);
            }

            Map<String, Object> configMap = (Map<String, Object>) dataConfig;

            // ��ȡ���ݿ�����
            Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) configMap.get("database"));

            // ��ȫ��ȡ������Ϣ
            String server = databaseConfig.map(m -> m.get("server")).orElse(null);
            String username = databaseConfig.map(m -> m.get("username")).orElse(null);
            String password = databaseConfig.map(m -> m.get("passwd")).orElse(null);
//            System.out.println(server);
//            System.out.println(username);
//            System.out.println(password);

            if (server == null || username == null || password == null) {
                System.out.println("Missing required configuration in the config file.");
                System.exit(1);
            }

            // todo passwd δ����Ҫ����

            // ��������
            String url = "jdbc:dm://" + server;
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(true);
            System.out.println("========== JDBC: connect to server success! ==========");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[FAIL]conn database: " + e.getMessage());
        }
    }

    public void close() {
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

    public boolean Query(String sql, ResultSetWrapper rsWrapper) {
        System.out.println("-------------- Query --------------");
        if (!isValidSQLCommand(sql, "select")) {
            System.out.println("illegal statement.");
            return false;
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            //ִ�в�ѯ
            rsWrapper.setRs(stmt.executeQuery());

            System.out.println("query success!");
            displayResultSet(rsWrapper);
            // �ر����
            stmt.close();
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

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("update success!");
                // �ر����
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

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("delete success!");
                // �ر����
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

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("insert success!");
                // �ر����
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

    public boolean executeSelectQueryV2(String sql, ResultSetWrapper rsWrapper) {
        System.out.println("-------------- Query --------------");
        if (!isValidSQLCommand(sql, "select")) {
            System.out.println("illegal statement.");
            return false;
        }

        List<Integer> colTypes = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            rsWrapper.setRs(stmt.executeQuery());

            // ��ȡ����
            int colNumber = rsWrapper.getRs().getMetaData().getColumnCount();

            // ��ȡ������
            for (int i = 1; i <= colNumber; i++) {
                int type = rsWrapper.getRs().getMetaData().getColumnType(i);
                colTypes.add(type);
            }

            // ��ʾ�б�ͷ
            for (int i = 1; i <= colNumber; i++) {
                System.out.printf("%-30s", rsWrapper.getRs().getMetaData().getColumnLabel(i));
                //System.out.print(rsWrapper.getRs().getMetaData().getColumnLabel(i));
            }
            System.out.println("");

            // ����ÿһ�м�¼
            while (rsWrapper.getRs().next()) {
                for (int i = 1; i <= colNumber; i++) {
                    int type = colTypes.get(i - 1);
                    switch (type) {
                        case java.sql.Types.BIT:
                        case java.sql.Types.TINYINT:
                        case java.sql.Types.SMALLINT:
                        case java.sql.Types.INTEGER:
                        case java.sql.Types.BIGINT:
                            System.out.printf("%-30d", rsWrapper.getRs().getInt(i));
                            //System.out.print(rsWrapper.getRs().getInt(i) + "     ");
                            break;
                        case java.sql.Types.FLOAT:
                            System.out.printf("%-30.2f", rsWrapper.getRs().getFloat(i));
                            //System.out.print(rsWrapper.getRs().getFloat(i) + "     ");
                            break;
                        case java.sql.Types.DOUBLE:
                            System.out.printf("%-30.2f", rsWrapper.getRs().getDouble(i));
                            //System.out.print(rsWrapper.getRs().getDouble(i) + "     ");
                            break;
                        default:
                            System.out.printf("%-30s", rsWrapper.getRs().getString(i));
                            //System.out.print(rsWrapper.getRs().getString(i) + "    ");
                    }
                }
                System.out.println();
            }
            //displayResultSetV2(rsWrapper,colTypes);
            // �ر����
            stmt.close();
            System.out.println("query success!");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void connectTest(String filePath) {
        try (FileReader reader = new FileReader(new File(filePath))) {
            Object dataConfig = yaml.load(reader);
            if (dataConfig == null) {
                System.out.println("Open config File: test failed.");
            }
            // ȷ�� dataConfig ��һ�� Map
            if (!(dataConfig instanceof Map)) {
                System.out.println("Config file format is incorrect. Expected a Map.");
                return;
            }

            Map<String, Object> configMap = (Map<String, Object>) dataConfig;

            // ��ȡ���ݿ�����
            Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) configMap.get("database"));

            // ��ȫ��ȡ������Ϣ
            String server = databaseConfig.map(m -> m.get("server")).orElse(null);
            String username = databaseConfig.map(m -> m.get("username")).orElse(null);

            if (server == null || username == null) {
                System.out.println("Missing required configuration in the config file.");
                return;
            }

            System.out.println("Server: " + server);
            System.out.println("Username: " + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ��ʾ�����
     * @param rs ���������
     * @throws SQLException �쳣 */
    public void displayResultSet(ResultSetWrapper rs){
        if (rs == null) {
            System.err.println("ResultSet is null. No data to display.");
            return;
        }
        try {
            // ȡ�ý����Ԫ����
            ResultSetMetaData rsmd = rs.getRs().getMetaData();
            // ȡ�ý����������������
            int numCols = rsmd.getColumnCount();
            // ��ʾ�б�ͷ
            for (int i = 1; i <= numCols; i++) {
                if (i > 1) {
                    System.out.print(",");
                }
                System.out.print(rsmd.getColumnLabel(i));
            }
            System.out.println("");

            // ʵ����ʾ��������߼�
            while (rs.getRs().next()) {
                // ����ÿһ�����ݲ���ӡ
                for (int i = 1; i <= numCols; i++) {
                    if (i > 1) {
                        System.out.print("\t");
                    }
                    System.out.print(rs.getRs().getString(i));
                }
                System.out.println("");
            }
        } catch (SQLException e) {
            System.err.println("Error displaying ResultSet: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // �ر� ResultSet
    public void closeResultSet(ResultSetWrapper rs) {
        if (rs != null) {
            try {
                rs.getRs().close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

