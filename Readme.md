# Proyecto excel2database

Programa ejemplo en Java que vuelca un archivo Excel a una base de datos MySQL.

Este programa genérico en java (proyecto Maven) es un ejercicio simple que vuelca un libro Excel (xlsx) a una base de datos (MySQL) y viceversa. El programa lee la configuración de la base de datos de un fichero "properties" de Java y luego, con apache POI, leo las hojas, el nombre de cada hoja será el nombre de las tablas, la primera fila de cada hoja será el nombre de los atributos de cada tabla (hoja) y para saber el tipo de dato, tendré que preguntar a la segunda fila qué tipo de dato tiene.

Procesamos el fichero Excel y creamos una estructura de datos con la información siguiente: La estructura principal es el libro, que contiene una lista de tablas y cada tabla contiene tuplas nombre del campo y tipo de dato.


Uso del programa:

```bash
excel2database -f personas.xlsx -db agenda
```

Para ello en nuestro sistema operativo debemos tener instalado maven con 
``` bash
sudo apt update
sudo apt install maven
```
Una vez instalado, nos vamos al directorio del proyecto y ejecutamos 
``` bash 
mvn package
```
Una vez hecho eso, creamos el Script de Ejecución llamado 'excel2database' con el siguiente contenido
```bash
#!/bin/bash
java -jar target/excel2database-1.0-SNAPSHOT.jar "$@"
```
Le asignamos permisos al archivo con
``` bash
chmod +x excel2database
```
Y por ultimo para la ejución hacemos 
``` bash
./excel2database -f ./datos/test.xlsx -db db-excel
```


## Instalando Java para que funcione



## Cómo crear este proyecto maven desde cero

Instalamos las dependencias Maven:

* apache poi
* apache poi ooxml
* mysql 

## El archivo de propiedades

Creamos el archivo **`config.properties`**:

```bash
user=root
password=s83n38DGB8d72
useUnicode=yes
useJDBCCompliantTimezoneShift=true
port=33307
database=agenda
host=localhost
driver=MySQL
outputFile=datos/salida.xlsx
inputFile=datos/entrada.xlsx
useSSL=false
serverTimezone=Europe/Madrid
allowPublicKeyRetrieval=true
```

En producción **jamás** debemos de usar estos parámetros:

* `useSSL=false`: No encripta la conexión.
* `allowPublicKeyRetrieval=true`: No comprueba el certificado (como el candado rojo del navegador)

## Detectando qué tipo de dato hay con Apache POI

Con **Apache POI** puedes inspeccionar el **tipo de dato almacenado en una celda de Excel (.xlsx)** y actuar según corresponda. Cuando trabajas con una celda (`Cell`), POI te permite preguntar su tipo con:

```java
cell.getCellType()
```

Esto devuelve un valor del enum `CellType`, que puede ser:

| Tipo (`CellType`) | Significado                                                             |
| ----------------- | ----------------------------------------------------------------------- |
| `NUMERIC`         | Número (entero o decimal, o incluso fecha/hora si el formato lo indica) |
| `STRING`          | Texto                                                                   |
| `BOOLEAN`         | Verdadero/Falso                                                         |
| `FORMULA`         | Celda con una fórmula                                                   |
| `BLANK`           | Celda vacía                                                             |
| `ERROR`           | Celda con error                                                         |


Excel almacena las **fechas y horas como números** (un número de días desde el 1/1/1900).
Para distinguirlas, Apache POI ofrece un método:

```java
DateUtil.isCellDateFormatted(cell)
```

Si devuelve `true`, el contenido es una **fecha o una hora**, y puedes obtenerla así:

```java
Date fecha = cell.getDateCellValue();
```

Si no, puedes tratarlo como número:

```java
double valor = cell.getNumericCellValue();
```

En Excel, todos los números se almacenan como double internamente. No existe distinción “formal” entre enteros y decimales dentro del archivo. Por eso, Apache POI siempre te devuelve NUMERIC y cell.getNumericCellValue() da un double. Como Excel no distingue formalmente entre ellos: ambos son `NUMERIC`, podemos comprobarlo fácilmente así:

```java
double valor = cell.getNumericCellValue();
if (valor == Math.floor(valor)) {
    System.out.println("Entero: " + (int) valor);
} else {
    System.out.println("Decimal: " + valor);
}
```

Un ejemplo de cómo hacer la detección sería:

```java

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;

public class ExcelUtils {

    private static final double EPSILON = 1e-10;

    /**
     * Devuelve un String indicando el tipo de dato de la celda.
     * Puede ser: Entero, Decimal, Texto, Booleano, Fecha, Vacía, Fórmula, Error
     */
    public static String getTipoDato(Cell cell) {
        if (cell == null) {
            return "Vacía";
        }

        switch (cell.getCellType()) {
            case STRING:
                return "Texto";

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return "Fecha";
                } else {
                    double valor = cell.getNumericCellValue();
                    if (Math.abs(valor - Math.floor(valor)) < EPSILON) {
                        return "Entero";
                    } else {
                        return "Decimal";
                    }
                }

            case BOOLEAN:
                return "Booleano";

            case FORMULA:
                // Puedes decidir si quieres evaluar la fórmula o solo indicar que es fórmula
                return "Fórmula";

            case BLANK:
                return "Vacía";

            case ERROR:
                return "Error";

            default:
                return "Desconocido";
        }
    }
}

```

## Apéndice: Repaso de SQL

Para crear una base de datos en MySQL hacemos:

```sql
create database `agenda`;
```

Para borrar una base de datos en MySQL hacemos:

```sql 
drop database `agenda`;
```


```sql
CREATE DATABASE `agenda` COLLATE 'utf16_spanish_ci';
```

Crear una tabla de personas:

```sql
CREATE TABLE `personas` (
  `nombre` varchar(100) NOT NULL,
  `apellidos` varchar(300) NOT NULL,
  `email` varchar(100),
  `telefono` varchar(12),
  `genero` enum('FEMENINO','MASCULINO','NEUTRO','OTRO') NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY
) ENGINE='InnoDB';



INSERT INTO `personas` (
    `nombre`, `apellidos`, 
    `email`, `telefono`, 
    `genero`)
VALUES (
    'Juan', 'Sin Miedo', 
    'juan@sinmiedo.com', '+34555123456', 
    'OTRO');

SELECT * FROM `personas` LIMIT 50;

SELECT * FROM `personas` LIMIT 5 OFFSET 10;
```

# Justificación Tecnológica: JDBC y Conector MySQL

## Elección del Conector MySQL

Se utiliza el **conector oficial MySQL para JDBC** porque:
- Es mantenido directamente por Oracle/MySQL
- Ofrece compatibilidad total con características de MySQL 8.0
- Proporciona rendimiento optimizado específicamente para MySQL
- Incluye soporte para conexiones seguras y manejo de timezone

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

## Justificación del Uso de JDBC
JDBC fue seleccionado porque es el estándar de Java para conectividad con bases de datos y ofrece:

### Ventajas clave:
1. Nativo de Java: Parte del JDK, sin dependencias externas

2. Control total: Acceso directo a SQL y operaciones de base de datos

3. Eficiencia: Ideal para operaciones ETL masivas como importación/exportación

### Ejemplo de código

``` xml
// Conexión directa usando JDBC
try (Connection conexion = Conexion.getConnection()) {
    if (conexion != null) {
        System.out.println("Conectado correctamente.");
        // Operaciones de importación/exportación aquí
    }
}
```

### Alternativas consideradas y rechazadas:
**JPA/Hibernate:** Demasiado complejo para operaciones simples ETL

**Spring JDBC:** Introduce dependencia innecesaria del framework

**Librerías especializadas:** Menor flexibilidad que JDBC puro

### Conclusión

JDBC con el conector MySQL oficial proporciona el balance perfecto entre simplicidad, control y rendimiento para este proyecto de transferencia de datos Excel-MySQL, evitando la sobre-ingeniería de frameworks más complejos.
