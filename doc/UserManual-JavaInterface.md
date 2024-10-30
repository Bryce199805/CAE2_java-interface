# Java���ݽӿ��û��ֲ�

```
Java���ݽӿ�Ϊ�û��ṩֱ�Ӳ����������ݿ�����API�ӿڣ�����ӵ�й���ԱȨ�޵��û������ݿ�����ؿ�������ɾ�Ĳ������
```

### Ŀ¼˵��

- **CAE-v0.1.jar** �������ݽӿ�������Ŀ��ļ��Լ��������
- **UserManual-JavaInterface.md** �����ĵ���Java���ݽӿ��û��ֲ�
- **interface-config.yaml** ���ݿ����ò����ļ�

### ��������

- **���ı���**��������Ϊ`gb18030`��

- **������Ϣ**���뽫`config.yaml`�е�������Ϣ�޸�Ϊ������Ӧ����Ϣ��

  ### **config.yaml**

```json
database:
  server: "222.27.255.211:15236"  # ���ݿ�����ַ ip:port
  username: "SYSDBA"              # ���ݿ��û���
  passwd: "SYSDBA"                # ���ݿ�����
```

------

## API �û��ֲ�

```java
API����CAE���ResultSetWrapper��
```

#### Class ResultSetWrapper

`ResultSetWrapper` �����ڰ�װ `ResultSet` �����ṩ��һ�ַ���ķ�ʽ������ͷ��� `ResultSet` ����

##### ��Ա����

- rs��Result���ͣ����ڴ洢��ѯ�������

##### ���캯��

```java
ResultSetWrapper()
```

��ʼ��ResultSetWrapper����Ϊnull��

- �����б���
- �������ͣ���



##### setRs����

����ResultSetWrapper�е�ResultSet��������󣬽������ResultSetWrapper����ֵ����Ա����rs��

```java
public void setRs(ResultSet rs)
```

- �����б�
  - rs��ResultSet���ͣ�Ҫ���õ�ResultSet����
- �������ͣ�void



##### getRs����

��ȡResultSetWrapper�е�ResultSet���������

```java
public ResultSet getRs()
```

- �����б��ޡ�
- �������ͣ�ResultSet���ͣ����ص�ǰ��װ��ResultSet����



#### Class CAE

`CAE` ���װ�˶����ݿ�����Ļ�����ɾ�Ĳ鷽�������ṩ�˶Բ�ѯ����Ĵ�ӡ����������֤��

##### ���캯��

```java
public CAE(String filePath)
```

��ʼ�� `CAE` ��Ķ��󣬲���ָ���� yaml�ļ��м������ݿ����á����⣬�����᳢�Լ��� JDBC �������򣬲��������ݿ�������Ϣ���������ݿ����ӡ�

- �β��б�
  - **filePath**��String���ͣ�yaml�����ļ���ַ��

- ����ֵ���ͣ���

```java
String filePath = "xxx/config.yaml";
CAE db = new CAE(filePath);
```



##### Query����

�˷�������ִ�� SQL ��ѯ��䣬������ѯ�����װ�� `ResultSetWrapper` �����С�

```java
public boolean Query(String sql, ResultSetWrapper rsWrapper)
```

- �β��б�
  - **sql**����Ҫִ�е�`SELECT`���͵�SQL��䡣
  - **rsWrapper**���������װ���������ڴ�Ų�ѯ�����

- ����ֵ���ͣ�boolean�������ѯ�ɹ����򷵻�true�����򷵻�false��

- �ر�ע����ǣ�����ض�JSONֵ�Ĳ�ѯ��
  - ���Ե���JSON_VALUE()��һSQL��������JSON�ַ�������ȡ����ֵ���˺����᷵��һ����JSON�ַ�����ָ��·��ƥ���ֵ�����·�������ڻ���һ����Ч��·������᷵��NULL��������һ����������key��SQL����demo������
  
    ```sql
    "select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.\"���Ĳ���\"') = '����ֵ';"
    ```
  
  - JSON_VALUE(�ֶ���, $.����) = ��Ӧֵ �������²�ѯ���Ϊ����������ָ�� `SPECIAL_ATTRIBUTE`�ֶ��ڵ�JSON������ `CapacityPerson`���µ�ֵΪ�ַ��� `'����(��)'`ʱ�ļ�¼
  
    ```java
    CAE db = new CAE(filePath);
    ResultSetWrapper rsWrapper = new ResultSetWrapper();
    //�������json�ֶεĲ�ѯ������ӡ���
    if(db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.CapacityPerson') = '����(��)';", rsWrapper)){
        System.out.println("QUERY SUCCESS!");
        db.Display(rsWrapper);
    };
    ```



##### Update����

ִ���û��ṩ��`UPDATE`����SQL��䣬����ؼ�¼���и��²�����

```java
public boolean Update(String sql)
```

- �β��б�
  - **sql**��String���ͣ���Ҫִ�е�`UPDATE`���͵�SQL��䡣
- �������ͣ�boolean���ͣ�������³ɹ��򷵻�true�����򷵻�false��

```java
CAE db = new CAE(filePath);
//���Ը��£�����ӡ���³ɹ���Ϣ
if(db.Update("UPDATE SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF SET SYSTEM = '��ȼ��2' WHERE EQUIP_ID = 'LJ-2';")){
    System.out.println("UPDATE SUCCESS!");
};
```



##### Delete����

ִ���û��ṩ��`DELETE`����SQL��䣬����ؼ�¼����ɾ��������

```java
public boolean Delete(String sql)
```

- �β��б�
  - **sql**��String���ͣ���Ҫִ�е�`DELETE`���͵�SQL��䡣
- �������ͣ�boolean���ͣ����ɾ���ɹ��򷵻�true�����򷵻�false��

```java
CAE db = new CAE(filePath);
//����ɾ��������ӡɾ���ɹ���Ϣ
if(db.Delete("DELETE FROM SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF WHERE EQUIP_ID = 'LJ-2';")){
    System.out.println("DELETE SUCCESS!");
};
```



##### Insert����

ִ���û��ṩ��`INSERT`����SQL��䣬�����н��м򵥵Ĳ��빦�ܡ�

```java
public boolean Insert(String sql)
```

- �β��б�
  - **sql**��String���ͣ���Ҫִ�е�`INSERT`���͵�SQL��䡣
- �������ͣ�boolean���ͣ�����ɹ��򷵻�true�����򷵻�false��

```java
CAE db = new CAE(filePath);
//���Բ��룬����ӡ����ɹ���Ϣ
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
     "    '�ֻ�',\n" +
     "    '��ȼ��',\n" +
     "    '����',\n" +
     "    '{\"cylinderNum\":\"����\",\"cylinderDiameter\":\"�׾�(mm)\",\"stroke\":\"���(mm)\",\"ratedPower\":\"�����(kW)\",\"ratedSpeed\":\"�ת��(rpm)\",\"smcrPower\":\"SMCR����(kW)\",\"smcrSpeed\":\"SMCRת��(rpm)\",\"smcrFuelUsed\":\"SMCR�ͺ�(g/kWh)\",\"ncrPower\":\"NCR����(kW)\",\"ncrSpeed\":\"NCRת��(rpm)\",\"ncrFuelUsed\":\"NCR�ͺ�(g/kWh)\",\"greaseFuelUsed\":\"�����ͺ�(kg/d)\",\"cylinderFuelUsed\":\"�����ͺ�(g/kWh)\",\"oilPump\":\"���ͱ�(m3/h)\",\"oilPumpHead\":\"���ͱ�ѹͷ(bar)\",\"stressPump\":\"��ѹ��(m3/h)\",\"stressPumpHead\":\"��ѹ��ѹͷ(bar)\",\"greasePump\":\"���ͱ�(m3/h)\",\"greasePumpHead\":\"���ͱ�ѹͷ(bar)\",\"greaseTank\":\"����ѭ����(m3)\",\"greasePurifierFlow\":\"���ͷ��ͻ�����(L/h)\",\"middleHeatExchange\":\"�����Ƚ�����(kW)\",\"middleWaterFlow\":\"������ȴˮ����(m3/h)\",\"cylinderHeatExchange\":\"�����Ƚ�����(kW)\",\"cylinderWaterFlow\":\"������ȴˮ����(m3/h)\",\"greaseHeatExchange\":\"�����Ƚ�����(kW)\",\"greaseWaterFlow\":\"������ȴˮ����(m3/h)\",\"airPump\":\"��ѹ��(Nm3/h)\",\"airBottle\":\"����ƿ(m3)\",\"exhaustDiameter\":\"�����ܾ�(mm)\"}',\n" +
    "    NULL, \n" +
    "    NULL  \n" +
    ");")){
    System.out.println("INSERT SUCCESS!");
};
```



##### Display����

��ʾ��ѯ��� `ResultSetWrapper` �����е�Ԫ���ݣ�����������ÿ�е��������ͣ���ʾ�б��⣻����������е�ÿһ�м�¼���������������͸�ʽ�������

```java
public void Display(ResultSetWrapper rsWrapper)
```

- �β��б�
  - **rsWrapper**��`ResultSetWrapper` ���ͣ��������װ����������չʾ��ѯ�����
- �������ͣ�void

```java
CAE db = new CAE(filePath);
ResultSetWrapper rsWrapper = new ResultSetWrapper();
//���Բ�ѯ������ӡ�����
if (db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF;",rsWrapper)) {
    db.Display(rsWrapper);
};
```



##### setClose����

ִ�����ѯ����֮�󣬹ر�������ͽ������

```java
public void setClose(ResultSetWrapper rsWrapper)
```

- �β��б�
  - **rsWrapper**��`ResultSetWrapper` ���ͣ��������װ������
- �������ͣ�void

```java
CAE db = new CAE(filePath);
ResultSetWrapper rsWrapper = new ResultSetWrapper();
//���Բ�ѯ������ӡ��������رղ�ѯ������������
if (db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF;",rsWrapper)) {
    db.Display(rsWrapper);
    db.setClose(rsWrapper);
};
```



##### connClose����

�ر������ݿ�����ӡ�

```java
public void connClose()
```

- �����б���
- �������ͣ�void

```java
CAE db = new CAE(filePath);
//�ر����ݿ�����
db.connClose();
```



------

## Maven����ָ�����������

#### ����ָ��

- �½�lib�ļ��У���CAE-1.0.jar�����ڸ�Ŀ¼��
- ����jar���Լ�����������������һ���򵥵�`pom.xml`ʾ�������ڹ���������Java��Ŀ��

```xml
<?xml version="1.0" encoding="GB18030"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
     <!-- ģ�Ͱ汾 -->
    <modelVersion>4.0.0</modelVersion>
    
	<!-- Maven����ϵͳ�е������������� -->
    <groupId>org.example</groupId>  <!-- ��Ŀ��֯�� -->
    <artifactId>TestJar</artifactId>  <!-- ��Ŀ�ڲ����� -->
    <version>1.0-SNAPSHOT</version>  <!-- ��Ŀ�汾�ţ�SNAPSHOT��ʾ���հ汾 -->
	
    <!-- �������� -->
    <properties>
        <maven.compiler.source>11</maven.compiler.source>  <!-- ������Դ��������� -->
        <maven.compiler.target>11</maven.compiler.target>  <!-- ������Ŀ���������� -->
        <project.build.sourceEncoding>GB18030</project.build.sourceEncoding>  <!-- Դ�ļ����� -->
    </properties>
	
    <!-- jar��������ϵ -->
    <dependencies>
        <dependency>
            <groupId>com.cae</groupId>  <!-- ��������Ŀ��֯�� -->
            <artifactId>CAE</artifactId>   <!-- ��������Ŀ�ڲ����� -->
            <version>v0.1</version>   <!-- �����İ汾�� -->
            <systemPath>${project.basedir}/lib/CAE-v0.1.jar</systemPath>  <!-- ����ϵͳ��·�� -->
            <scope>system</scope>  <!-- ������������system��ʾ�����ڱ���ϵͳ�� -->
        </dependency>
    </dependencies>
    
</project>
```

- ʹ�����´�����䵼��CAE���ResultSetWrapper��

  ```
  import com.cae.CAE;
  import com.cae.ResultSetWrapper;
  ```



#### ��������

������һ���򵥵�ʾ����չʾ�����ʹ��jar���е�`CAE`���Լ�`ResultSetWrapper`����ִ�л��������ݿ������

```java
package TestJar;
import com.cae.CAE;
import com.cae.ResultSetWrapper;


public class main {
    public static void main(String[] args) {
        //config�ļ�·��
        String filePath = "D:\\idea_workspace\\TestJar\\src\\main\\resources\\config.yaml";
        CAE db = new CAE(filePath);

        // ���Բ���
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
                "    '�ֻ�',\n" +
                "    '��ȼ��',\n" +
                "    '����',\n" +
                "    '{\"cylinderNum\":\"����\",\"cylinderDiameter\":\"�׾�(mm)\",\"stroke\":\"���(mm)\",\"ratedPower\":\"�����(kW)\",\"ratedSpeed\":\"�ת��(rpm)\",\"smcrPower\":\"SMCR����(kW)\",\"smcrSpeed\":\"SMCRת��(rpm)\",\"smcrFuelUsed\":\"SMCR�ͺ�(g/kWh)\",\"ncrPower\":\"NCR����(kW)\",\"ncrSpeed\":\"NCRת��(rpm)\",\"ncrFuelUsed\":\"NCR�ͺ�(g/kWh)\",\"greaseFuelUsed\":\"�����ͺ�(kg/d)\",\"cylinderFuelUsed\":\"�����ͺ�(g/kWh)\",\"oilPump\":\"���ͱ�(m3/h)\",\"oilPumpHead\":\"���ͱ�ѹͷ(bar)\",\"stressPump\":\"��ѹ��(m3/h)\",\"stressPumpHead\":\"��ѹ��ѹͷ(bar)\",\"greasePump\":\"���ͱ�(m3/h)\",\"greasePumpHead\":\"���ͱ�ѹͷ(bar)\",\"greaseTank\":\"����ѭ����(m3)\",\"greasePurifierFlow\":\"���ͷ��ͻ�����(L/h)\",\"middleHeatExchange\":\"�����Ƚ�����(kW)\",\"middleWaterFlow\":\"������ȴˮ����(m3/h)\",\"cylinderHeatExchange\":\"�����Ƚ�����(kW)\",\"cylinderWaterFlow\":\"������ȴˮ����(m3/h)\",\"greaseHeatExchange\":\"�����Ƚ�����(kW)\",\"greaseWaterFlow\":\"������ȴˮ����(m3/h)\",\"airPump\":\"��ѹ��(Nm3/h)\",\"airBottle\":\"����ƿ(m3)\",\"exhaustDiameter\":\"�����ܾ�(mm)\"}',\n" +
                "    NULL, \n" +
                "    NULL  \n" +
                ");")){
            System.out.println("INSERT SUCCESS!");
        };

        // ���Ը���
        if(db.Update("UPDATE SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF SET SYSTEM = '��ȼ��2' WHERE EQUIP_ID = 'LJ-2';")){
            System.out.println("UPDATE SUCCESS!");
        };

        //json�ֶβ�ѯ�İ���
        ResultSetWrapper rsWrapper = new ResultSetWrapper();
        if(db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.\"���Ĳ���\"') = '����ֵ';", rsWrapper)){
            System.out.println("QUERY SUCCESS!");
            db.Display(rsWrapper);
        };
        
        //����json�ֶεĲ�ѯ
        if(db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.AnchorType') = '����';", rsWrapper)){
            System.out.println("QUERY SUCCESS!");
            db.Display(rsWrapper);
        };
        

        //���Բ�ѯ--�ؼ��豸��
        if (db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF;",rsWrapper)) {
            db.Display(rsWrapper);
            db.setClose(rsWrapper);
        };

        // ����ɾ��
        if(db.Delete("DELETE FROM SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF WHERE EQUIP_ID = 'LJ-2';")){
            System.out.println("DELETE SUCCESS!");
        };

        db.connClose();
    }
}
```





