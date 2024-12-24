package com.task11.models;

import java.util.List;

public class TablesResponse {

    private List<TableInfo> tables;

    public TablesResponse(List<TableInfo> tables) {
        this.tables = tables;
    }

    public List<TableInfo> getTables() {
        return tables;
    }

    public void setTables(List<TableInfo> tables) {
        this.tables = tables;
    }
}
