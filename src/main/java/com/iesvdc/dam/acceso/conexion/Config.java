package com.iesvdc.dam.acceso.conexion;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {
    /**
     * Método para cargar las propiedades de un fichero.
     * Ejemplo de archivo de propiedades:
     *  - user=root
     *  - password=s83n38DGB8d72
     *  - useUnicode=yes
     *  - useJDBCCompliantTimezoneShift=true
     *  - port=33307
     *  - database=agenda
     *  - host=localhost
     *  - driver=MySQL
     *  - outputFile=datos/salida.xlsx
     *  - inputFile=datos/entrada.xlsx
     *  - useSSL=false
     *  - serverTimezone=Europe/Madrid
     *  - allowPublicKeyRetrieval=true
     * @param nombreArchivo el nombre del archivo que contiene esa información.
     * @return Un objeto del tipo {@link java.util.Properties}
     */
    static public Properties getProperties(String nombreArchivo) {
        Properties props = new Properties();
        try (FileInputStream is = new FileInputStream(nombreArchivo)) {
            props.load(is);            
        } catch (FileNotFoundException fnfe) {
            System.out.println( "No encuentro el fichero de propiedades: " + fnfe.getLocalizedMessage() );
        } catch (IOException ioe){
            System.out.println( "Error al leer el fichero de propiedades: " + ioe.getLocalizedMessage() );
        }
        return props;
    }
}
