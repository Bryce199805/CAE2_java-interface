package com.cae;

import java.sql.ResultSet;

public class ResultSetWrapper {
    public ResultSet rs;

    public ResultSetWrapper() {
        this.rs = null;
    }

    public void setRs(ResultSet rs) {
        this.rs = rs;
    }

    public ResultSet getRs() {
        return rs;
    }
}