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
            // 取得结果集元数据
            ResultSetMetaData rsmd = rsWrapper.getRs().getMetaData();
            // 取得结果集所包含的列数
            int numCols = rsmd.getColumnCount();
            // 显示列标头
            for (int i = 1; i <= numCols; i++) {
                if (i > 1) {
                    System.out.print(",");
                }
                System.out.print(rsmd.getColumnLabel(i));
            }
            System.out.println("");

            // 实现显示结果集的逻辑
            while (rsWrapper.getRs().next()) {
                // 遍历每一行数据并打印
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
        // 读取 YAML 配置文件
        yaml = new Yaml();
        try (FileReader reader = new FileReader(new File(filePath))) {
            Object dataConfig = yaml.load(reader);

            if (dataConfig == null) {
                System.out.println("Open config file: " + filePath + " failed.");
                System.exit(1);
            }

            // 加载 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 确保 dataConfig 是一个 Map
            if (!(dataConfig instanceof Map)) {
                System.out.println("Config file format is incorrect. Expected a Map.");
                System.exit(1);
            }

            Map<String, Object> configMap = (Map<String, Object>) dataConfig;

            // 获取数据库配置
            Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) configMap.get("database"));

            // 安全获取配置信息
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

            // todo passwd 未来需要加密

            // 建立连接
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
            //执行查询
            rsWrapper.setRs(stmt.executeQuery());

            System.out.println("query success!");
            displayResultSet(rsWrapper);
            // 关闭语句
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
                // 关闭语句
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
                // 关闭语句
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
                // 关闭语句
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

            // 获取列数
            int colNumber = rsWrapper.getRs().getMetaData().getColumnCount();

            // 获取列类型
            for (int i = 1; i <= colNumber; i++) {
                int type = rsWrapper.getRs().getMetaData().getColumnType(i);
                colTypes.add(type);
            }

            // 显示列标头
            for (int i = 1; i <= colNumber; i++) {
                System.out.printf("%-30s", rsWrapper.getRs().getMetaData().getColumnLabel(i));
                //System.out.print(rsWrapper.getRs().getMetaData().getColumnLabel(i));
            }
            System.out.println("");

            // 处理每一行记录
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
            // 关闭语句
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
            // 确保 dataConfig 是一个 Map
            if (!(dataConfig instanceof Map)) {
                System.out.println("Config file format is incorrect. Expected a Map.");
                return;
            }

            Map<String, Object> configMap = (Map<String, Object>) dataConfig;

            // 获取数据库配置
            Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) configMap.get("database"));

            // 安全获取配置信息
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

    /* 显示结果集
     * @param rs 结果集对象
     * @throws SQLException 异常 */
    public void displayResultSet(ResultSetWrapper rs){
        if (rs == null) {
            System.err.println("ResultSet is null. No data to display.");
            return;
        }
        try {
            // 取得结果集元数据
            ResultSetMetaData rsmd = rs.getRs().getMetaData();
            // 取得结果集所包含的列数
            int numCols = rsmd.getColumnCount();
            // 显示列标头
            for (int i = 1; i <= numCols; i++) {
                if (i > 1) {
                    System.out.print(",");
                }
                System.out.print(rsmd.getColumnLabel(i));
            }
            System.out.println("");

            // 实现显示结果集的逻辑
            while (rs.getRs().next()) {
                // 遍历每一行数据并打印
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
    // 关闭 ResultSet
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

