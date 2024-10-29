package com.cae;


public class main {
    public static void main(String[] args) {
        String filePath = "D:\\idea_workspace\\TestJar\\src\\main\\resources\\config.yaml";
        CAE db = new CAE(filePath);

        // 测试删除
//        if(db.Delete("DELETE FROM basic_ship_information_DB.ship_data_info WHERE ship_data_id = 7082003")){
//            System.out.println("DELETE SUCCESS!");
//        };

        //json字段的查询，中文key的情况
        ResultSetWrapper rsWrapper = new ResultSetWrapper();
        if(db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.AnchorType') = '类型';", rsWrapper)){
            System.out.println("QUERY SUCCESS!");
            db.Display(rsWrapper);
        };

        if(db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.CapacityPerson') = '容量(人)';", rsWrapper)){
            System.out.println("QUERY SUCCESS!");
            db.Display(rsWrapper);
        };

        // 测试插入
//        if(db.Insert("INSERT INTO basic_ship_information_DB.ship_data_info (\n" +
//                "    SHIP_DATA_ID, \n" +
//                "    SHIP_NAME, \n" +
//                "    SHIP_TYPE, \n" +
//                "    BUILT, \n" +
//                "    SPEED_SERVICE, \n" +
//                "    OWNER, \n" +
//                "    CLASSIFICATION_DATA, \n" +
//                "    CLASSIFICATION_SOCIETY\n" +
//                ") VALUES (\n" +
//                "    7082001, \n" +
//                "    '航海3号', \n" +
//                "    '油船3', \n" +
//                "    TO_DATE('2020-10-03', 'YYYY-MM-DD'), \n" +
//                "    24.3, \n" +
//                "    '航运有限公司', \n" +
//                "    TO_DATE('2020-11-03', 'YYYY-MM-DD'), \n" +
//                "    '中国船级社CSS'\n" +
//                ");")){
//            System.out.println("INSERT SUCCESS!");
//        };

        //测试查询--设备库
        if (db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF;",rsWrapper)) {
            db.Display(rsWrapper);
            db.setClose(rsWrapper);
        };

        //测试查询
        if (db.Query("SELECT * FROM basic_ship_information_DB.ship_data_info;",rsWrapper)) {
            db.Display(rsWrapper);
            db.setClose(rsWrapper);
        };

        // 测试更新
//        if(db.Update("UPDATE basic_ship_information_DB.ship_data_info SET speed_service = 24.0 WHERE ship_data_id = 7082001;")){
//            System.out.println("UPDATE SUCCESS!");
//        };

//        db.connectTest(filePath);
        db.connClose();
    }
}