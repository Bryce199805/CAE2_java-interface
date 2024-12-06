package com.cae;

import java.io.InputStream;

public class main {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\Edwina\\Desktop\\JAVA\\CAE2_java-interface\\src\\main\\resources\\interface-config.yaml";
        //CAE db = new CAE(filePath);
        CAE File = new CAE(filePath,true);
        String localPath= "C:\\Users\\Edwina\\Desktop\\JAVA\\CAE2_java-interface\\File-test";
        String uploadFile = "C:\\Users\\Edwina\\Desktop\\JAVA\\CAE2_java-interface\\File-test\\7082001-船壳三维模型文件.igs";

        //测试下载单个文件√
//        if(File.GetFile(localPath,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","TRANSVERSE_AREA_CURVE","SampleShip_KCS0000")){
//            System.out.println("下载成功！");
//        }
//        //测试下载单个文件，不存在的ID
//        if(File.GetFile(localPath,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","M7081001")){
//            System.out.println("下载成功！");
//        }
//        //测试下载单个文件，文件数据为空
//        if(File.GetFile(localPath,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_3D_MODEL","SampleShip_VLCC0000")){
//            System.out.println("下载成功！");
//        }
//        //测试下载单个文件，不涉及文件数据
//        if(File.GetFile(localPath,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_ID","SampleShip_KCS0000")){
//            System.out.println("下载成功！");
//        }


        //测试下载单个文件字符流√
//        InputStream inputStream = File.GetFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","SampleShip_KCS0000");
//        if(inputStream != null){
//            System.out.println("GET STREAM SUCCESS！");
//        }
//        //测试下载不存在的ID
//        InputStream inputStream1 = File.GetFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_3D_MODEL","M7081001");
//        if(inputStream1 != null){
//            System.out.println("GET STREAM SUCCESS！");
//        }
//        //测试下载文件数据为空
//        InputStream inputStream2 = File.GetFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_3D_MODEL","SampleShip_VLCC0000");
//        if(inputStream2 != null){
//            System.out.println("GET STREAM SUCCESS！");
//        }
//        //测试下载，不涉及文件数据
//        InputStream inputStream3 = File.GetFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_ID","SampleShip_KCS0000");
//        if(inputStream3 != null){
//            System.out.println("GET STREAM SUCCESS！");
//        }
//        try {
//            inputStream.close();
//            inputStream1.close();
//            inputStream2.close();
//            inputStream3.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            //System.err.println("Exception occurred: " + e.getMessage());
//        }

        //测试上传√
//        if(File.UploadFile(uploadFile,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","SampleShip_KCS0000")){
//            System.out.println("上传成功！");
//        }
//        //测试上传，不存在的记录
//        if(File.UploadFile(uploadFile,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","M7081001")){
//            System.out.println("上传成功！");
//        }
//        //测试上传，文件字段内容为空，也可以成功！
//        if(File.UploadFile(uploadFile,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_3D_MODEL","SampleShip_VLCC0000")){
//            System.out.println("上传成功！");
//        }
//        //测试上传，不是文件字段
//        if(File.UploadFile(uploadFile,"HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","AFTER_SHAPE","SampleShip_KCS0000")){
//            System.out.println("上传成功！");
//        }

        //测试正常删除一条记录√
//        if(File.DeleteRecord("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","SampleShip_KCS0000")){
//            System.out.println("删除记录成功！");
//        }
//        //测试删除一条不存在的记录
//        if(File.DeleteRecord("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","M7081003")){
//            System.out.println("删除记录成功！");
//        }
//        //测试删除文件数据为空的记录
//        if(File.DeleteRecord("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","SampleShip_VLCC0000")){
//            System.out.println("删除记录成功！");
//        }
//        //该记录没有涉及文件数据
//        if(File.DeleteRecord("HULL_MODEL_AND_INFORMATION_DB","PERFORMANCE_INFO","SampleShip_KCS0000")){
//            System.out.println("删除记录成功！");
//        }


        //测试正常删除√
//        if(File.DeleteFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","OFFSETS_TABLE","SampleShip_VLCC0000")){
//            System.out.println("删除成功！");
//        }
//        if(File.DeleteFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","TRANSVERSE_AREA_CURVE","SampleShip_VLCC0000")){
//            System.out.println("删除成功！");
//        }
//        //测试删除，不存在的记录
//        if(File.DeleteFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_3D_MODEL","M7081004")){
//            System.out.println("删除成功！");
//        }
//        //测试删除，文件字段内容为空
//        if(File.DeleteFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","HULL_3D_MODEL","SampleShip_VLCC0000")){
//            System.out.println("删除成功！");
//        }
//        //测试删除，不是文件字段
//        if(File.DeleteFile("HULL_MODEL_AND_INFORMATION_DB","HULL_PARAMETER_INFO","AFTER_SHAPE","SampleShip_KCS0000")){
//            System.out.println("删除成功！");
//        }




        //测试查询--基本船型库
//        ResultSetWrapper rsWrapper = new ResultSetWrapper();
//        if (File.Query("select * from BASIC_SHIP_INFORMATION_DB.SHIP_DATA_INFO where ship_type = '油船';",rsWrapper)) {
//            File.Display(rsWrapper);
//            File.setClose(rsWrapper);
//        };
//
//        //测试查询--关键设备库
//        if (File.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF;",rsWrapper)) {
//            File.Display(rsWrapper);
//            File.setClose(rsWrapper);
//        };

        /**
         * 达梦数据库操作测试 - File对象
         */

        // 测试插入
//        if(File.Insert("INSERT INTO SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF (\n" +
//                "    EQUIP_ID,\n" +
//                "    MAJOR,\n" +
//                "    SYSTEM,\n" +
//                "    CATEGORY,\n" +
//                "    SPECIAL_ATTRIBUTE,\n" +
//                "    REMARK,\n" +
//                "    RECORD\n" +
//                ") VALUES (\n" +
//                "    'LJ-2',\n" +
//                "    '轮机',\n" +
//                "    '内燃机',\n" +
//                "    '主机',\n" +
//                "    '{\"cylinderNum\":\"缸数\",\"cylinderDiameter\":\"缸径(mm)\",\"stroke\":\"冲程(mm)\",\"ratedPower\":\"额定功率(kW)\",\"ratedSpeed\":\"额定转速(rpm)\",\"smcrPower\":\"SMCR功率(kW)\",\"smcrSpeed\":\"SMCR转速(rpm)\",\"smcrFuelUsed\":\"SMCR油耗(g/kWh)\",\"ncrPower\":\"NCR功率(kW)\",\"ncrSpeed\":\"NCR转速(rpm)\",\"ncrFuelUsed\":\"NCR油耗(g/kWh)\",\"greaseFuelUsed\":\"滑油油耗(kg/d)\",\"cylinderFuelUsed\":\"汽缸油耗(g/kWh)\",\"oilPump\":\"供油泵(m3/h)\",\"oilPumpHead\":\"供油泵压头(bar)\",\"stressPump\":\"增压泵(m3/h)\",\"stressPumpHead\":\"增压泵压头(bar)\",\"greasePump\":\"滑油泵(m3/h)\",\"greasePumpHead\":\"滑油泵压头(bar)\",\"greaseTank\":\"滑油循环舱(m3)\",\"greasePurifierFlow\":\"滑油分油机流量(L/h)\",\"middleHeatExchange\":\"中央热交换量(kW)\",\"middleWaterFlow\":\"中央冷却水流量(m3/h)\",\"cylinderHeatExchange\":\"缸套热交换量(kW)\",\"cylinderWaterFlow\":\"缸套冷却水流量(m3/h)\",\"greaseHeatExchange\":\"滑油热交换量(kW)\",\"greaseWaterFlow\":\"滑油冷却水流量(m3/h)\",\"airPump\":\"空压机(Nm3/h)\",\"airBottle\":\"空气瓶(m3)\",\"exhaustDiameter\":\"排气管径(mm)\"}',\n" +
//                "    NULL, \n" +
//                "    NULL  \n" +
//                ");")){
//            System.out.println("INSERT SUCCESS!");
//        };
//
//        // 测试更新
//        if(File.Update("UPDATE SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF SET SYSTEM = '内燃机2' WHERE EQUIP_ID = 'LJ-2';")){
//            System.out.println("UPDATE SUCCESS!");
//        };
//
//        //json字段的查询，中文key的情况
//        ResultSetWrapper rsWrapper = new ResultSetWrapper();
//        if(File.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF where JSON_VALUE(SPECIAL_ATTRIBUTE, '$.AnchorType') = '类型';", rsWrapper)){
//            System.out.println("QUERY SUCCESS!");
//            File.Display(rsWrapper);
//        };
//
//        //测试查询--关键设备库
//        if (File.Query("select * from SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF;",rsWrapper)) {
//            File.Display(rsWrapper);
//            File.setClose(rsWrapper);
//        };
//
//        // 测试删除
//        if(File.Delete("DELETE FROM SHIP_EQUIPMENT_INFO_DB.EQUI_CLASSIFY_PARADEF WHERE EQUIP_ID = 'LJ-2';")){
//            System.out.println("DELETE SUCCESS!");
//        };

       //关闭文件系统连接
        File.File_connClose();

        /**
         * 达梦数据库操作测试 - db对象
         */
        // 测试插入
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
        
        db.connClose();*/
    }
}