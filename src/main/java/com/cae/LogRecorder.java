package com.cae;

import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.net.util.SubnetUtils;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.select.Select;


public class LogRecorder {
    private static final String JDBC_DRIVER = "dm.jdbc.driver.DmDriver";
    private Connection conn;
    private Yaml yaml;
    private String m_username;
    private String d_username;
    private String ip;
    private String server;
    private String CIDR;
    private Map<String, Object> configMap;
    private static LogRecorder instance;
    private boolean enable;

    private LogRecorder(String filePath){
        this.readYaml(filePath);
        this.enable = readEnable();
        if(this.enable){
            this.readServer();
            this.readCIDR();
            this.d_username = this.readUserName("database");
            this.m_username = this.readUserName("fileSystem");
            this.ip =  this.getIP(CIDR);

            String url = "jdbc:dm://"+this.server;
            try {
                this.conn = DriverManager.getConnection(url, "loguser", "SYSDBA1234");
                this.conn.setAutoCommit(true);
                System.out.println("成功连接");
            } catch (Exception e) {
                System.err.println("[No Need / Fail] conn database for logUser:" + e.getMessage());
            }
        }
    }

    // 静态方法，CAE 类可以通过此方法访问 Logger 对象
    public static synchronized LogRecorder getLogger(String filePath) {
        if (instance == null) {
            instance = new LogRecorder(filePath);
        }
        return instance;
    }

//    // 静态方法，CAE 类可以通过此方法访问 Logger 对象
//    public static LogRecorder getLogger() {
//        return LoggerHolder.INSTANCE;
//    }
//
//    // 使用静态内部类单例模式
//    private static class LoggerHolder {
//        private static final LogRecorder INSTANCE = new LogRecorder();
//    }

    private boolean readEnable() {
        Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) this.configMap.get("log"));
        String enableStr = databaseConfig.map(m -> m.get("enable")).orElse(null);

        if (enableStr == null) {
            System.err.println("Missing 'enable' configuration in the config file. Defaulting to false.");
            return false; // 如果没有配置 enable，则返回 false
        }

        return Boolean.parseBoolean(enableStr.trim().toLowerCase());
    }

    private void readYaml(String filePath){
        // 读取 YAML 配置文件
        this.yaml = new Yaml();
        try (FileReader reader = new FileReader(new File(filePath))) {
            Object dataConfig = this.yaml.load(reader);
            if (dataConfig == null) {
                System.err.println("Open config file: " + filePath + " failed.");
            }

            // 加载 JDBC 驱动
            Class.forName(this.JDBC_DRIVER);

            // 确保 dataConfig 是一个 Map
            if (!(dataConfig instanceof Map)) {
                System.err.println("Config file format is incorrect. Expected a Map.");
            }
            this.configMap = (Map<String, Object>) dataConfig;

        }catch(Exception e){
            System.err.println("[No Need / Fail] configMap:" + e.getMessage());
        }
    }

    private void readCIDR(){
        Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) this.configMap.get("log"));
        this.CIDR = databaseConfig.map(m -> m.get("CIDR")).orElse(null);
        if (this.CIDR == null) {
            System.err.println("Missing required configuration in the config file.");
        }
    }

    private String readUserName(String system){
        String username = null;
        // 获取数据库配置
        Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) this.configMap.get(system));
        // 安全获取配置信息
        username = databaseConfig.map(m -> m.get("username")).orElse(null);
        if (username == null) {
            System.err.println("Missing required configuration in the config file.");
        }
        return username;
    }

    private void readServer(){
        Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) this.configMap.get("database"));
        this.server = databaseConfig.map(m -> m.get("server")).orElse(null);
        if (this.server == null) {
            System.err.println("Missing required configuration in the config file.");
        }
    }

    /**
     * 插入达梦日志记录
     * @param sql
     * @return
     */
    boolean insertRecord(String sql, String operation, int result){
        if(this.enable){
            System.out.println("======================日志更新======================");
            if (this.ip == null) {
                System.err.println("No valid IP found in the specified CIDR range.");
                //return false; // 或其他处理逻辑
            }

            // Parse SQL
            List<String[]> names = this.parseSQL(sql);

            // Combine schemas and tables into the required format
            String schemas = names.stream()
                    .map(pair -> pair[0] != null ? "'" + pair[0] + "'" : "NULL")
                    .distinct() // Avoid duplicate schemas
                    .collect(Collectors.joining(",", "(", ")"));

            String tables = names.stream()
                    .map(pair -> "'" + pair[1] + "'")
                    .distinct() // Avoid duplicate tables
                    .collect(Collectors.joining(",", "(", ")"));

            //System.out.println("schemas = " + schemas);
            //System.out.println("tables = " + tables);

            String encodedOperation = new String(operation.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            String insert_sql = String.format(
                    "insert into \"LOGS\".\"LOG\" ( \"USER_NAME\", \"IP_ADDR\", \"SOURCE\", \"OPERATION\", \"TIME\", \"SCHEMAS\", \"TABLES\", \"RESULT\") " +
                            "values ( '%s', '%s', 'java接口', '%s', SYSTIMESTAMP, LOGS.TABLES%s, LOGS.TABLES%s, %d);",
                    this.d_username, this.ip, encodedOperation, schemas, tables, result
            );
            System.out.println(insert_sql);

            if(!this.insert_(insert_sql)){
                System.err.println("日志更新失败！!");
                return false;
            };
            System.out.println("日志更新成功！");
            return true;
        }else {
            System.out.println("不更新日志！");
            return false;
        }

    }

    /**
     * 插入MiniO日志记录
     * @return
     */
    boolean insertRecord( String operation, String SchameName, String tableName, int result){
        if(this.enable){
            System.out.println("======================日志更新=======================");
            //getUserName
            //String userName = this.readUserName(filePath,"fileSystem");

            //getIP
            //String ip = this.getIP(this.readCIDR(filePath));
            if (this.ip == null) {
                System.err.println("No valid IP found in the specified CIDR range.");
                //return false; // 或其他处理逻辑
            }

            String encodedOperation = new String(operation.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            String insert_sql = String.format(
                    "insert into \"LOGS\".\"LOG\" ( \"USER_NAME\", \"IP_ADDR\", \"SOURCE\", \"OPERATION\", \"TIME\", \"SCHEMAS\", \"TABLES\", \"RESULT\") " +
                            "values ( '%s', '%s', 'java接口', '%s', SYSTIMESTAMP, LOGS.TABLES('%s'), LOGS.TABLES('%s'), %d);",
                    this.m_username, this.ip, encodedOperation, SchameName, tableName, result
            );
            System.out.println(insert_sql);

            if(!this.insert_(insert_sql)){
                System.err.println("日志更新失败！!");
                return false;
            };
            System.out.println("日志更新成功！");
            return true;
        }else{
            System.out.println("不更新日志！");
            return false;
        }
    }

    //往日志表中插入相关数据
    private boolean insert_(String insert_sql){
        System.out.println("--------- Insert In DM ---------");
        try {
            PreparedStatement stmt = this.conn.prepareStatement(insert_sql);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DM insert success!");
                // 关闭语句
                stmt.close();
                return true;
            } else {
                System.out.println("no rows affected.");
                return false;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            System.err.println("Exception occurred: " + e.getMessage());
            return false;
        }
    }

    private String getIP(String CIDR){
        String ip = null;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.getHostAddress().contains(".")) {
                        ip = inetAddress.getHostAddress();
                        //System.out.println("IP 地址: " + ip);

                        // 使用 SubnetUtils 检查是否是局域网
                        SubnetUtils subnetUtils = new SubnetUtils(CIDR);
                        if (subnetUtils.getInfo().isInRange(ip)) {
                            //System.out.println("局域网 IP: " + ip);
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return ip;
    }

    private List<String[]> parseSQL(String sql) {
        List<String[]> schemaTableList = new ArrayList<>();
        try {
            // 解析SQL语句
            Statement statement = CCJSqlParserUtil.parse(sql);

            // 检查 SQL 语句类型
            if (statement instanceof Insert) {
                Insert insertStatement = (Insert) statement;
                Table table = insertStatement.getTable();
                this.addTableInfoToList(table,schemaTableList);
            }
            else if (statement instanceof Select) {
                // 解析SELECT语句
                Select selectStatement = (Select) CCJSqlParserUtil.parse(sql);

                // 获取PlainSelect对象
                PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();

                // 获取FROM子句中的表
                FromItem fromItem = plainSelect.getFromItem();
                if (fromItem instanceof Table) {
                    Table table = (Table) fromItem;
                    this.addTableInfoToList(table,schemaTableList);
                }

                // 获取JOIN子句中的表（如果有的话）
                List<Join> joins = plainSelect.getJoins();
                if (joins != null) {
                    for (Join join : joins) {
                        FromItem joinItem = join.getRightItem();
                        if (joinItem instanceof Table) {
                            Table table = (Table) joinItem;
                            this.addTableInfoToList(table,schemaTableList);
                        }
                    }
                }
            }
            else if (statement instanceof Update) {
                Update updateStatement = (Update) statement;
                Table table = updateStatement.getTable();
                this.addTableInfoToList(table,schemaTableList);
            }
            else if (statement instanceof Delete) {
                Delete deleteStatement = (Delete) statement;
                Table table = deleteStatement.getTable();
                this.addTableInfoToList(table,schemaTableList);
            }
            else {
                System.err.println("Unsupported SQL type");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("[FAIL] ParseSQL ERROR：" + e.getMessage());
        }
        return schemaTableList;
    }

    private void addTableInfoToList(Table table, List<String[]> list) {
        if (table != null) {
            String schema = table.getSchemaName();
            String tableName = table.getName();
            //System.out.println("schema = " + schema);
            //System.out.println("tableName = " + tableName);
            list.add(new String[]{schema, tableName});
        }
    }
}
