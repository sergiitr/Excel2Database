package com.iesvdc.dam.acceso.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * El modelo que almacena información de una tabla y su lista de campos.
 */
public class TableModel {
    private final String name;
    private final List<FieldModel> fields = new ArrayList<>();

    public TableModel() {
        this.name = "";
    }

    public TableModel(String name) {
        this.name = name;
    } 
    public String getName() {
        return name;
    }
    public List<FieldModel> getFields() {
        return fields;
    }

    public boolean addField(FieldModel fm) {
        return fields.add(fm);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TableModel{");
        sb.append("name=").append(name);
        sb.append(", fields=").append(fields);
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
    
        final TableModel other = (TableModel) obj;
        if (!Objects.equals(this.name, other.name))
            return false;
        
        return Objects.equals(this.fields, other.fields);
    }
}
