package com.iesvdc.dam.acceso;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Clase encargada de importar personas desde un archivo Excel (.xlsx) hacia una base de datos MySQL. Se ejecuta dentro de una transacción
 * Si ocurre un error, se realiza ROLLBACK automático.
 */
public class PersonasImporter {
    /**
     * Importa los datos de un archivo Excel a la base de datos.
     * El proceso consiste en:
     *  - Desactivar autocommit para iniciar transacción
     *  - Crear/verificar la tabla personas
     *  - Insertar cada fila del Excel
     *  - Realizar commit si todo va bien
     * En caso de fallo, se ejecuta rollback.
     * @param excelFile ruta al archivo Excel (.xlsx) a importar
     * @param conexion objeto Connection conectado a la base de datos
     * @throws Exception si ocurre cualquier error durante la importación
     */
    public void importar(String excelFile, Connection conexion) throws Exception {
        conexion.setAutoCommit(false); // BEGIN TRANSACTION

        try (FileInputStream fis = new FileInputStream(excelFile); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            // 1. Crear tabla
            createTable(conexion);
            // 2. Insertar datos
            insertData(sheet, conexion);

            conexion.commit();
            System.out.println("Importación completada correctamente. COMMIT realizado.");
        } catch (Exception e) {
            conexion.rollback();
            System.err.println("ERROR, realizando ROLLBACK...");
            throw e;
        }
    }

    /**
     * Crea la tabla `personas` si no existe ya en la base de datos.
     * Este método incluye:
     *  - id (clave primaria)
     *  - nombre
     *  - apellidos
     *  - email
     *  - telefono
     *  - genero (ENUM)
     * @param conexion conexión activa con la base de datos
     * @throws SQLException si la sentencia SQL falla
     */
    private void createTable(Connection conexion) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS personas ( id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(50),  apellidos VARCHAR(80), email VARCHAR(100), telefono INT, genero ENUM('MASCULINO','FEMENINO','NEUTRO','OTRO') )";

        try (Statement st = conexion.createStatement()) {
            st.execute(sql);
        }
        System.out.println("Tabla 'personas' verificada/creada.");
    }

    /**
     * Inserta los datos del Excel en la tabla personas utilizando batches para optimizar el rendimiento.
     * Valida que el teléfono sea numérico; si no lo es, se lanza excepción y se aborta toda la importación.
     * @param sheet hoja del Excel que contiene los datos
     * @param conexion conexión activa con la base de datos
     * @throws Exception si la fila tiene datos inválidos o falla la inserción
     */
    private void insertData(Sheet sheet, Connection conexion) throws Exception {
        String sqlCheck = "SELECT COUNT(*) FROM personas WHERE email = ?";
        String sqlInsert = "INSERT INTO personas (nombre, apellidos, email, telefono, genero) VALUES (?,?,?,?,?)";

        try (PreparedStatement psInsert = conexion.prepareStatement(sqlInsert); PreparedStatement psCheck = conexion.prepareStatement(sqlCheck)) {
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                String nombre = getString(row.getCell(0));
                String apellidos = getString(row.getCell(1));
                String email = getString(row.getCell(2));
                String telefonoStr = getString(row.getCell(3));
                String genero = getString(row.getCell(4));

                // Validación teléfono numérico
                if (!telefonoStr.matches("\\d+"))
                    throw new Exception("Fila " + (i + 1) + ": teléfono no numérico -> '" + telefonoStr + "'");

                int telefono = Integer.parseInt(telefonoStr);

                // Comprobar si el email ya existe
                psCheck.setString(1, email);
                try (var rs = psCheck.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0)
                        throw new Exception("Fila " + (i + 1) + ": email duplicado -> '" + email + "'. ROLLBACK total.");   // Si existe, abortar toda la importación
                }

                // Preparar batch
                psInsert.setString(1, nombre);
                psInsert.setString(2, apellidos);
                psInsert.setString(3, email);
                psInsert.setInt(4, telefono);
                psInsert.setString(5, genero);

                psInsert.addBatch();
            }

            // Ejecutar batch solo si no hay duplicados
            psInsert.executeBatch();
        }
    }

    /**
     * Obtiene el valor de una celda del Excel como cadena limpiando espacios.
     * Si la celda es nula, retorna una cadena vacía para evitar NullPointerExceptions.
     * @param cell celda de Excel a convertir
     * @return valor textual de la celda, nunca null
     */
    private String getString(Cell cell) {
        if (cell == null)
            return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}
