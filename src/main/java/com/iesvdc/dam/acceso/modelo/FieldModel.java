package com.iesvdc.dam.acceso.modelo;

/**
 * El modelo que almacena información de un campo y sus propiedades.
 */

public class FieldModel {
    private final String name;
    private final FieldType type;

    public FieldModel() {
        this.name = "";
        this.type = FieldType.UNKNOWN;
    }

    public FieldModel(String name) {
        this.name = name;
        this.type = FieldType.UNKNOWN;
    }

    public FieldModel(String name, FieldType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return "FieldModel [name=" + name + ", type=" + type + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FieldModel other = (FieldModel) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
}
