package com.cae;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.yaml.snakeyaml.Yaml;

public class DamengDB {
    private static final String JDBC_DRIVER = "dm.jdbc.driver.DmDriver";
    private Connection conn;
    private Yaml yaml;

    public DamengDB(String filePath) {
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
            System.out.println(server);
            System.out.println(username);
            System.out.println(password);

            if (server == null || username == null || password == null) {
                System.out.println("Missing required configuration in the config file.");
                System.exit(1);
            }

//            // 获取配置信息
//            String server = (String) ((Map)dataConfig).get("database").get("server");
//            String username = (String) ((Map)dataConfig).get("database").get("username");
//            String password = (String) ((Map)dataConfig).get("database").get("passwd");

            // 建立连接
            String url = "jdbc:dm://" + server;
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("========== JDBC: connect to server success! ==========");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
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

    public boolean query(String sql, List<List<String>> res) {
        System.out.println("---------- Query ----------");
        if (!isValidSQLCommand(sql, "select")) {
            System.out.println("illegal statement.");
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // 获取列数
            int colNumber = rs.getMetaData().getColumnCount();

            // 处理每一行记录
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= colNumber; i++) {
                    row.add(rs.getString(i));
                }
                res.add(row);
            }

            System.out.println("query success!");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean query(String sql, List<List<DBVariant>> res, List<Integer> colTypes) {
        System.out.println("---------- Query ----------");
        if (!isValidSQLCommand(sql, "select")) {
            System.out.println("illegal statement.");
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // 获取列数
            int colNumber = rs.getMetaData().getColumnCount();

            // 获取列类型
            for (int i = 1; i <= colNumber; i++) {
                int type = rs.getMetaData().getColumnType(i);
                colTypes.add(type);
            }

            // 处理每一行记录
            while (rs.next()) {
                List<DBVariant> row = new ArrayList<>();
                for (int i = 1; i <= colNumber; i++) {
                    int type = colTypes.get(i - 1);
                    switch (type) {
                        case java.sql.Types.BIT:
                        case java.sql.Types.TINYINT:
                        case java.sql.Types.SMALLINT:
                        case java.sql.Types.INTEGER:
                        case java.sql.Types.BIGINT:
                            row.add(new DBVariant(rs.getInt(i)));
                            break;
                        case java.sql.Types.FLOAT:
                            row.add(new DBVariant(rs.getFloat(i)));
                            break;
                        case java.sql.Types.DOUBLE:
                            row.add(new DBVariant(rs.getDouble(i)));
                            break;
                        default:
                            row.add(new DBVariant(rs.getString(i)));
                    }
                }
                res.add(row);
            }

            System.out.println("query success!");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String sql) {
        System.out.println("---------- Delete ----------");
        if (!isValidSQLCommand(sql, "delete")) {
            System.out.println("illegal statement.");
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("delete success!");
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

    public boolean update(String sql) {
        System.out.println("---------- Update ----------");
        if (!isValidSQLCommand(sql, "update")) {
            System.out.println("illegal statement.");
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("update success!");
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

    public boolean insert(String sql) {
        System.out.println("---------- Insert ----------");
        if (!isValidSQLCommand(sql, "insert")) {
            System.out.println("illegal statement.");
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("insert success!");
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

    public void printResult(List<List<String>> res) {
        for (List<String> row : res) {
            for (String col : row) {
                System.out.print(col + " ");
            }
            System.out.println();
        }
    }

    public void printResult(List<List<DBVariant>> res, List<Integer> colTypes) {
        for (List<DBVariant> row : res) {
            for (int i = 0; i < row.size(); i++) {
                switch (colTypes.get(i)) {
                    case 0: // string
                        System.out.print(row.get(i).asTypeString() + " ");
                        break;
                    case 1: // int
                        System.out.print(row.get(i).asTypeInteger() + " ");
                        break;
                    case 2: // float
                        System.out.print(row.get(i).asTypeFloat() + " ");
                        break;
                    case 3: // double
                        System.out.print(row.get(i).asTypeDouble() + " ");
                        break;
                }
            }
            System.out.println();
        }
    }

    private boolean isValidSQLCommand(String sql, String type) {
        String trimmedSQL = sql.trim().toLowerCase();
        return trimmedSQL.startsWith(type);
    }

    public void connectTest() {
        try (FileReader reader = new FileReader(new File("../config.yaml"))) {
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
            String host = databaseConfig.map(m -> m.get("host")).orElse(null);

            if (server == null || host == null) {
                System.out.println("Missing required configuration in the config file.");
                return;
            }

            System.out.println("Server: " + server);
            System.out.println("Host: " + host);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

