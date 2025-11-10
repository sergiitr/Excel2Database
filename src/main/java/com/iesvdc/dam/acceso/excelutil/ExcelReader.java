package com.iesvdc.dam.acceso.excelutil;

import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.iesvdc.dam.acceso.modelo.FieldModel;
import com.iesvdc.dam.acceso.modelo.FieldType;
import com.iesvdc.dam.acceso.modelo.TableModel;
import com.iesvdc.dam.acceso.modelo.WorkbookModel;

public class ExcelReader {
    private Workbook wb;
    private WorkbookModel wbm;
    private final double EPSILON = 1e-10;
    
    public ExcelReader() { }

    public Workbook getWb() {
        return wb;
    }

    public void setWb(Workbook wb) {
        this.wb = wb;
    }

    public WorkbookModel getWbm() {
        return wbm;
    }

    public void setWbm(WorkbookModel wbm) {
        this.wbm = wbm;
    }

    /**
     * Determina el tipo de dato contenido en una celda de Excel y lo asigna a un {@link FieldType} personalizado.
     * Analiza tanto tipos básicos como valores numéricos y fechas.
     * El criterio de detección es:
     *   - STRING → devuelve FieldType.STRING
     *   - BOOLEAN → devuelve FieldType.BOOLEAN
     *   - NUMERIC:
     *       - Si la celda está formateada como fecha → FieldType.DATE
     *       - Si el número es entero (sin decimales) → FieldType.INTEGER
     *       - En caso contrario → FieldType.DECIMAL
     *   - Cualquier otro caso → FieldType.UNKNOWN
     *
     * @param cell celda de Excel a analizar
     * @return el tipo de dato deducido como {@link FieldType}, o UNKNOWN si no se reconoce
     */
    public FieldType getTipoDato(Cell cell) {
        if (cell == null)
            return FieldType.UNKNOWN;
        
        switch (cell.getCellType()) {
            case STRING:
                return FieldType.STRING;

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                    return FieldType.DATE;
                else {
                    double valor = cell.getNumericCellValue();
                    if (Math.abs(valor - Math.floor(valor)) < EPSILON)
                        return FieldType.INTEGER;
                    else
                        return FieldType.DECIMAL;
                }

            case BOOLEAN:
                return FieldType.BOOLEAN;

            default:
                return FieldType.UNKNOWN;
        }
    }

    /**
     * Carga un archivo Excel (.xlsx) desde disco y analiza su estructura para construir un {@link WorkbookModel} que representa las tablas y sus campos.
     * El proceso ejecutado es:
     *   - Abrir el fichero Excel mediante Apache POI
     *   - Recorrer cada hoja del libro como si fuera una tabla
     *   - Tomar la primera fila como cabeceras de columnas
     *   - Tomar la segunda fila como muestra para deducir tipos de datos
     *   - Crear {@link FieldModel} por columna con nombre y tipo
     *   - Añadir la tabla construida al {@link WorkbookModel}
     * Si ocurre un error de lectura, este se captura y se muestra un mensaje por consola sin detener la aplicación.
     *
     * @param filename ruta del archivo Excel a cargar
     */
    public void loadWorkbook(String filename) {
        try (FileInputStream fis = new FileInputStream(filename)) {
            wb = new XSSFWorkbook(fis);
            wbm = new WorkbookModel();
            
            int numHojas = wb.getNumberOfSheets();
            
            for (int i=0; i<numHojas; i++) {
                Sheet hojaActual = wb.getSheetAt(i);
                // El nombre de la tabla es el nombre de la hoja
                TableModel tabla = new TableModel(hojaActual.getSheetName());
                
                // De la primera fila obtengo las cabeceras
                Row pFila = hojaActual.getRow(0);
                Row sFila = hojaActual.getRow(1);
                //Obtengo el numero de columnas
                int nCols = pFila.getLastCellNum();
                for (int j=0; j<nCols;j++) {
                    FieldModel campo = new FieldModel (pFila.getCell(j).getStringCellValue(), getTipoDato(sFila.getCell(j)));
                    tabla.addField(campo);
                }                
                wbm.addTable(tabla);
            }
            System.out.println("---TABLAS---");
            System.out.println(wbm.toString());
            wb.close();
        } catch (Exception e) {
            System.out.println("Imposible cargar el archivo Excel");
        }
    }
}
