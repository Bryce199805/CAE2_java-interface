package com.cae;

import java.io.InputStream;

public class main {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\Edwina\\Desktop\\JAVA\\CAE2_java-interface\\src\\main\\resources\\interface-config.yaml";
        CAE db = new CAE(filePath);
        CAE File = new CAE(filePath,true);
        String localPath= "C:\\Users\\Edwina\\Desktop\\JAVA\\CAE2_java-interface\\File-test";
        String uploadFile = "C:\\Users\\Edwina\\Desktop\\JAVA\\CAE2_java-interface\\File-test\\7082001-������άģ���ļ�.igs";

        //�������ص����ļ���
//        if(File.GetFile(localPath,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","M7081004")){
//            System.out.println("���سɹ���");
//        }
//        //�������ص����ļ��������ڵ�ID
//        if(File.GetFile(localPath,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","M7081001")){
//            System.out.println("���سɹ���");
//        }
//        //�������ص����ļ����ļ�����Ϊ��
//        if(File.GetFile(localPath,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_3D_MODEL","M7081004")){
//            System.out.println("���سɹ���");
//        }
//        //�������ص����ļ������漰�ļ�����
//        if(File.GetFile(localPath,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_ID","M7081004")){
//            System.out.println("���سɹ���");
//        }


        //�������ص����ļ��ַ�����
//        InputStream inputStream = File.GetFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","M7081004");
//        if(inputStream != null){
//            System.out.println("GET STREAM SUCCESS��");
//        }
//        //�������ز����ڵ�ID
//        InputStream inputStream1 = File.GetFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_3D_MODEL","M7081001");
//        if(inputStream1 != null){
//            System.out.println("GET STREAM SUCCESS��");
//        }
//        //���������ļ�����Ϊ��
//        InputStream inputStream2 = File.GetFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_3D_MODEL","M7081004");
//        if(inputStream2 != null){
//            System.out.println("GET STREAM SUCCESS��");
//        }
//        //�������أ����漰�ļ�����
//        InputStream inputStream3 = File.GetFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_ID","M7081004");
//        if(inputStream3 != null){
//            System.out.println("GET STREAM SUCCESS��");
//        }
//        try {
//            inputStream.close();
//            inputStream1.close();
//            inputStream2.close();
//            inputStream3.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        //��������ɾ��һ����¼��
//        if(File.DeleteRecord("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","M7081004")){
//            System.out.println("ɾ����¼�ɹ���");
//        }
//        //����ɾ��һ�������ڵļ�¼
//        if(File.DeleteRecord("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","M7081003")){
//            System.out.println("ɾ����¼�ɹ���");
//        }
//        //����ɾ���ļ�����Ϊ�յļ�¼
//        if(File.DeleteRecord("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","SampleShip_JBC0000")){
//            System.out.println("ɾ����¼�ɹ���");
//        }
//        //�ü�¼û���漰�ļ�����
//        if(File.DeleteRecord("HULL_MODEL_AND_INFORMATION_DB","PERFORMANCE_INFO","M7081004")){
//            System.out.println("ɾ����¼�ɹ���");
//        }


        //��������ɾ����
//        if(File.DeleteFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","SampleShip_KCS0000")){
//            System.out.println("ɾ���ɹ���");
//        }
//        //����ɾ���������ڵļ�¼
//        if(File.DeleteFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_3D_MODEL","M7081003")){
//            System.out.println("ɾ���ɹ���");
//        }
//        //����ɾ�����ļ��ֶ�����Ϊ��
//        if(File.DeleteFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_3D_MODEL","M7081004")){
//            System.out.println("ɾ���ɹ���");
//        }
//        //����ɾ���������ļ��ֶ�
//        if(File.DeleteFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","AFTER_SHAPE","M7081004")){
//            System.out.println("ɾ���ɹ���");
//        }


        //�����ϴ���
//        if(File.UploadFile(uploadFile,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","M7081004")){
//            System.out.println("�ϴ��ɹ���");
//        }
//        //�����ϴ��������ڵļ�¼
//        if(File.UploadFile(uploadFile,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","M7081001")){
//            System.out.println("�ϴ��ɹ���");
//        }
//        //�����ϴ����ļ��ֶ�����Ϊ�գ�Ҳ���Գɹ���
//        if(File.UploadFile(uploadFile,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","M7081004")){
//            System.out.println("�ϴ��ɹ���");
//        }
//        //�����ϴ��������ļ��ֶ�
//        if(File.UploadFile(uploadFile,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","AFTER_SHAPE","M7081004")){
//            System.out.println("�ϴ��ɹ���");
//        }

        //���Բ�ѯ--�ؼ��豸��
//        ResultSetWrapper rsWrapper = new ResultSetWrapper();
//        if (File.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF;",rsWrapper)) {
//            File.Display(rsWrapper);
//            File.setClose(rsWrapper);
//        };
//       //�ر��ļ�ϵͳ����
//        File.File_connClose();


        // ���Բ���
        /*if(db.Insert("INSERT INTO SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF (\n" +
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

        //json�ֶεĲ�ѯ������key�����
        ResultSetWrapper rsWrapper = new ResultSetWrapper();
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
        
        db.connClose();*/
    }
}