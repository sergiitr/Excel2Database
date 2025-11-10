package com.iesvdc.dam.acceso.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * El modelo que almacena el libro o lista de tablas.
 */
public class WorkbookModel {
    private List<TableModel> tables = new ArrayList<TableModel>();

    public WorkbookModel() {
        this.tables = new ArrayList<TableModel>();
    }

    public WorkbookModel(List<TableModel> tables) {
        this.tables = tables;
    }

    public boolean addTable(TableModel table) {
        return tables.add(table);
    }

    public List<TableModel> getTables() {
        return tables;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WorkbookModel{");
        sb.append("tables=").append(tables);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
    
        final WorkbookModel other = (WorkbookModel) obj;
        return Objects.equals(this.tables, other.tables);
    }
}
