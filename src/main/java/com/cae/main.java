package com.cae;

import java.sql.SQLException;

public class main {
    public static void main(String[] args) throws SQLException {
        String filePath = "D:\\idea_workspace\\java01\\src\\main\\resources\\config.yaml";
        CAE db = new CAE(filePath);

        // ����ɾ��
        if(db.Delete("DELETE FROM basic_ship_information_DB.ship_data_info WHERE ship_data_id = 7082005")){
            System.out.println("DELETE SUCCESS!");
        };

        // ���Բ���
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
                "    7082003, \n" +
                "    '����3��', \n" +
                "    '�ʹ�3', \n" +
                "    TO_DATE('2020-10-03', 'YYYY-MM-DD'), \n" +
                "    24.3, \n" +
                "    '�������޹�˾', \n" +
                "    TO_DATE('2020-11-03', 'YYYY-MM-DD'), \n" +
                "    '�й�������CSS'\n" +
                ");")){
            System.out.println("INSERT SUCCESS!");
        };

        //���Բ�ѯ
        ResultSetWrapper rsWrapper = new ResultSetWrapper();
        if (db.Query("SELECT * FROM basic_ship_information_DB.ship_data_info;",rsWrapper)) {
            db.Display(rsWrapper);
            db.Set_close(rsWrapper);
        };

        // ���Ը���
        if(db.Update("UPDATE basic_ship_information_DB.ship_data_info SET speed_service = 24.0 WHERE ship_data_id = 7082001;")){
            System.out.println("UPDATE SUCCESS!");
        };

        db.connectTest(filePath);
        db.Conn_close();
    }
}
