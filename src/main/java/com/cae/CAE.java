package com.cae;

import io.minio.*;
import org.yaml.snakeyaml.Yaml;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class CAE {
    private static final String JDBC_DRIVER = "dm.jdbc.driver.DmDriver";
    private Connection conn;
    private Yaml yaml;
    private PreparedStatement stmt;
    private static final Logger logger = Logger.getLogger(CAE.class.getName());  //日志记录器
    private MinioClient fileClient;
    private static final Map<String, Map<String, Set<String>>> fileDBMap = new HashMap<>();
    private static final Map<String, Map<String, String>> fileIDMap = new HashMap<>();

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
        this.yaml = new Yaml();
        try (FileReader reader = new FileReader(new File(filePath))) {
            Object dataConfig = this.yaml.load(reader);
            if (dataConfig == null) {
                System.err.println("Open config file: " + filePath + " failed.");
                System.exit(1);
            }

            // 加载 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 确保 dataConfig 是一个 Map
            if (!(dataConfig instanceof Map)) {
                System.err.println("Config file format is incorrect. Expected a Map.");
                System.exit(1);
            }

            Map<String, Object> configMap = (Map<String, Object>) dataConfig;

            // 获取数据库配置
            Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) configMap.get("database"));

            // 安全获取配置信息
            String server = databaseConfig.map(m -> m.get("server")).orElse(null);
            String username = databaseConfig.map(m -> m.get("username")).orElse(null);
            String password = databaseConfig.map(m -> m.get("passwd")).orElse(null);

            System.out.println(encryption(password));

            if (server == null || username == null || password == null) {
                System.err.println("Missing required configuration in the config file.");
                System.exit(1);
            }

            // 建立达梦的连接
            String url = "jdbc:dm://" + server;
            this.conn = DriverManager.getConnection(url, username, encryption(password));
            this.conn.setAutoCommit(true);
            System.out.println("========== JDBC: connect to DM server success! ==========");
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("[FAIL]conn database：" + e.getMessage());
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
     * @return cipherString 密文
     */
    private static String encryption(String plainString) {
        String cipherString = null;
        try {
            // 指定算法
            String algorithm = "HmacSHA256";
            // 创建密钥规范
            SecretKeySpec secretKeySpec = new SecretKeySpec("3G@ln$UOd8Ptf@XU".getBytes(StandardCharsets.UTF_8), algorithm);
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
            cipherString = sb.toString().substring(0, 32);
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            //e.printStackTrace();
        }
        return cipherString;
    }


    public CAE(String FilePath, boolean xxx) {
        // 读取 YAML 配置文件
        this.yaml = new Yaml();
        try (FileReader reader = new FileReader(new File(FilePath))) {
            Object dataConfig = this.yaml.load(reader);
            if (dataConfig == null) {
                System.err.println("Open config file: " + FilePath + " failed.");
                System.exit(1);
            }

            // 加载 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 确保 dataConfig 是一个 Map
            if (!(dataConfig instanceof Map)) {
                System.err.println("Config file format is incorrect. Expected a Map.");
                System.exit(1);
            }

            Map<String, Object> configMap = (Map<String, Object>) dataConfig;

            // 获取数据库配置
            Map<String, String> databaseConfig = (Map<String, String>) configMap.get("database");
            // 获取 filesystem 配置信息
            Map<String, String> fileConfig = (Map<String, String>) configMap.get("fileSystem");
            if (fileConfig == null) {
                System.err.println("Missing 'endpoint' configuration in the config file.");
                System.exit(1);
            }

            // 安全获取配置信息
            String server = databaseConfig.get("server");
            String username = databaseConfig.get("username");
            String password = databaseConfig.get("passwd");

            if (server == null || username == null || password == null) {
                System.err.println("Missing required configuration in the config file.");
                System.exit(1);
            }

            // 建立达梦的连接
            String url = "jdbc:dm://" + server;
            this.conn = DriverManager.getConnection(url, username, encryption(password));
            this.conn.setAutoCommit(true);
            System.out.println("========== JDBC: connect to DM server success! ==========");

            // 获取具体的 filesystem 配置信息
            String endpoint = fileConfig.get("endpoint");
            String file_username = fileConfig.get("username");
            String file_password = fileConfig.get("passwd");

            if (endpoint == null || file_username == null || file_password == null) {
                System.err.println("Missing required configuration in the config file.");
                System.exit(1);
            }
            // 检查并补全 endpoint 的协议前缀
            if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
                endpoint = "http://" + endpoint; // 默认为 http，如果需要 https，请更改为 https://
            }

            System.out.println(encryption(file_password));
            // 在建立 Minio 连接时，捕获网络连接异常
            try {
                this.fileClient = MinioClient.builder()
                        .endpoint(endpoint)
                        .credentials(file_username, encryption(file_password))
                        .build();

                // 尝试连接 Minio 以确保能够连接成功
                this.fileClient.listBuckets();  // 尝试列出桶，测试连接是否成功
                System.out.println("========== Minio: connect to CAE_FILE server success! ==========");
            } catch (Exception e) {
                System.err.println("========== ERROR: Failed to establish Minio connection. " + e.getMessage());
                System.exit(1);
            }

            System.out.println(this.fileDBMap);
            System.out.println(this.fileIDMap);

            System.out.println("========== JDBC: connect to CAE_FILE server success! ==========");
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("[FAIL]conn database：" + e.getMessage());
        }
    }

    public boolean DeleteRecord(String dbName, String tableName, String id) {
        boolean hasValidCAEPath = false;
        //删除一条记录对应的所有文件
        if (!this.fileDBMap.containsKey(dbName)) {
            System.err.println(String.format("Database [%s] does not exist!", dbName));
            return false;
        }

        Map<String, Set<String>> tableMap = this.fileDBMap.get(dbName);
        if (tableMap == null || !tableMap.containsKey(tableName)) {
            System.err.println(String.format("No table [%s] found for the database: [%s]!", dbName, tableName, id));
            return false;
        }

        Set<String> fields = tableMap.get(tableName);
        if (fields == null) {
            System.err.println("No fields found for table: " + tableName);//不是对应数据表名
            return false;
        }

        // 遍历字段，执行删除操作
        for (String field : fields) {
            // 生成 CAEPath
            String CAEPath = this.Sql2CAEPath(dbName, tableName, field, id);
            // 校验CAEPath
            if(CAEPath == null || CAEPath.trim().isEmpty() || CAEPath.equals("false")){
                continue;
            }
            // 标志位更新为 true，说明找到了有效的 CAEPath
            hasValidCAEPath = true;
            // 删除文件
            this.delete(CAEPath);
        }
        // 检查标志位，如果没有有效的 CAEPath，则输出提示信息
        if (!hasValidCAEPath) {
            System.err.println(String.format("No corresponding file data found for the database: [%s], table: [%s], ID: [%s]!", dbName, tableName, id));
        }
        //删除记录
        return this.DeleteRecordInDM(dbName, tableName, id);
    }

    //在DM数据库中删除一条记录
    private boolean DeleteRecordInDM(String dbName, String tableName, String id) {
        String idField = this.GetIDField(dbName, tableName);
        String delete_sql = String.format("DELETE FROM %s.%s WHERE %s = '%s';", dbName, tableName, idField, id);

        return Delete(delete_sql);
    }

    /**
     * 根据数据库名称、表名、字段名和 ID 删除单个文件
     * @param dbName    数据库名称
     * @param tableName 表名称
     * @param field     字段名称
     * @param id        主键 ID
     * @return 是否删除成功
     */
    public boolean DeleteFile(String dbName, String tableName, String field, String id) {
        System.out.println("--------------Delete File--------------");
        String CAEPath = null;
        try {
            //没找到对应记录
            if (!this.CheckFileField(dbName, tableName, field)) {
                System.err.println(String.format("The specified field -- %s is not a file field! ", field));
                return false;
            }
            //定制sql语句，解析得到新的CAEPath
            CAEPath = this.Sql2CAEPath(dbName, tableName, field, id);
            //字段内容为空，不需要去minio中处理操作
            // 校验CAEPath
            if (!this.isCAEPathValid(CAEPath, dbName, tableName, field, id)) {
                return false;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println(String.format("dbName [%s], tableName [%s], or field [%s] is incorrect.", dbName, tableName, field) + e.getMessage());
            //System.err.println("对应库名，表名，文件字段名有误");
            return false;
        }
        //删除文件 并 清空字段
        return this.delete(CAEPath) && this.UpdateField(null, dbName, tableName, id, field);
    }

    private boolean delete(String CAEPath) {
        //获取对应ObjectName
        String[] pathParts = this.TrimmedCAEPath(CAEPath);
        String bucketName = pathParts[0];
        String objectName = pathParts[1];

        try {
            this.fileClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            System.out.println("Object deleted: " + objectName);
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }

    /**
     * 上传单个文件到 CAE_FILE 并同时更新文件路径
     * @param localPath 本地文件路径，表示要上传的文件。
     */
    public boolean UploadFile(String localPath, String dbName, String tableName, String field, String id) {
        System.out.println("--------------UpLoad File--------------");
        String CAEPath = null;
        try {
            if (!this.CheckFileField(dbName, tableName, field)) {
                System.err.println(String.format("The specified field [%s] is not a file field.", field));
                return false;
            }
            //ID是否存在
            if (this.Sql2CAEPath(dbName, tableName, field, id).equals("false")) { //不存在对应的记录
                System.err.println(String.format("No corresponding field found for the database: [%s], table: [%s], field：[%s], ID: [%s]! ", dbName, tableName, field,id));
                return false;
            }
            //更新文件路径 & 在达梦中更新
            if (!UpdateField(localPath, dbName, tableName, id, field)) {
                return false;
            }
            //定制sql语句，解析得到新的CAEPath
            CAEPath = this.Sql2CAEPath(dbName, tableName, field, id);
            //字段内容为空，自主更新桶名，ObjectName
            return this.upload(CAEPath, localPath, dbName, tableName, id);
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println(String.format("dbName [%s], tableName [%s], or field [%s] is incorrect. ", dbName, tableName, field) + e.getMessage());
            //System.err.println("对应库名，表名，文件字段名有误！");
            return false;
        }

    }

    private boolean upload(String CAEPath, String localPath, String dbName, String tableName, String id) {
        String bucketName = null;
        String objectName = null;
        //初始字段内容为空或者空串的情况，初始化桶名和objectName
        if (CAEPath == null || CAEPath.trim().isEmpty()) {
            bucketName = dbName.toLowerCase().replace("_", "-");
            File file = new File(localPath);
            String fileName = file.getName();  // 获取文件名，包括扩展名，如 "file.jpg"
            objectName = tableName + "/" + id + "/" + fileName;
        } else {
            //获取对应ObjectName
            String[] pathParts = this.TrimmedCAEPath(CAEPath);
            bucketName = pathParts[0];
            objectName = pathParts[1];

        }

        // 确保桶存在
        try {
            if (!this.fileClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                this.fileClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("Exception occurred: " + e.getMessage());
        }

        // 上传文件
        try {
            this.fileClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(localPath)
                            .build()
            );
            System.out.println("UPLOAD FILE：" + localPath + " -> " + CAEPath);
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("Exception occurred: " + e.getMessage());
            // 如果 localPath 为 null，不执行文件保存，直接返回 false
            return false;
        }
    }

    /**
     * 解析得到要更新字段内容，并上传至DM数据库
     * @return 成功？失败
     */
    public boolean UpdateField(String localPath, String dbName, String tableName, String id, String field) {
        String dbName1 = dbName.toLowerCase().replace("_", "-");
        String resultPath = null;
        File file = new File(localPath);
        if(!file.exists() || !file.isFile()){  //文件路径不存在或者不是文件
            System.err.println(String.format("Exception occurred: [%s] not a regular file", localPath));
            return false;
        }
        if (localPath == null) {
            resultPath = " "; //删除文件的时候内容设为空串
        } else {
            String fileName = file.getName();  // 获取文件名，包括扩展名，如 "file.jpg"
            resultPath = "/" + dbName1 + "/" + tableName + "/" + id + "/" + fileName;
        }

        System.out.println("Result Path: " + resultPath);
        return this.UpdateFieldInDM(resultPath, dbName, tableName, id, field);
    }

    /**
     * 在DM数据库更新字段内容--即 resultPath
     *
     * @param resultPath 要更新的路径内容
     */
    private boolean UpdateFieldInDM(String resultPath, String dbName, String tableName, String id, String field) {
        String idField = this.GetIDField(dbName, tableName);
        String update_sql = String.format(
                "UPDATE %s.%s SET %s = '%s' WHERE %s = '%s';", dbName, tableName, field, resultPath, idField, id);
        return Update(update_sql);
    }


    /**
     * 根据数据库名称、表名、字段名和 ID 下载单个文件
     * @param localPath 本地保存路径
     *   ...
     * @return 是否下载成功
     */
    public boolean GetFile(String localPath, String dbName, String tableName, String field, String id) {
        System.out.println("--------------DownLoad File--------------");
        String CAEPath = null;
        // 校验 localPath 合法性
        if (!isLocalPathValid(localPath)) {
            //System.err.println(String.format("The specified local path [%s] is invalid.", localPath));
            return false;
        }
        try {
            if (!this.CheckFileField(dbName, tableName, field)) {
                System.err.println(String.format("The specified field [%s] is not a file field.", field));
                return false;
            }
            //定制sql语句，解析得到CAEPath
            CAEPath = this.Sql2CAEPath(dbName, tableName, field, id);

            // 校验CAEPath
            if (!this.isCAEPathValid(CAEPath, dbName, tableName, field, id)) {
                return false;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            System.err.println(String.format("dbName [%s], tableName [%s], or field [%s] is incorrect.", dbName, tableName, field) + e.getMessage());
            return false;
        }
        //解析CAEPath下载到本地路径
        return this.DownloadObject(CAEPath, localPath);
    }

    private boolean DownloadObject(String CAEPath, String localPath) {

        String[] pathParts = this.TrimmedCAEPath(CAEPath);
        String bucketName = pathParts[0];
        String objectName = pathParts[1];

        // 提取文件名（从 objectName 中去除路径，只保留文件名）
        String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);

        // 拼接本地路径和文件名
        String localFilePath = localPath + File.separator + fileName;
        //System.out.println(localFilePath);

        try {
            this.fileClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(localFilePath) // 必须指定文件名
                    .build());
            System.out.println("GET FILE：" + CAEPath + " -> " + localFilePath);
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("Exception occurred: " + e.getMessage());
            return false;
        }
    }


    /**
     * 根据数据库名称、表名、字段名和 ID 下载单个文件字符流
     * ...
     *
     * @return 是否下载成功
     */
    public InputStream GetFile(String dbName, String tableName, String field, String id) {
        System.out.println("--------------DownLoad FileStream--------------");
        String CAEPath = null;
        try {
            if (!this.CheckFileField(dbName, tableName, field)) {
                System.err.println(String.format("The specified field [%s] is not a file field.", field));
                return null;
            }
            //定制sql语句，解析得到CAEPath
            CAEPath = this.Sql2CAEPath(dbName, tableName, field, id);
            if (CAEPath == null || CAEPath.trim().isEmpty()) {
                System.err.println(String.format("The file field content is empty for the database: [%s], table: [%s], field: [%s], ID: [%s]!！", dbName, tableName, field, id));
                return null;
            } else if (CAEPath.equals("false")) {//不存在对应的记录
                System.err.println(String.format("No corresponding field found for the database: [%s], table: [%s], field：[%s], ID: [%s]! ", dbName, tableName, field,id));
                return null;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println(String.format("dbName [%s], tableName [%s], or field [%s] is incorrect.", dbName, tableName, field) + e.getMessage());
            //System.err.println("对应库名，表名，文件字段名有误");
            return null;
        }
        //解析CAEPath下载到本地路径
        return this.GetStream(CAEPath);
    }

    private InputStream GetStream(String CAEPath) {

        String[] pathParts = this.TrimmedCAEPath(CAEPath);
        String bucketName = pathParts[0];
        String objectName = pathParts[1];

        // 调用 API 获取对象流
        InputStream inputStream = null;
        try {
            inputStream = this.fileClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("Exception occurred: " + e.getMessage());
        }
        return inputStream;
    }

    private boolean isCAEPathValid(String CAEPath, String dbName, String tableName, String field, String id) {
        if (CAEPath == null || CAEPath.trim().isEmpty()) {
            System.err.println(String.format("The file field content is empty for the database: [%s], table: [%s], field: [%s], ID: [%s]!！", dbName, tableName, field, id));
            return false;
        } else if (CAEPath.equals("false")) {
            System.err.println(String.format("No corresponding field found for the database: [%s], table: [%s], field：[%s], ID: [%s]! ", dbName, tableName, field,id));
            //System.err.println("不存在对应的记录！");
            return false;
        } else {
            return true;
        }

    }

    /**
     * 校验 localPath 是否有效
     * @param localPath 本地路径
     * @return 是否合法
     */
    private boolean isLocalPathValid(String localPath) {
        File path = new File(localPath);
        if (!path.exists()) {
            // 如果路径不存在，直接返回 false
            System.err.println(String.format("The specified path [%s] does not exist.", localPath));
            return false;
        }
        if (!path.isDirectory()) {
            System.err.println(String.format("The specified path [%s] is not a directory.", localPath));
            return false;
        }
        return true;
    }


    /**
     * 封装的文件字段检查逻辑，返回 true 或 false
     * ...
     * @return 文件字段是否对应成功
     */
    private boolean CheckFileField(String dbName, String tableName, String field) {
        // 使用 Optional 进行链式检查
        //boolean check = this.fileDBMap.get(dbName).get(tableName).contains(field);
        return this.fileDBMap.get(dbName).get(tableName).contains(field);
    }

    /**
     * 根据数据库名称、表名、字段名和 ID 获取文件表中文件字段内容--CAEPath
     * ...
     *
     * @return CAEPath路径
     */
    private String Sql2CAEPath(String dbName, String tableName, String field, String id) {
        // 获取对应的 ID 字段
        String idField = this.GetIDField(dbName, tableName);

        String CAEPath = null;
        String sql = String.format("SELECT %s FROM %s.%s WHERE %s = '%s';", field, dbName, tableName, idField, id);

        try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CAEPath = rs.getString(1); // 获取文件路径
                } else {
                    //System.err.println(String.format("No corresponding field found for the database: [%s], table: [%s], field：[%s], ID: [%s]! ", dbName, tableName, field,id));
                    return "false";
                }
            }
        } catch (Exception e) {
            System.err.println(String.format("Failed to query the database: %s.", dbName) + e.getMessage());
            //e.printStackTrace();
            return "false";
        }
        return CAEPath;
    }

    // 根据 dbName 和 tableName 获取对应的 ID 字段
    public String GetIDField(String dbName, String tableName) {
        // 从 fileIDMap 中查找对应的 dbName
        Map<String, String> tableMap = this.fileIDMap.get(dbName);
        if (tableMap != null) {
            return tableMap.get(tableName);
        } else {
            throw new IllegalArgumentException("Database not found: " + dbName);
        }
    }

    /**
     * 处理字段内容--文件路径
     *
     * @param CAEPath filesystem中的目标路径，格式类似于 "xxx/folderName/fileName"。
     *                return [bucketName,objectName]
     */
    private String[] TrimmedCAEPath(String CAEPath) {
        // 去掉前面的 '/'，以便统一处理
        String trimmedPath = CAEPath.substring(1);

        // 分析 CAEPath，提取桶名和对象名
        int firstSlashIndex = trimmedPath.indexOf("/");
        if (firstSlashIndex == -1) {
            System.err.println("路径格式不正确，必须包含桶名和文件路径，例如：xxx/folderName/fileName");
            //throw new IllegalArgumentException("路径格式不正确，必须包含桶名和文件路径，例如：xxx/folderName/fileName");
        }

        String bucketName = trimmedPath.substring(0, firstSlashIndex);
        String objectName = trimmedPath.substring(firstSlashIndex + 1);

        return new String[]{bucketName, objectName};
    }

    public boolean Query(String sql, ResultSetWrapper rsWrapper) {

        System.out.println("---------- Query In DM ---------");

        if (!isValidSQLCommand(sql, "select")) {
            System.err.println("illegal statement.");
            return false;
        }

        try {
            this.stmt = this.conn.prepareStatement(sql);
            //执行查询
            rsWrapper.setRs(this.stmt.executeQuery());
            System.out.println("DM query success!");
            return true;
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            //e.printStackTrace();
            return false;
        }
    }

    public boolean Update(String sql) {
        System.out.println("--------- Update In DM ---------");

        if (!isValidSQLCommand(sql, "update")) {
            System.err.println("illegal statement.");
            return false;
        }

        try {
            this.stmt = this.conn.prepareStatement(sql);
            int rowsAffected = this.stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DM update success!");
                // 关闭语句
                this.stmt.close();
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

    public boolean Delete(String sql) {
        System.out.println("---------- Delete In DM ----------");

        if (!isValidSQLCommand(sql, "delete")) {
            System.err.println("illegal statement.");
            return false;
        }

        try {
            this.stmt = this.conn.prepareStatement(sql);
            int rowsAffected = this.stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DM delete success!");
                // 关闭语句
                this.stmt.close();
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

    public boolean Insert(String sql) {
        System.out.println("--------- Insert In DM ---------");
        if (!isValidSQLCommand(sql, "insert")) {
            System.err.println("illegal statement.");
            return false;
        }

        try {
            this.stmt = this.conn.prepareStatement(sql);
            int rowsAffected = this.stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DM insert success!");
                // 关闭语句
                this.stmt.close();
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

    /**
     * 显示结果集
     *
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
        } catch (SQLException e) {
            System.err.println("Error displaying ResultSet: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    private boolean isValidSQLCommand(String sql, String type) {
        String trimmedSQL = sql.trim().toLowerCase();
        return trimmedSQL.startsWith(type);
    }


    // 关闭语句句柄  关闭数据对象
    public void setClose(ResultSetWrapper rsWrapper) {
        if (rsWrapper != null) {
            try {
                // 关闭语句
                this.stmt.close();
                //关闭结果集
                rsWrapper.getRs().close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
                //e.printStackTrace();
            }
        }
    }

    //关闭连接
    public void connClose() {
        try {
            if (this.conn != null && !this.conn.isClosed()) {
                this.conn.close();
                System.out.println("========== JDBC: disconnect from server success! ==========");
            }
        } catch (SQLException e) {
            System.err.println("Exception occurred: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    // 关闭文件系统的连接
    public void File_connClose() {
        // 关闭 JDBC 连接
        try {
            if (this.conn != null && !this.conn.isClosed()) {
                this.conn.close();
                System.out.println("========== JDBC: disconnect from server success! ==========");
            }
        } catch (SQLException e) {
            System.err.println("Exception occurred: " + e.getMessage());
            //e.printStackTrace();
        }

        // 关闭 Minio 客户端连接
        try {
            if (this.fileClient != null) {
                // Minio 没有直接关闭的方法，设置为 null 以帮助垃圾回收
                this.fileClient = null;
                System.out.println("========== Minio: disconnect from server success! ==========");
            }
        } catch (Exception e) {
            System.err.println("========== ERROR: Failed to disconnect from Minio server. " + e.getMessage());
        }
    }


}

