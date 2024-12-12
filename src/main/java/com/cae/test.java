package com.cae;



public class test {
    public static void main(String[] args) {
        String sql= "SELECT \n" +
                        "    db1.employees.name AS employee_name,\n" +
                        "    db1.employees.salary AS employee_salary,\n" +
                        "    db2.departments.name AS department_name,\n" +
                        "    db3.projects.project_name AS project_name\n" +
                        "FROM \n" +
                        "    db1.employees\n" +
                        "JOIN \n" +
                        "    db2.departments \n" +
                        "    ON db1.employees.department_id = db2.departments.department_id\n" +
                        "LEFT JOIN \n" +
                        "    db3.projects \n" +
                        "    ON db1.employees.project_id = db3.projects.project_id\n" +
                        "WHERE \n" +
                        "    db1.employees.salary > (\n" +
                        "        SELECT AVG(salary) \n" +
                        "        FROM db1.employees \n" +
                        "        WHERE department_id = db1.employees.department_id\n" +
                        "    )\n" +
                        "ORDER BY \n" +
                        "    db1.employees.name; ";

        //Log_Recorder logger = new Log_Recorder();
        //logger.insertRecord(sql,"src/main/resources/interface-config.yaml"  , "查询",  1);
    }
}