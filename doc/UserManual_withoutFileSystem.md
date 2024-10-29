# Java数据接口用户手册

```
Java数据接口为用户提供直接操作达梦数据库的相关API接口，便于拥有管理员权限的用户对数据库中相关库表进行增删改查操作。
```

### 目录说明

- **lib** 包含数据接口所打包的库文件以及相关依赖
- **src/main/TestJar/main** 测试主代码
- **src/main/TestJar/resources** 数据库配置参数文件

### 环境配置

- **中文编码**：请设置为`gb18030`。

- **环境变量**：确保项目路径中的类路径已被添加到环境变量中。

- **配置信息**：请将`config.yaml`中的配置信息修改为本机对应的信息。

  ### **config.yaml**

```json
database:
  server: "222.27.255.211:15236"  # 数据库服务地址 ip:port
  username: "SYSDBA"              # 数据库用户名
  passwd: "SYSDBA"                # 数据库密码
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



#### Class CAE

`CAE` 类封装了对数据库操作的基本增删改查方法，并提供了对查询结果的打印方法用于验证。

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



##### Query方法

此方法用于执行 SQL 查询语句，并将查询结果封装到 `ResultSetWrapper` 对象中。

```java
public boolean Query(String sql, ResultSetWrapper rsWrapper)
```

- 形参列表
  - **sql**：需要执行的`SELECT`类型的SQL语句。
  - **rsWrapper**：结果集包装器对象，用于存放查询结果。

- 返回值类型：boolean，如果查询成功，则返回true，否则返回false。

- 特别注意的是，针对特定JSON值的查询。
  - 可以调用JSON_VALUE()这一SQL函数，从JSON字符串中提取标量值。此函数会返回一个与JSON字符串中指定路径匹配的值。如果路径不存在或不是一条有效的路径，则会返回NULL。
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

- 形参列表：
  - **sql**：String类型，需要执行的`UPDATE`类型的SQL语句。
- 返回类型：boolean类型，如果更新成功则返回true，否则返回false。

```java
CAE db = new CAE(filePath);
//测试更新，并打印更新成功信息
if(db.Update("UPDATE basic_ship_information_DB.ship_data_info SET speed_service = 24.0 WHERE ship_data_id = 7082001;")){            
			System.out.println("UPDATE SUCCESS!");
            };

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
        if(db.Delete("DELETE FROM basic_ship_information_DB.ship_data_info WHERE ship_data_id = 7082003")){
            System.out.println("DELETE SUCCESS!");
        };
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
if(db.Insert("INSERT INTO basic_ship_information_DB.ship_data_info (\n" +
      "    SHIP_DATA_ID, \n" +
      "    SHIP_NAME, \n" +
      "    SHIP_TYPE, \n" +
      "    BUILT, \n" +
      "    SPEED_SERVICE, \n" +
      "    OWNER, \n" +
      "    CLASSIFICATION_DATA, \n" +
      "    CLASSIFICATION_SOCIETY\n" +
      ") VALUES (\n" +
      "    7082001, \n" +
      "    '航海3号', \n" +
      "    '油船3', \n" +
      "    TO_DATE('2020-10-03', 'YYYY-MM-DD'), \n" +
      "    24.3, \n" +
      "    '航运有限公司', \n" +
      "    TO_DATE('2020-11-03', 'YYYY-MM-DD'), \n" +
      "    '中国船级社CSS'\n" +
      ");")){
            System.out.println("INSERT SUCCESS!");
        };
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
if (db.Query("SELECT * FROM basic_ship_information_DB.ship_data_info;",rsWrapper)) {
    db.Display(rsWrapper);
   };
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
if (db.Query("SELECT * FROM basic_ship_information_DB.ship_data_info;",rsWrapper)) {
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

- 将CAE-1.0.jar包放在lib文件夹下
- 加载jar包以及依赖驱动，下面是一个简单的`pom.xml`示例，用于构建和运行Java项目：

```xml
<?xml version="1.0" encoding="GB18030"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>TestJar</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>GB18030</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.cae</groupId>
            <artifactId>CAE</artifactId>
            <version>1.0</version>
            <systemPath>${project.basedir}/lib/CAE-1.0.jar</systemPath>
            <scope>system</scope>
        </dependency>
    </dependencies>
    
</project>
```

- 你可以使用以下代码语句导入CAE类和ResultSetWrapper类

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

        //json字段的查询，中文key的情况
        ResultSetWrapper rsWrapper = new ResultSetWrapper();
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

//        db.connectTest(filePath);
        db.connClose();
    }
}
```





