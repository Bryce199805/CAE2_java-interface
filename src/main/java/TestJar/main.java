package TestJar;
import com.cae.CAE;
import com.cae.ResultSetWrapper;


public class main {
    public static void main(String[] args) {
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
                "    '',\n" +
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

        //json�ֶεĲ�ѯ������key�����
        ResultSetWrapper rsWrapper = new ResultSetWrapper();
        if(db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.AnchorType') = '����';", rsWrapper)){
            System.out.println("QUERY SUCCESS!");
            db.Display(rsWrapper);
        };

        if(db.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.CapacityPerson') = '����(��)';", rsWrapper)){
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

//        db.connectTest(filePath);
        db.connClose();
    }
}