package com.cae;

import java.util.ArrayList;
import java.util.List;

public class main {
    public static void main(String[] args) {
        DamengDB db = new DamengDB("D:\\idea_workspace\\java01\\src\\main\\resources\\config.yaml");

        // 测试查询
        List<List<String>> results = new ArrayList<>();
        if (db.query("SELECT * FROM your_table", results)) {
            db.printResult(results);
        }

        // 测试删除
        if (db.delete("DELETE FROM your_table WHERE id = 1")) {
            System.out.println("Delete operation successful.");
        }

        // 测试更新
        if (db.update("UPDATE your_table SET column1 = 'value' WHERE id = 1")) {
            System.out.println("Update operation successful.");
        }

        // 测试插入
        if (db.insert("INSERT INTO your_table (column1, column2) VALUES ('value1', 'value2')")) {
            System.out.println("Insert operation successful.");
        }

        db.connectTest();
        db.close();
    }
}
