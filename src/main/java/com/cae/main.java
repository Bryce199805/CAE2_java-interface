package com.cae;

public class main {
    public static void main(String[] args) {
        String filePath = "D:\\idea_workspace\\java01\\src\\main\\resources\\config.yaml";
        DamengDB db = new DamengDB(filePath);

        // 测试插入
        if(db.executeSelectInsert("INSERT INTO basic_ship_information_DB.ship_data_info (\n" +
                "    SHIP_DATA_ID, \n" +
                "    SHIP_NAME, \n" +
                "    SHIP_TYPE, \n" +
                "    BUILT, \n" +
                "    SPEED_SERVICE, \n" +
                "    OWNER, \n" +
                "    CLASSIFICATION_DATA, \n" +
                "    CLASSIFICATION_SOCIETY\n" +
                ") VALUES (\n" +
                "    7082003, \n" +
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

        //测试查询
        ResultSetWrapper rsWrapper = new ResultSetWrapper();
        if (db.executeSelectQueryV2("SELECT * FROM basic_ship_information_DB.ship_data_info;",rsWrapper)) {
            //db.displayResultSet(rsWrapper);
            db.closeResultSet(rsWrapper);
        }

        // 测试删除
        if(db.executeSelectDelete("DELETE FROM basic_ship_information_DB.ship_data_info WHERE ship_data_id = 7082003")){
            System.out.println("DELETE SUCCESS!");
        };

        // 测试更新
        if(db.executeSelectUpdate("UPDATE basic_ship_information_DB.ship_data_info SET speed_service = 24.0 WHERE ship_data_id = 7082001;")){
            System.out.println("UPDATE SUCCESS!");
        };

        db.connectTest(filePath);
        db.close();
    }
}
