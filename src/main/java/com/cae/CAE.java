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
    private static final Logger logger = Logger.getLogger(CAE.class.getName());  //��־��¼��
    private MinioClient fileClient;
    private static final Map<String, Map<String, Set<String>>> fileDBMap = new HashMap<>();
    private static final Map<String,Map<String,String>> fileIDMap = new HashMap<>();

    static {
        // fileIDMap ��ʼ��
        fileIDMap.put("HULL_MODEL_AND_INFORMATION_DB",
                Collections.singletonMap("HULL_PARAMETER_INFO", "HULL_ID"));

        // fileDBMap ��ʼ��
        fileDBMap.put("HULL_MODEL_AND_INFORMATION_DB",
                Collections.singletonMap("HULL_PARAMETER_INFO",
                        new HashSet<>(Arrays.asList("TRANSVERSE_AREA_CURVE", "HULL_3D_MODEL", "OFFSETS_TABLE"))
                ));
    }

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

            System.out.println(hmacSHA256(password,"SYSDBA"));

            if (server == null || username == null || password == null) {
                System.out.println("Missing required configuration in the config file.");
                System.exit(1);
            }

            // �������ε�����
            String url = "jdbc:dm://" + server;
            conn = DriverManager.getConnection(url, username, hmacSHA256(password,"SYSDBA"));
            conn.setAutoCommit(true);
            System.out.println("========== JDBC: connect to DM server success! ==========");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[FAIL]conn database��" + e.getMessage());
        }
    }

    /**
     * HMAC-SHA256��Hash-based Message Authentication Code����һ�ֻ��ڹ�ϣ��������Կ����Ϣ��֤���㷨������ȷ����Ϣ�������Ժ���֤��
     * <p>
     * ���룺�����ܵ�����
     * �������SHA256һ��
     * Ӧ�ã������������ǩ�����ļ�������У��
     * ��ȫ�ԣ�������
     *
     * @param plainString ��������
     * @param key         ��Կ
     * @return cipherString ����
     */
    private static String hmacSHA256(String plainString, String key) {
        String cipherString = null;
        try {
            // ָ���㷨
            String algorithm = "HmacSHA256";
            // ������Կ�淶
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            // ��ȡMac����ʵ��
            Mac mac = Mac.getInstance(algorithm);
            // ��ʼ��mac
            mac.init(secretKeySpec);
            // ����mac
            byte[] macBytes = mac.doFinal(plainString.getBytes(StandardCharsets.UTF_8));
            // ���Ϊ16�����ַ���
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
        // ��ȡ YAML �����ļ�
        yaml = new Yaml();
        try (FileReader reader = new FileReader(new File(FilePath))) {
            Object dataConfig = yaml.load(reader);
            if (dataConfig == null) {
                System.out.println("Open config file: " + FilePath + " failed.");
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
            Map<String, String> databaseConfig = (Map<String, String>) configMap.get("database");
            // ��ȡ CAE-FILE ������Ϣ
            Map<String, String> fileConfig = (Map<String, String>) configMap.get("CAE-FILE");
            if (fileConfig == null) {
                System.out.println("Missing 'endpoint' configuration in the config file.");
                System.exit(1);
            }

            // ��ȫ��ȡ������Ϣ
            String server = databaseConfig.get("server");
            String username = databaseConfig.get("username");
            String password = databaseConfig.get("passwd");

            if (server == null || username == null || password == null) {
                System.out.println("Missing required configuration in the config file.");
                System.exit(1);
            }

            // �������ε�����
            String url = "jdbc:dm://" + server;
            conn_db = DriverManager.getConnection(url, username, hmacSHA256(password,"SYSDBA"));
            conn_db.setAutoCommit(true);
            System.out.println("========== JDBC: connect to DM server success! ==========");

            // ��ȡ����� CAE-FILE ������Ϣ
            String endpoint = fileConfig.get("endpoint");
            String file_username = fileConfig.get("username");
            String file_password = fileConfig.get("passwd");

            if (endpoint == null || file_username == null || file_password == null) {
                System.out.println("Missing required configuration in the config file.");
                System.exit(1);
            }
            // ��鲢��ȫ endpoint ��Э��ǰ׺
            if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
                endpoint = "http://" + endpoint; // Ĭ��Ϊ http�������Ҫ https�������Ϊ https://
            }
            System.out.println(hmacSHA256(file_password,"fileadmin"));
            //����CAE-FILE�����ӣ�������һ��fileClientʵ��
            fileClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(file_username, hmacSHA256(file_password,"fileadmin"))
                    .build();

            System.out.println(fileDBMap);
            System.out.println(fileIDMap);

            System.out.println("========== JDBC: connect to CAE_FILE server success! ==========");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[FAIL]conn database��" + e.getMessage());
        }
    }

    public boolean DeleteRecord(String dbName,String tableName,String id){
        //ɾ��һ����¼��Ӧ�������ļ�
        if(fileDBMap.get(dbName).containsKey(tableName)){
            // �� fileDBMap ��ȡ�ļ��ֶμ���
            Map<String, Set<String>> tableMap = fileDBMap.get(dbName);
            if (tableMap != null) {
                Set<String> fields = tableMap.get(tableName);
                if (fields != null) {
                    // �����ֶΣ�ִ��ɾ������
                    for (String field : fields) {
                        // ���� CAEPath
                        String CAEPath = sql2CAEPath(dbName, tableName, field, id);
                        // ɾ���ļ�
                        delete(CAEPath);
                    }
                    //ɾ����¼
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

    //��DM���ݿ���ɾ��һ����¼
    private boolean deleterecord(String dbName,String tableName,String id){
        String idField = getIDField(dbName, tableName);
        String delete_sql = String.format("DELETE FROM %s.%s WHERE %s = '%s';",dbName, tableName,idField,id);
        try {
            stmt = conn_db.prepareStatement(delete_sql);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DM delete this record success!");
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

    /**
     * �������ݿ����ơ��������ֶ����� ID ɾ�������ļ�
     *
     * @param dbName ���ݿ�����
     * @param tableName ������
     * @param field �ֶ�����
     * @param id ���� ID
     * @return �Ƿ�ɾ���ɹ�
     */
    public boolean DeleteFile(String dbName, String tableName, String field, String id){
        System.out.println("--------------Delete File--------------");
        String CAEPath = null;
        try{
            if(checkFileField(dbName,tableName,field)){
                //����sql��䣬�����õ��µ�CAEPath
                CAEPath = sql2CAEPath(dbName,tableName,field,id);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("��Ӧ�������������ļ��ֶ�������");
        }
        //ɾ���ļ� �� ����ֶ�
        return delete(CAEPath) && updateField(null,dbName,tableName,id,field) ;
    }

    private boolean delete(String CAEPath) {
        //��ȡ��ӦObjectName
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
     * �ϴ������ļ��� CAE_FILE ��ͬʱ�����ļ�·��
     * @param localPath �����ļ�·������ʾҪ�ϴ����ļ���
     */
    public boolean UploadFile(String localPath, String dbName, String tableName, String field, String id){
        System.out.println("--------------UpLoad File--------------");
        String CAEPath = null;
        try{
            if(checkFileField(dbName,tableName,field)){
                try{
                    //�����ļ�·��
                    if(updateField(localPath,dbName,tableName,id,field)){
                        //����sql��䣬�����õ��µ�CAEPath
                        CAEPath = sql2CAEPath(dbName,tableName,field,id);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println("DM ���ݿ�����ļ�·��δ�ɹ���");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("��Ӧ�������������ļ��ֶ�������");
        }

        return upload(CAEPath,localPath,dbName,tableName,id);
    }

    private boolean upload(String CAEPath,String localPath,String dbName, String tableName, String id){
        String bucketName = null;
        String objectName = null;
        //��ʼ�ֶ�����Ϊ�ջ��߿մ����������ʼ��Ͱ����objectName
        if(CAEPath == null || CAEPath == " "){
            bucketName = dbName.toLowerCase().replace("_", "-");
            File file = new File(localPath);
            String fileName = file.getName();  // ��ȡ�ļ�����������չ������ "file.jpg"
            objectName = tableName + "/" + id + "/" + fileName;
        }else{
            //��ȡ��ӦObjectName
            bucketName = trimmedCAEPath(CAEPath)[0];
            objectName = trimmedCAEPath(CAEPath)[1];
        }

        // ȷ��Ͱ����
        try {
            if (!fileClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                fileClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // �ϴ��ļ�
        try {
            fileClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(localPath)
                            .build()
            );
            System.out.println("UPLOAD FILE SUCCESS��" + CAEPath + " -> " + localPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // ��� localPath Ϊ null����ִ���ļ����棬ֱ�ӷ��� false
            return false;
        }
    }

    /**
     * �����õ�Ҫ�����ֶ����ݣ����ϴ���DM���ݿ�
     * @return  �ɹ���ʧ��
     */
    public boolean updateField(String localPath, String dbName, String tableName, String id,String field) {
        String dbName1 = dbName.toLowerCase().replace("_", "-");
        String resultPath = null;
        if(localPath == null){
            resultPath = " "; //ɾ���ļ���ʱ��������Ϊ�մ�
        }else {
            File file = new File(localPath);
            String fileName = file.getName();  // ��ȡ�ļ�����������չ������ "file.jpg"
            resultPath = "/" + dbName1 + "/" + tableName + "/" + id + "/" + fileName;
        }

        System.out.println("Result Path: " + resultPath);
        return updatefield(resultPath,dbName,tableName,id,field);
    }

    /**
     * ��DM���ݿ�����ֶ�����--�� resultPath
     * @param resultPath Ҫ���µ�·������
     */
    private boolean updatefield(String resultPath,String dbName, String tableName, String id,String field){
        String idField = getIDField(dbName, tableName);
        String update_sql = String.format("UPDATE %s.%s SET %s = '%s' WHERE %s = '%s';", dbName, tableName,field,resultPath,idField,id);

        try {
            stmt = conn_db.prepareStatement(update_sql);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("update DM success!");
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


    /**
     * �������ݿ����ơ��������ֶ����� ID ���ص����ļ�
     * @param localPath ���ر���·��
     * ...
     * @return �Ƿ����سɹ�
     */
    public boolean GetFile(String localPath, String dbName, String tableName, String field, String id){
        System.out.println("--------------DownLoad File--------------");
        String CAEPath = null;
        try{
            if(checkFileField(dbName,tableName,field)){
                //����sql��䣬�����õ�CAEPath
                CAEPath = sql2CAEPath(dbName,tableName,field,id);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("��Ӧ�������������ļ��ֶ�������");
        }
        //����CAEPath���ص�����·��
        return getfile(CAEPath, localPath);
    }

    private boolean getfile(String CAEPath, String localPath) {
        String bucketName = trimmedCAEPath(CAEPath)[0];
        String objectName = trimmedCAEPath(CAEPath)[1];

        // ��ȡ�ļ������� objectName ��ȥ��·����ֻ�����ļ�����
        String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);

        // ƴ�ӱ���·�����ļ���
        String localFilePath = localPath + File.separator + fileName;
        //System.out.println(localFilePath);

        try {
            fileClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(localFilePath) // ����ָ���ļ���
                    .build());
            System.out.println("GET FILE SUCCESS��" + CAEPath + " -> " + localFilePath);
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
            // ���� API ��ȡ������
            *//*inputStream = fileClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName) // ע�����ﲻҪ�ٴ� URL ����
                    .build());*/
    /*

        } catch (Exception e) {
            System.err.println("Error fetching object: " + e.getMessage());
            e.printStackTrace();
        }
        //fileClient.getObject(bucketName, objectName, localPath);

        // ��ȡ�ļ���
        String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
        //System.out.println("Extracted File Name: " + fileName);
        // ��� localFolderPath ��Ϊ null�����ļ����浽����
        if (localPath != null) {
            try {
                // ȷ��Ŀ��·���ĸ�Ŀ¼����
                File localDir = new File(localPath);
                if (!localDir.exists()) {
                    localDir.mkdirs(); // �����ļ���
                }

                // ƴ��Ŀ���ļ�����·��
                File localFile = new File(localDir, fileName);
                //System.out.println("Saving file to: " + localFile.getAbsolutePath());

                // �����ļ�
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
                        System.out.println("�ļ����سɹ���" + CAEPath + " -> " + localPath);
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // ��� localPath Ϊ null����ִ���ļ����棬ֱ�ӷ��� false
        return false;
    }*/

    /**
     * �������ݿ����ơ��������ֶ����� ID ���ص����ļ��ַ���
     * ...
     * @return �Ƿ����سɹ�
     */
    public InputStream GetFile( String dbName, String tableName, String field, String id){
        System.out.println("--------------DownLoad FileStream--------------");
        String CAEPath = null;
        try{
            if(checkFileField(dbName,tableName,field)){
                //����sql��䣬�����õ�CAEPath
                CAEPath = sql2CAEPath(dbName,tableName,field,id);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("��Ӧ�������������ļ��ֶ�������");
        }

        System.out.println("GET STREAM SUCCESS��");
        //����CAEPath���ص�����·��
        return getstream(CAEPath);

    }

    private InputStream getstream(String CAEPath) {

        String bucketName = trimmedCAEPath(CAEPath)[0];
        String objectName = trimmedCAEPath(CAEPath)[1];

        // ���� API ��ȡ������
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
     * ��װ���ļ��ֶμ���߼������� true �� false
     * ...
     * @return �ļ��ֶ��Ƿ��Ӧ�ɹ�
     */
    private static boolean checkFileField(String dbName, String tableName, String field) {
        // ʹ�� Optional ������ʽ���
        return fileDBMap.get(dbName).get(tableName).contains(field);
    }

    /**
     * �������ݿ����ơ��������ֶ����� ID ��ȡ�ļ������ļ��ֶ�����--CAEPath
     * ...
     * @return CAEPath·��
     */
    private String sql2CAEPath(String dbName, String tableName, String field, String id){
        // ��ȡ��Ӧ�� ID �ֶ�
        String idField = getIDField(dbName, tableName);

        String CAEPath = null;
        String sql = String.format("SELECT %s FROM %s.%s WHERE %s = '%s';", field, dbName, tableName,idField,id);

        try (PreparedStatement stmt = conn_db.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CAEPath = rs.getString(1); // ��ȡ�ļ�·��
                } else {
                    System.err.println("δ�ҵ���Ӧ�ļ�¼��ID: " + id);
                    return null;
                }
            }
        } catch (Exception e) {
            System.err.println("��ѯ���ݿ�ʧ��: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return CAEPath;
    }

    // ���� dbName �� tableName ��ȡ��Ӧ�� ID �ֶ�
    public static String getIDField(String dbName, String tableName) {
        // �� fileIDMap �в��Ҷ�Ӧ�� dbName
        Map<String, String> tableMap = fileIDMap.get(dbName);
        if(tableMap != null){
            return tableMap.get(tableName);
        }else{
            throw new IllegalArgumentException("Database not found: " + dbName);
        }
    }

    /**
     * �����ֶ�����--�ļ�·��
     * @param CAEPath   CAE-FILE�е�Ŀ��·������ʽ������ "xxx/folderName/fileName"��
     * return [bucketName,objectName]
     */
    private String[] trimmedCAEPath(String CAEPath){
        // ȥ��ǰ��� '/'���Ա�ͳһ����
        String trimmedPath = CAEPath.substring(1);

        // ���� CAEPath����ȡͰ���Ͷ�����
        int firstSlashIndex = trimmedPath.indexOf("/");
        if (firstSlashIndex == -1) {
            throw new IllegalArgumentException("CAEPath ��ʽ����ȷ���������Ͱ�����ļ�·�������磺xxx/folderName/fileName");
        }

        String bucketName = trimmedPath.substring(0, firstSlashIndex);
        String objectName = trimmedPath.substring(firstSlashIndex + 1);

        return new String[]{bucketName,objectName};
    }

    //�����õ����� & ���� --> [schema,tableName]
    private String[] parseSQL(String sql) {
        String schema = null;
        String tableName = null;
        List<String> columns = new ArrayList<>();
        try {
            // ����SQL���
            Statement statement = CCJSqlParserUtil.parse(sql);

            // ��� SQL �������
            if (statement instanceof Select) {
                // ����SELECT���
                Select selectStatement = (Select) CCJSqlParserUtil.parse(sql);
                PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();

                // ��ȡFROM�Ӿ��еı�
                FromItem fromItem = plainSelect.getFromItem();
                if (fromItem instanceof Table) {
                    Table table = (Table) fromItem;
                    if (table != null) {
                        schema = table.getSchemaName();
                        tableName = table.getName();
                    }
                }

                // ��ȡSELECT�Ӿ��е��ֶ�
                List<SelectItem> selectItems = plainSelect.getSelectItems();
                for (SelectItem selectItem : selectItems) {
                    if (selectItem instanceof SelectExpressionItem) {
                        SelectExpressionItem expressionItem = (SelectExpressionItem) selectItem;
                        String columnName = expressionItem.getExpression().toString();
                        columns.add(columnName);
                    }
                }

                // ��ȡJOIN�Ӿ��еı�����еĻ���
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
                String.join(",", columns),  // ���ֶ����ö������ӳ�һ���ַ���
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
            //ִ�в�ѯ
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

        try {
            stmt = conn.prepareStatement(sql);
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

        try {
            stmt = conn.prepareStatement(sql);
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

    /** ��ʾ�����
     * @param rsWrapper ���������
     */
    public void Display(ResultSetWrapper rsWrapper) {
        if (rsWrapper.getRs() == null) {
            System.err.println("ResultSet is null. No data to display.");
            return;
        }
        try {
            List<Integer> colTypes = new ArrayList<>();
            // ȡ�ý����Ԫ����
            ResultSetMetaData rsmd = rsWrapper.getRs().getMetaData();
            // ȡ�ý����������������
            int numCols = rsmd.getColumnCount();
            // ��ȡ������   // ��ʾ�б�ͷ
            for (int i = 1; i <= numCols; i++) {
                int type = rsWrapper.getRs().getMetaData().getColumnType(i);
                colTypes.add(type);
                System.out.printf("%-30s", rsmd.getColumnLabel(i));
            }
            System.out.println("");


            // ����ÿһ�м�¼
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
//            // ȷ�� dataConfig ��һ�� Map
//            if (!(dataConfig instanceof Map)) {
//                System.out.println("Config file format is incorrect. Expected a Map.");
//                return;
//            }
//
//            Map<String, Object> configMap = (Map<String, Object>) dataConfig;
//
//            // ��ȡ���ݿ�����
//            Optional<Map<String, String>> databaseConfig = Optional.ofNullable((Map<String, String>) configMap.get("database"));
//
//            // ��ȫ��ȡ������Ϣ
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

    // �ر������  �ر����ݶ���
    public void setClose(ResultSetWrapper rsWrapper) {
        if (rsWrapper != null) {
            try {
                // �ر����
                stmt.close();
                //�رս����
                rsWrapper.getRs().close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //�ر�����
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

