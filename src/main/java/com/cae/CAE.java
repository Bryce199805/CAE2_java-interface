package com.cae;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.select.Select;

import io.minio.*;

import org.yaml.snakeyaml.Yaml;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CAE {
    private static final String JDBC_DRIVER = "dm.jdbc.driver.DmDriver";
    private Connection conn;
    private Connection conn_db;
    private Yaml yaml;
    private PreparedStatement stmt;
    private static final Logger logger = Logger.getLogger(CAE.class.getName());  //日志记录器
    private MinioClient fileClient;
    private static final Map<String, Map<String, Set<String>>> fileDBMap = new HashMap<>();
    private static final Map<String,Map<String,String>> fileIDMap = new HashMap<>();

    static {
        // fileIDMap 初始化
        fileIDMap.put("HULL_MODEL_AND_INFORMATION_DB",
                Collections.singletonMap("HULL_PARAMETER_INFO", "HULL_ID"));

        // fileDBMap 初始化
        fileDBMap.put("HULL_MODEL_AND_INFORMATION_DB",
                Collections.singletonMap("HULL_PARAMETER_INFO",
                        new HashSet<>(Arrays.asList("TRANSVERSE_AREA_CURVE", "HULL_3D_MODEL", "OFFSETS_TABLE"))
                ));
    }

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

            System.out.println(hmacSHA256(password,"SYSDBA"));

            if (server == null || username == null || password == null) {
                System.out.println("Missing required configuration in the config file.");
                System.exit(1);
            }

            // 建立达梦的连接
            String url = "jdbc:dm://" + server;
            conn = DriverManager.getConnection(url, username, hmacSHA256(password,"SYSDBA"));
            conn.setAutoCommit(true);
            System.out.println("========== JDBC: connect to DM server success! ==========");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[FAIL]conn database：" + e.getMessage());
        }
    }

    /**
     * HMAC-SHA256，Hash-based Message Authentication Code，是一种基于哈希函数和密钥的消息认证码算法，用于确保消息的完整性和认证。
     * <p>
     * 输入：待加密的密码
     * 输出：与SHA256一致
     * 应用：密码管理、数字签名、文件完整性校验
     * 安全性：★★★☆☆
     *
     * @param plainString 明文密码
     * @param key         秘钥
     * @return cipherString 密文
     */
    private static String hmacSHA256(String plainString, String key) {
        String cipherString = null;
        try {
            // 指定算法
            String algorithm = "HmacSHA256";
            // 创建密钥规范
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            // 获取Mac对象实例
            Mac mac = Mac.getInstance(algorithm);
            // 初始化mac
            mac.init(secretKeySpec);
            // 计算mac
            byte[] macBytes = mac.doFinal(plainString.getBytes(StandardCharsets.UTF_8));
            // 输出为16进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : macBytes) {
                sb.append(String.format("%02x", b));
            }
            cipherString = sb.toString().substring(0, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherString;
    }


    public CAE(String FilePath,boolean xxx) {
        // 读取 YAML 配置文件
        yaml = new Yaml();
        try (FileReader reader = new FileReader(new File(FilePath))) {
            Object dataConfig = yaml.load(reader);
            if (dataConfig == null) {
                System.out.println("Open config file: " + FilePath + " failed.");
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
            Map<String, String> databaseConfig = (Map<String, String>) configMap.get("database");
            // 获取 CAE-FILE 配置信息
            Map<String, String> fileConfig = (Map<String, String>) configMap.get("CAE-FILE");
            if (fileConfig == null) {
                System.out.println("Missing 'endpoint' configuration in the config file.");
                System.exit(1);
            }

            // 安全获取配置信息
            String server = databaseConfig.get("server");
            String username = databaseConfig.get("username");
            String password = databaseConfig.get("passwd");

            if (server == null || username == null || password == null) {
                System.out.println("Missing required configuration in the config file.");
                System.exit(1);
            }

            // 建立达梦的连接
            String url = "jdbc:dm://" + server;
            conn_db = DriverManager.getConnection(url, username, hmacSHA256(password,"SYSDBA"));
            conn_db.setAutoCommit(true);
            System.out.println("========== JDBC: connect to DM server success! ==========");

            // 获取具体的 CAE-FILE 配置信息
            String endpoint = fileConfig.get("endpoint");
            String file_username = fileConfig.get("username");
            String file_password = fileConfig.get("passwd");

            if (endpoint == null || file_username == null || file_password == null) {
                System.out.println("Missing required configuration in the config file.");
                System.exit(1);
            }
            // 检查并补全 endpoint 的协议前缀
            if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
                endpoint = "http://" + endpoint; // 默认为 http，如果需要 https，请更改为 https://
            }
            System.out.println(hmacSHA256(file_password,"fileadmin"));
            //建立CAE-FILE的连接，并返回一个fileClient实例
            fileClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(file_username, hmacSHA256(file_password,"fileadmin"))
                    .build();

            System.out.println(fileDBMap);
            System.out.println(fileIDMap);

            System.out.println("========== JDBC: connect to CAE_FILE server success! ==========");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[FAIL]conn database：" + e.getMessage());
        }
    }

    public boolean DeleteRecord(String dbName,String tableName,String id){
        //删除一条记录对应的所有文件
        if(fileDBMap.get(dbName).containsKey(tableName)){
            // 从 fileDBMap 获取文件字段集合
            Map<String, Set<String>> tableMap = fileDBMap.get(dbName);
            if (tableMap != null) {
                Set<String> fields = tableMap.get(tableName);
                if (fields != null) {
                    // 遍历字段，执行删除操作
                    for (String field : fields) {
                        // 生成 CAEPath
                        String CAEPath = sql2CAEPath(dbName, tableName, field, id);
                        // 删除文件
                        delete(CAEPath);
                    }
                    //删除记录
                    deleterecord(dbName, tableName, id);
                    return true;
                } else {
                    System.out.println("No fields found for table: " + tableName);
                    return false;
                }
            } else {
                System.out.println("No data found for dbName: " + dbName);
                return false;
            }
        }
        System.out.println("No FileData for this record!");
        return false;
    }

    //在DM数据库中删除一条记录
    private boolean deleterecord(String dbName,String tableName,String id){
        String idField = getIDField(dbName, tableName);
        String delete_sql = String.format("DELETE FROM %s.%s WHERE %s = '%s';",dbName, tableName,idField,id);
        try {
            stmt = conn_db.prepareStatement(delete_sql);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DM delete this record success!");
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

    /**
     * 根据数据库名称、表名、字段名和 ID 删除单个文件
     *
     * @param dbName 数据库名称
     * @param tableName 表名称
     * @param field 字段名称
     * @param id 主键 ID
     * @return 是否删除成功
     */
    public boolean DeleteFile(String dbName, String tableName, String field, String id){
        System.out.println("--------------Delete File--------------");
        String CAEPath = null;
        try{
            if(checkFileField(dbName,tableName,field)){
                //定制sql语句，解析得到新的CAEPath
                CAEPath = sql2CAEPath(dbName,tableName,field,id);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("对应库名，表名，文件字段名有误");
        }
        //删除文件 并 清空字段
        return delete(CAEPath) && updateField(null,dbName,tableName,id,field) ;
    }

    private boolean delete(String CAEPath) {
        //获取对应ObjectName
        String bucketName = trimmedCAEPath(CAEPath)[0];
        String objectName = trimmedCAEPath(CAEPath)[1];

        try {
            fileClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            System.out.println("Object deleted: " + objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 上传单个文件到 CAE_FILE 并同时更新文件路径
     * @param localPath 本地文件路径，表示要上传的文件。
     */
    public boolean UploadFile(String localPath, String dbName, String tableName, String field, String id){
        System.out.println("--------------UpLoad File--------------");
        String CAEPath = null;
        try{
            if(checkFileField(dbName,tableName,field)){
                try{
                    //更新文件路径
                    if(updateField(localPath,dbName,tableName,id,field)){
                        //定制sql语句，解析得到新的CAEPath
                        CAEPath = sql2CAEPath(dbName,tableName,field,id);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println("DM 数据库更新文件路径未成功！");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("对应库名，表名，文件字段名有误！");
        }

        return upload(CAEPath,localPath,dbName,tableName,id);
    }

    private boolean upload(String CAEPath,String localPath,String dbName, String tableName, String id){
        String bucketName = null;
        String objectName = null;
        //初始字段内容为空或者空串的情况，初始化桶名和objectName
        if(CAEPath == null || CAEPath == " "){
            bucketName = dbName.toLowerCase().replace("_", "-");
            File file = new File(localPath);
            String fileName = file.getName();  // 获取文件名，包括扩展名，如 "file.jpg"
            objectName = tableName + "/" + id + "/" + fileName;
        }else{
            //获取对应ObjectName
            bucketName = trimmedCAEPath(CAEPath)[0];
            objectName = trimmedCAEPath(CAEPath)[1];
        }

        // 确保桶存在
        try {
            if (!fileClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                fileClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 上传文件
        try {
            fileClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(localPath)
                            .build()
            );
            System.out.println("UPLOAD FILE SUCCESS：" + CAEPath + " -> " + localPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // 如果 localPath 为 null，不执行文件保存，直接返回 false
            return false;
        }
    }

    /**
     * 解析得到要更新字段内容，并上传至DM数据库
     * @return  成功？失败
     */
    public boolean updateField(String localPath, String dbName, String tableName, String id,String field) {
        String dbName1 = dbName.toLowerCase().replace("_", "-");
        String resultPath = null;
        if(localPath == null){
            resultPath = " "; //删除文件的时候内容设为空串
        }else {
            File file = new File(localPath);
            String fileName = file.getName();  // 获取文件名，包括扩展名，如 "file.jpg"
            resultPath = "/" + dbName1 + "/" + tableName + "/" + id + "/" + fileName;
        }

        System.out.println("Result Path: " + resultPath);
        return updatefield(resultPath,dbName,tableName,id,field);
    }

    /**
     * 在DM数据库更新字段内容--即 resultPath
     * @param resultPath 要更新的路径内容
     */
    private boolean updatefield(String resultPath,String dbName, String tableName, String id,String field){
        String idField = getIDField(dbName, tableName);
        String update_sql = String.format("UPDATE %s.%s SET %s = '%s' WHERE %s = '%s';", dbName, tableName,field,resultPath,idField,id);

        try {
            stmt = conn_db.prepareStatement(update_sql);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("update DM success!");
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


    /**
     * 根据数据库名称、表名、字段名和 ID 下载单个文件
     * @param localPath 本地保存路径
     * ...
     * @return 是否下载成功
     */
    public boolean GetFile(String localPath, String dbName, String tableName, String field, String id){
        System.out.println("--------------DownLoad File--------------");
        String CAEPath = null;
        try{
            if(checkFileField(dbName,tableName,field)){
                //定制sql语句，解析得到CAEPath
                CAEPath = sql2CAEPath(dbName,tableName,field,id);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("对应库名，表名，文件字段名有误");
        }
        //解析CAEPath下载到本地路径
        return getfile(CAEPath, localPath);
    }

    private boolean getfile(String CAEPath, String localPath) {
        String bucketName = trimmedCAEPath(CAEPath)[0];
        String objectName = trimmedCAEPath(CAEPath)[1];

        // 提取文件名（从 objectName 中去除路径，只保留文件名）
        String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);

        // 拼接本地路径和文件名
        String localFilePath = localPath + File.separator + fileName;
        //System.out.println(localFilePath);

        try {
            fileClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(localFilePath) // 必须指定文件名
                    .build());
            System.out.println("GET FILE SUCCESS：" + CAEPath + " -> " + localFilePath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*private boolean getfile(String CAEPath, String localPath) {
        String bucketName = trimmedCAEPath(CAEPath)[0];
        String objectName = trimmedCAEPath(CAEPath)[1];

        InputStream inputStream = null;
        try {
            // 调用 API 获取对象流
            *//*inputStream = fileClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName) // 注意这里不要再次 URL 编码
                    .build());*/
    /*

        } catch (Exception e) {
            System.err.println("Error fetching object: " + e.getMessage());
            e.printStackTrace();
        }
        //fileClient.getObject(bucketName, objectName, localPath);

        // 提取文件名
        String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
        //System.out.println("Extracted File Name: " + fileName);
        // 如果 localFolderPath 不为 null，将文件保存到本地
        if (localPath != null) {
            try {
                // 确保目标路径的父目录存在
                File localDir = new File(localPath);
                if (!localDir.exists()) {
                    localDir.mkdirs(); // 创建文件夹
                }

                // 拼接目标文件完整路径
                File localFile = new File(localDir, fileName);
                //System.out.println("Saving file to: " + localFile.getAbsolutePath());

                // 保存文件
                try (FileOutputStream fos = new FileOutputStream(localFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error saving file: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                        System.out.println("文件下载成功：" + CAEPath + " -> " + localPath);
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 如果 localPath 为 null，不执行文件保存，直接返回 false
        return false;
    }*/

    /**
     * 根据数据库名称、表名、字段名和 ID 下载单个文件字符流
     * ...
     * @return 是否下载成功
     */
    public InputStream GetFile( String dbName, String tableName, String field, String id){
        System.out.println("--------------DownLoad FileStream--------------");
        String CAEPath = null;
        try{
            if(checkFileField(dbName,tableName,field)){
                //定制sql语句，解析得到CAEPath
                CAEPath = sql2CAEPath(dbName,tableName,field,id);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("对应库名，表名，文件字段名有误");
        }

        System.out.println("GET STREAM SUCCESS！");
        //解析CAEPath下载到本地路径
        return getstream(CAEPath);

    }

    private InputStream getstream(String CAEPath) {

        String bucketName = trimmedCAEPath(CAEPath)[0];
        String objectName = trimmedCAEPath(CAEPath)[1];

        // 调用 API 获取对象流
        InputStream inputStream = null;
        try {
            inputStream = fileClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    /**
     * 封装的文件字段检查逻辑，返回 true 或 false
     * ...
     * @return 文件字段是否对应成功
     */
    private static boolean checkFileField(String dbName, String tableName, String field) {
        // 使用 Optional 进行链式检查
        return fileDBMap.get(dbName).get(tableName).contains(field);
    }

    /**
     * 根据数据库名称、表名、字段名和 ID 获取文件表中文件字段内容--CAEPath
     * ...
     * @return CAEPath路径
     */
    private String sql2CAEPath(String dbName, String tableName, String field, String id){
        // 获取对应的 ID 字段
        String idField = getIDField(dbName, tableName);

        String CAEPath = null;
        String sql = String.format("SELECT %s FROM %s.%s WHERE %s = '%s';", field, dbName, tableName,idField,id);

        try (PreparedStatement stmt = conn_db.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CAEPath = rs.getString(1); // 获取文件路径
                } else {
                    System.err.println("未找到对应的记录，ID: " + id);
                    return null;
                }
            }
        } catch (Exception e) {
            System.err.println("查询数据库失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return CAEPath;
    }

    // 根据 dbName 和 tableName 获取对应的 ID 字段
    public static String getIDField(String dbName, String tableName) {
        // 从 fileIDMap 中查找对应的 dbName
        Map<String, String> tableMap = fileIDMap.get(dbName);
        if(tableMap != null){
            return tableMap.get(tableName);
        }else{
            throw new IllegalArgumentException("Database not found: " + dbName);
        }
    }

    /**
     * 处理字段内容--文件路径
     * @param CAEPath   CAE-FILE中的目标路径，格式类似于 "xxx/folderName/fileName"。
     * return [bucketName,objectName]
     */
    private String[] trimmedCAEPath(String CAEPath){
        // 去掉前面的 '/'，以便统一处理
        String trimmedPath = CAEPath.substring(1);

        // 分析 CAEPath，提取桶名和对象名
        int firstSlashIndex = trimmedPath.indexOf("/");
        if (firstSlashIndex == -1) {
            throw new IllegalArgumentException("CAEPath 格式不正确，必须包含桶名和文件路径，例如：xxx/folderName/fileName");
        }

        String bucketName = trimmedPath.substring(0, firstSlashIndex);
        String objectName = trimmedPath.substring(firstSlashIndex + 1);

        return new String[]{bucketName,objectName};
    }

    //解析得到库名 & 表名 --> [schema,tableName]
    private String[] parseSQL(String sql) {
        String schema = null;
        String tableName = null;
        List<String> columns = new ArrayList<>();
        try {
            // 解析SQL语句
            Statement statement = CCJSqlParserUtil.parse(sql);

            // 检查 SQL 语句类型
            if (statement instanceof Select) {
                // 解析SELECT语句
                Select selectStatement = (Select) CCJSqlParserUtil.parse(sql);
                PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();

                // 获取FROM子句中的表
                FromItem fromItem = plainSelect.getFromItem();
                if (fromItem instanceof Table) {
                    Table table = (Table) fromItem;
                    if (table != null) {
                        schema = table.getSchemaName();
                        tableName = table.getName();
                    }
                }

                // 获取SELECT子句中的字段
                List<SelectItem> selectItems = plainSelect.getSelectItems();
                for (SelectItem selectItem : selectItems) {
                    if (selectItem instanceof SelectExpressionItem) {
                        SelectExpressionItem expressionItem = (SelectExpressionItem) selectItem;
                        String columnName = expressionItem.getExpression().toString();
                        columns.add(columnName);
                    }
                }

                // 获取JOIN子句中的表（如果有的话）
                List<Join> joins = plainSelect.getJoins();
                if (joins != null) {
                    for (Join join : joins) {
                        FromItem joinItem = join.getRightItem();
                        if (joinItem instanceof Table) {
                            Table table = (Table) joinItem;
                            if (table != null) {
                                schema = table.getSchemaName();
                                tableName = table.getName();
                            }
                        }
                    }
                }
            }
            else {
                System.out.println("Unsupported SQL type");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{
                schema,
                tableName,
                String.join(",", columns),  // 将字段名用逗号连接成一个字符串
        };
    }

    public boolean Query(String sql,ResultSetWrapper rsWrapper) {

        System.out.println("-------------- Query --------------");

        if (!isValidSQLCommand(sql, "select")) {
            System.out.println("illegal statement.");
            return false;
        }

        try {
            stmt = conn.prepareStatement(sql);
            //执行查询
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

        try {
            stmt = conn.prepareStatement(sql);
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

        try {
            stmt = conn.prepareStatement(sql);
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

    /** 显示结果集
     * @param rsWrapper 结果集对象
     */
    public void Display(ResultSetWrapper rsWrapper) {
        if (rsWrapper.getRs() == null) {
            System.err.println("ResultSet is null. No data to display.");
            return;
        }
        try {
            List<Integer> colTypes = new ArrayList<>();
            // 取得结果集元数据
            ResultSetMetaData rsmd = rsWrapper.getRs().getMetaData();
            // 取得结果集所包含的列数
            int numCols = rsmd.getColumnCount();
            // 获取列类型   // 显示列标头
            for (int i = 1; i <= numCols; i++) {
                int type = rsWrapper.getRs().getMetaData().getColumnType(i);
                colTypes.add(type);
                System.out.printf("%-30s", rsmd.getColumnLabel(i));
            }
            System.out.println("");


            // 处理每一行记录
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
//            // 确保 dataConfig 是一个 Map
//            if (!(dataConfig instanceof Map)) {
//                System.out.println("Config file format is incorrect. Expected a Map.");
//                return;
//            }
//
//            Map<String, Object> configMap = (Map<String, Object>) dataConfig;
//
//            // 获取数据库配置
//            Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) configMap.get("database"));
//
//            // 安全获取配置信息
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

    // 关闭语句句柄  关闭数据对象
    public void setClose(ResultSetWrapper rsWrapper) {
        if (rsWrapper != null) {
            try {
                // 关闭语句
                stmt.close();
                //关闭结果集
                rsWrapper.getRs().close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //关闭连接
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

