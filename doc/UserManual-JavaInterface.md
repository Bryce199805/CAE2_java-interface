# Java数据接口用户手册

```
Java数据接口为用户提供直接操作达梦数据库的相关API接口，便于拥有管理员权限的用户对数据库中相关库表进行增删改查操作，对文件系统上传、下载、删除单个文件以及删除一条记录的操作。
```

### 目录说明

- **CAE-1.0.jar** 包含数据接口所打包的库文件以及相关依赖
- **UserManual-JavaInterface.md** 即本文档，Java数据接口用户手册
- **config.yaml** 数据库配置参数文件

### 环境配置

- **中文编码**：请设置为`UTF-8`。

- **配置信息**：请将`config.yaml`中的配置信息修改为本机对应的信息。

  ### **config.yaml**

```json
server:                                        # 数据库相关信息
  username: "708_USER"                         # 数据库用户名
  password: "708_user"                         # 数据库密码
  database-server: "222.27.255.211:15236"      # 数据库服务地址 ip:port
  file-system-server: "222.27.255.211:19000"   # 文件数据库服务地址 ip:port

log:                                           # 日志相关信息
  username: "Admin_LOG"                        # 日志用户名
  password: "logmanager"                       # 日志密码
  cidr: "192.168.0.0/16"                       # 本机cidr
  enable: true                                 # 日志记录的开关
```

------

## API 用户手册

```java
API包括CAE类和ResultSetWrapper类
```

#### Class ResultSetWrapper

`ResultSetWrapper` 类用于包装 `ResultSet` 对象，提供了一种方便的方式来管理和访问 `ResultSet` 对象。

##### 成员变量

- rs：Result类型，用于存储查询结果集。

##### 构造函数

```java
ResultSetWrapper()
```

初始化ResultSetWrapper对象为null。

- 参数列表：无
- 返回类型：无



##### setRs方法

设置ResultSetWrapper中的ResultSet结果集对象，将传入的ResultSetWrapper对象赋值给成员变量rs。

```java
public void setRs(ResultSet rs)
```

- 参数列表：
  - rs：ResultSet类型，要设置的ResultSet对象。
- 返回类型：void



##### getRs方法

获取ResultSetWrapper中的ResultSet结果集对象。

```java
public ResultSet getRs()
```

- 参数列表：无。
- 返回类型：ResultSet类型，返回当前包装的ResultSet对象。

##### 

#### Class CAE

`CAE` 类封装了对数据库操作的基本增删改查方法，对文件数据库的上传、下载、删除单个文件以及删除一条记录方法，并提供了对查询结果的打印方法用于验证，对系统和数据库的连接。

##### 构造函数

```java
public CAE(String filePath)
```

初始化 `CAE` 类的对象，并从指定的 yaml文件中加载数据库配置。此外，它还会尝试加载 JDBC 驱动程序，并解析数据库配置信息，建立数据库连接。

- 形参列表
  - **filePath**：String类型，yaml配置文件地址。

- 返回值类型：无

```java
String filePath = "xxx/config.yaml";
CAE db = new CAE(filePath);
```



##### 构造函数

```java
public CAE(String filePath，boolean xxx)
```

初始化 `CAE` 类的实例对象，它的主要功能是根据给定的配置文件路径（`filePath`）加载数据库配置，并建立与数据库和文件系统的连接。

- **形参列表**

  - **filePath**：`String` 类型，表示配置文件路径。

  - **xxx**：`boolean` 类型，作为使用文件系统的标志参数。

- **返回值类型**：无



##### GetFile方法

```java
public InputStream GetFile(String dbName, String tableName, String field, String id)
```

该方法根据数据库名称、表名、字段名和 ID 下载单个文件的字符流。主要通过数据库字段中的路径信息来获取文件的存储路径，然后将文件以字符流的方式返回。

- **形参列表**
  - **dbName**：String 类型，表示数据库名称。
  - **tableName**：String 类型，表示数据库表名称。
  - **field**：String 类型，表示字段名称。
  - **id**：String 类型，表示文件记录的 ID。
- **返回值类型**：`InputStream`，如果文件存在并成功下载，则返回文件的字符流,并输出打印`[DB Message]: File stream successfully downloaded.`；否则返回 `null`。

```java
CAE File = new CAE(filePath,true);
InputStream inputStream = File.GetFile("HULL_MODEL_AND_INFORMATION_DB", "HULL_PARAMETER_INFO", "OFFSETS_TABLE", "SampleShip_KCS0001");
```



##### GetFile方法

```java
public boolean GetFile(String localPath, String dbName, String tableName, String field, String id)
```

该方法根据数据库名称、表名、字段名和 ID 下载文件，并将文件保存到本地路径localPath。

- **形参列表**

  - **localPath**：String 类型，表示文件的保存路径。
  - **dbName**：String 类型，表示数据库名称。
  - **tableName**：String 类型，表示数据库表名称。
  - **field**：String 类型，表示字段名称。
  - **id**：String 类型，表示文件记录的 ID。

- **返回值类型**：`boolean`，如果文件下载成功并保存至本地，返回 `true`,并输出打印`[DB Message]: File is successfully downloaded to xxx`；否则返回 `false`。

  ```java
  CAE File = new CAE(filePath,true);
  if(File.GetFile(localPath,"HULL_MODEL_AND_INFORMATION_DB", "HULL_PARAMETER_INFO", "OFFSETS_TABLE", "SampleShip_KCS0001"));
  ```

  

##### UploadFile方法

```java
public boolean UploadFile(String localPath, String dbName, String tableName, String field, String id)
```

该方法用于上传单个文件到数据库中指定的路径，并同时在数据库中更新文件路径。

- **形参列表**
  - **localPath**：String 类型，表示本地文件路径。
  - **dbName**：String 类型，表示数据库名称。
  - **tableName**：String 类型，表示数据库表名称。
  - **field**：String 类型，表示字段名称。
  - **id**：String 类型，表示文件记录的 ID。
- **返回值类型**：`boolean`，如果文件上传成功并更新了数据库中的文件路径，则返回 `true`，并输出打印`[DB Message]: File is successfully uploaded`；否则返回 `false`。

```java
CAE File = new CAE(filePath,true);
if(File.UploadFile(uploadFile, "HULL_MODEL_AND_INFORMATION_DB", "HULL_PARAMETER_INFO", "TRANSVERSE_AREA_CURVE", "SampleShip_KCS0000"));
```



##### DeleteFile方法

```java
public boolean DeleteFile(String dbName, String tableName, String field, String id)
```

该方法根据数据库名称、表名、字段名和 ID 删除指定文件，并清空数据库中对应字段的内容。

- **形参列表**
  - **dbName**：String 类型，表示数据库名称。
  - **tableName**：String 类型，表示数据库表名称。
  - **field**：String 类型，表示字段名称。
  - **id**：String 类型，表示文件记录的 ID。
- **返回值类型**：`boolean`，如果文件删除成功，返回 `true`，并输出打印`[DB Message]: File is successfully deleted`；否则返回 `false`。

```java
CAE File = new CAE(filePath,true);
if(File.DeleteFile("HULL_MODEL_AND_INFORMATION_DB", "HULL_PARAMETER_INFO", "HULL_3D_MODEL", "SampleShip_JBC0000"));
```



##### DeleteRecorder方法

```java
public boolean DeleteRecord(String dbName, String tableName, String id)
```

根据数据库名称、表名和 ID 删除数据库记录，并移除与该记录相关的所有文件。

- **形参列表**
  - **dbName**：`String` 类型，表示数据库名称。
  - **tableName**：`String` 类型，表示表名。
  - **id**：`String` 类型，表示记录的 ID。
- **返回值类型**：`boolean`，若删除成功返回 `true`，并输出打印`[DB Message]: Recorder is successfully deleted`，否则返回 `false`。

```java
CAE File = new CAE(filePath,true);
if(File.DeleteRecord("HULL_MODEL_AND_INFORMATION_DB", "HULL_PARAMETER_INFO", "SampleShip_JBC0000"));
```



##### File_connClose方法

```java
public void File_connClose()
```

该方法用于关闭文件系统和数据库连接，以释放资源。

- **形参列表**
  - 无
- **返回值类型**：无



##### Query方法

此方法用于执行 SQL 查询语句，并将查询结果封装到 `ResultSetWrapper` 对象中。

```java
public boolean Query(String sql, ResultSetWrapper rsWrapper)
```

- **形参列表**
  
  - **sql**：需要执行的`SELECT`类型的SQL语句。
  - **rsWrapper**：结果集包装器对象，用于存放查询结果。
  
- **返回值类型**：boolean，如果查询成功，则返回true，否则返回false。

- 特别注意的是，针对特定JSON值的查询。
  - 可以调用JSON_VALUE()这一SQL函数，从JSON字符串中提取标量值。此函数会返回一个与JSON字符串中指定路径匹配的值。如果路径不存在或不是一条有效的路径，则会返回NULL。以下是一个包含中文key的SQL语句的demo样例：
  
    ```sql
    "select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.\"中文测试\"') = '测试值';"
    ```
  
  - JSON_VALUE(字段名, $.键名) = 对应值 ，以如下查询语句为案例，即，指定 `SPECIAL_ATTRIBUTE`字段内的JSON对象在 `CapacityPerson`键下的值为字符串 `'容量(人)'`时的记录
  
    ```java
    CAE db = new CAE(filePath);
    ResultSetWrapper rsWrapper = new ResultSetWrapper();
    //进行相关json字段的查询，并打印结果
    if(db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.CapacityPerson') = '容量(人)';", rsWrapper)){
        System.out.println("QUERY SUCCESS!");
        db.Display(rsWrapper);
    };
    ```



##### Update方法

执行用户提供的`UPDATE`类型SQL语句，对相关记录进行更新操作。

```java
public boolean Update(String sql)
```

- **形参列表**：
  - **sql**：String类型，需要执行的`UPDATE`类型的SQL语句。
- **返回类型**：boolean类型，如果更新成功则返回true，否则返回false。

```java
CAE db = new CAE(filePath);
//测试更新，并打印更新成功信息
if(db.Update("UPDATE SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF SET SYSTEM = '内燃机2' WHERE EQUIP_ID = 'LJ-2';"));
```



##### Delete方法

执行用户提供的`DELETE`类型SQL语句，对相关记录进行删除操作。

```java
public boolean Delete(String sql)
```

- 形参列表：
  - **sql**：String类型，需要执行的`DELETE`类型的SQL语句。
- 返回类型：boolean类型，如果删除成功则返回true，否则返回false。

```java
CAE db = new CAE(filePath);
//测试删除，并打印删除成功信息
if(db.Delete("DELETE FROM SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF WHERE EQUIP_ID = 'LJ-2';"));
```



##### Insert方法

执行用户提供的`INSERT`类型SQL语句，向库表中进行简单的插入功能。

```java
public boolean Insert(String sql)
```

- 形参列表：
  - **sql**：String类型，需要执行的`INSERT`类型的SQL语句。
- 返回类型：boolean类型，插入成功则返回true，否则返回false。

```java
CAE db = new CAE(filePath);
//测试插入，并打印插入成功信息
if(db.Insert("INSERT INTO SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF (\n" +
     "    EQUIP_ID,\n" +
     "    MAJOR,\n" +
     "    SYSTEM,\n" +
     "    CATEGORY,\n" +
     "    SPECIAL_ATTRIBUTE,\n" +
     "    REMARK,\n" +
     "    RECORD\n" +
     ") VALUES (\n" +
     "    'LJ-2',\n" +
     "    '轮机',\n" +
     "    '内燃机',\n" +
     "    '主机',\n" +
     "    '{\"cylinderNum\":\"缸数\",\"cylinderDiameter\":\"缸径(mm)\",\"stroke\":\"冲程(mm)\",\"ratedPower\":\"额定功率(kW)\",\"ratedSpeed\":\"额定转速(rpm)\",\"smcrPower\":\"SMCR功率(kW)\",\"smcrSpeed\":\"SMCR转速(rpm)\",\"smcrFuelUsed\":\"SMCR油耗(g/kWh)\",\"ncrPower\":\"NCR功率(kW)\",\"ncrSpeed\":\"NCR转速(rpm)\",\"ncrFuelUsed\":\"NCR油耗(g/kWh)\",\"greaseFuelUsed\":\"滑油油耗(kg/d)\",\"cylinderFuelUsed\":\"汽缸油耗(g/kWh)\",\"oilPump\":\"供油泵(m3/h)\",\"oilPumpHead\":\"供油泵压头(bar)\",\"stressPump\":\"增压泵(m3/h)\",\"stressPumpHead\":\"增压泵压头(bar)\",\"greasePump\":\"滑油泵(m3/h)\",\"greasePumpHead\":\"滑油泵压头(bar)\",\"greaseTank\":\"滑油循环舱(m3)\",\"greasePurifierFlow\":\"滑油分油机流量(L/h)\",\"middleHeatExchange\":\"中央热交换量(kW)\",\"middleWaterFlow\":\"中央冷却水流量(m3/h)\",\"cylinderHeatExchange\":\"缸套热交换量(kW)\",\"cylinderWaterFlow\":\"缸套冷却水流量(m3/h)\",\"greaseHeatExchange\":\"滑油热交换量(kW)\",\"greaseWaterFlow\":\"滑油冷却水流量(m3/h)\",\"airPump\":\"空压机(Nm3/h)\",\"airBottle\":\"空气瓶(m3)\",\"exhaustDiameter\":\"排气管径(mm)\"}',\n" +
    "    NULL, \n" +
    "    NULL  \n" +
    ");"));
```



##### Display方法

显示查询结果 `ResultSetWrapper` 对象中的元数据，包括列数和每列的数据类型；显示列标题；遍历结果集中的每一行记录，并按列数据类型格式化输出。

```java
public void Display(ResultSetWrapper rsWrapper)
```

- 形参列表：
  - **rsWrapper**：`ResultSetWrapper` 类型，结果集包装器对象，用于展示查询结果。
- 返回类型：void

```java
CAE db = new CAE(filePath);
ResultSetWrapper rsWrapper = new ResultSetWrapper();
//测试查询，并打印结果集
if (db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF;",rsWrapper));
```



##### setClose方法

执行完查询操作之后，关闭语句句柄和结果集。

```java
public void setClose(ResultSetWrapper rsWrapper)
```

- 形参列表：
  - **rsWrapper**：`ResultSetWrapper` 类型，结果集包装器对象。
- 返回类型：void

```java
CAE db = new CAE(filePath);
ResultSetWrapper rsWrapper = new ResultSetWrapper();
//测试查询，并打印结果集，关闭查询语句句柄，结果集
if (db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF;",rsWrapper)) {
    db.Display(rsWrapper);
    db.setClose(rsWrapper);
};
```



##### connClose方法

关闭与数据库的连接。

```java
public void connClose()
```

- 参数列表：无
- 返回类型：void

```java
CAE db = new CAE(filePath);
//关闭数据库连接
db.connClose();
```



------

## Maven构建指南与程序样例

#### 构建指南

- 新建lib文件夹，将CAE-v0.1.jar包放在该目录下
- 加载jar包以及依赖驱动，下面是一个简单的`pom.xml`示例，用于构建和运行Java项目：

```xml
<?xml version="1.0" encoding="GB18030"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
     <!-- 模型版本 -->
    <modelVersion>4.0.0</modelVersion>
    
	<!-- Maven坐标系统中的三个必填属性 -->
    <groupId>org.example</groupId>  <!-- 项目组织名 -->
    <artifactId>TestJar</artifactId>  <!-- 项目内部名称 -->
    <version>1.0-SNAPSHOT</version>  <!-- 项目版本号，SNAPSHOT表示快照版本 -->
	
    <!-- 属性设置 -->
    <properties>
        <maven.compiler.source>11</maven.compiler.source>  <!-- 编译器源代码兼容性 -->
        <maven.compiler.target>11</maven.compiler.target>  <!-- 编译器目标代码兼容性 -->
        <project.build.sourceEncoding>GB18030</project.build.sourceEncoding>  <!-- 源文件编码 -->
    </properties>
	
    <!-- jar包依赖关系 -->
    <dependencies>
        <dependency>
            <groupId>com.cae</groupId>  <!-- 依赖的项目组织名 -->
            <artifactId>CAE</artifactId>   <!-- 依赖的项目内部名称 -->
            <version>v0.1</version>   <!-- 依赖的版本号 -->
            <systemPath>${project.basedir}/lib/CAE-v0.1.jar</systemPath>  <!-- 本地系统的路径 -->
            <scope>system</scope>  <!-- 依赖的作用域，system表示依赖在本地系统上 -->
        </dependency>
    </dependencies>
    
</project>
```

- 使用以下代码语句导入CAE类和ResultSetWrapper类

  ```
  import com.cae.CAE;
  import com.cae.ResultSetWrapper;
  ```



#### 程序样例

下面是一个简单的示例，展示了如何使用jar包中的`CAE`类以及`ResultSetWrapper`类来执行基本的数据库操作：

```java
package TestJar;
import com.cae.CAE;
import com.cae.ResultSetWrapper;


public class main {
    public static void main(String[] args) {
        //config文件路径
        String filePath = "D:\\idea_workspace\\TestJar\\src\\main\\resources\\config.yaml";
        CAE db = new CAE(filePath);

        // 测试插入
        if(db.Insert("INSERT INTO SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF (\n" +
                "    EQUIP_ID,\n" +
                "    MAJOR,\n" +
                "    SYSTEM,\n" +
                "    CATEGORY,\n" +
                "    SPECIAL_ATTRIBUTE,\n" +
                "    REMARK,\n" +
                "    RECORD\n" +
                ") VALUES (\n" +
                "    'LJ-2',\n" +
                "    '轮机',\n" +
                "    '内燃机',\n" +
                "    '主机',\n" +
                "    '{\"cylinderNum\":\"缸数\",\"cylinderDiameter\":\"缸径(mm)\",\"stroke\":\"冲程(mm)\",\"ratedPower\":\"额定功率(kW)\",\"ratedSpeed\":\"额定转速(rpm)\",\"smcrPower\":\"SMCR功率(kW)\",\"smcrSpeed\":\"SMCR转速(rpm)\",\"smcrFuelUsed\":\"SMCR油耗(g/kWh)\",\"ncrPower\":\"NCR功率(kW)\",\"ncrSpeed\":\"NCR转速(rpm)\",\"ncrFuelUsed\":\"NCR油耗(g/kWh)\",\"greaseFuelUsed\":\"滑油油耗(kg/d)\",\"cylinderFuelUsed\":\"汽缸油耗(g/kWh)\",\"oilPump\":\"供油泵(m3/h)\",\"oilPumpHead\":\"供油泵压头(bar)\",\"stressPump\":\"增压泵(m3/h)\",\"stressPumpHead\":\"增压泵压头(bar)\",\"greasePump\":\"滑油泵(m3/h)\",\"greasePumpHead\":\"滑油泵压头(bar)\",\"greaseTank\":\"滑油循环舱(m3)\",\"greasePurifierFlow\":\"滑油分油机流量(L/h)\",\"middleHeatExchange\":\"中央热交换量(kW)\",\"middleWaterFlow\":\"中央冷却水流量(m3/h)\",\"cylinderHeatExchange\":\"缸套热交换量(kW)\",\"cylinderWaterFlow\":\"缸套冷却水流量(m3/h)\",\"greaseHeatExchange\":\"滑油热交换量(kW)\",\"greaseWaterFlow\":\"滑油冷却水流量(m3/h)\",\"airPump\":\"空压机(Nm3/h)\",\"airBottle\":\"空气瓶(m3)\",\"exhaustDiameter\":\"排气管径(mm)\"}',\n" +
                "    NULL, \n" +
                "    NULL  \n" +
                ");")){
            System.out.println("INSERT SUCCESS!");
        };

        // 测试更新
        if(db.Update("UPDATE SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF SET SYSTEM = '内燃机2' WHERE EQUIP_ID = 'LJ-2';")){
            System.out.println("UPDATE SUCCESS!");
        };

        //json字段查询的案例
        ResultSetWrapper rsWrapper = new ResultSetWrapper();
        if(db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.\"中文测试\"') = '测试值';", rsWrapper)){
            System.out.println("QUERY SUCCESS!");
            db.Display(rsWrapper);
        };
        
        //测试json字段的查询
        if(db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.AnchorType') = '类型';", rsWrapper)){
            System.out.println("QUERY SUCCESS!");
            db.Display(rsWrapper);
        };
        

        //测试查询--关键设备库
        if (db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF;",rsWrapper)) {
            db.Display(rsWrapper);
            db.setClose(rsWrapper);
        };

        // 测试删除
        if(db.Delete("DELETE FROM SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF WHERE EQUIP_ID = 'LJ-2';")){
            System.out.println("DELETE SUCCESS!");
        };

        db.connClose();
    }
}
```





