/**
 * @author Sergio Trillo Rodriguez
 */

package com.iesvdc.dam.acceso.conexion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Conexion {
    /**
      * Establece una conexión con la base de datos MySQL utilizando los parámetros definidos en el fichero config.properties.
      * El fichero debe contener las claves:
      *     - host: dirección del servidor de base de datos
      *     - port: puerto del servidor
      *     - database: nombre de la base de datos
      *     - user: usuario de conexión
      *     - password: contraseña del usuario
      * Si ocurre algún error al leer el fichero o establecer la conexión, se muestra un mensaje descriptivo por consola y se devuelve {@code null}.
      *
      * @return un objeto {@link java.sql.Connection} si la conexión se establece correctamente; 
      * {@code null} si ocurre algún error durante el proceso.
      * @throws SecurityException si el acceso al fichero de propiedades está restringido por el sistema de seguridad de Java.
      * @see java.util.Properties
      * @see java.sql.Connection
      * @see java.sql.DriverManager
      */
    public static Connection getConnection() {
        Properties props = Config.getProperties("config.properties");
        Connection conexion = null;
        String cadenaConexion = "jdbc:mysql://"
            + props.getProperty("host")  //host
            + ":"+ props.getProperty("port") // puerto
            +"/"+ props.getProperty("database"); //base de datos
        try {            
            conexion = DriverManager.getConnection(cadenaConexion, props);            
        } catch (SQLException sqle) {
            System.err.println("Error al conectar a la base de datos: " + sqle.getLocalizedMessage());            
        }
        return conexion;
    }

    /**
     * Crea la base de datos indicada en config.properties si no existe.
     * Se conecta al servidor MySQL sin seleccionar base (porque aún no existe) y ejecuta "CREATE DATABASE IF NOT EXISTS <nombreBD>".
     * Debe llamarse antes de usar getConnection() para trabajar con esa base.
     */
    public static void crearDatabase() {
        Properties props = Config.getProperties("config.properties");

        String host = props.getProperty("host");
        String port = props.getProperty("port");
        String user = props.getProperty("user");
        String password = props.getProperty("password");
        String database = props.getProperty("database");
        String url = "jdbc:mysql://" + host + ":" + port + "/mysql";

        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st = conn.createStatement()) {
            String sql = "CREATE DATABASE IF NOT EXISTS `" + database + "`";
            st.execute(sql);
            System.out.println("Base de datos '" + database + "' creada/verificada.");
        } catch (SQLException e) {
            System.err.println("Error creando la base de datos: " + e.getMessage());
        }
    }

    /**
     * Desactiva el auto-commit en la conexión para iniciar una transacción.
     * Si la conexión es null o ya está en el modo deseado no hace nada.
     * @param conexion conexión activa
     * @throws SQLException en caso de error al cambiar el modo auto-commit
     */
    public static void beginTransaction(Connection conexion) throws SQLException {
        if (conexion==null)
            throw new SQLException("Conexion es nula");
        if (conexion.getAutoCommit()) 
            conexion.setAutoCommit(false);
    }

    /**
     * Aplica commit y restaura auto-commit a true.
     * @param conexion conexión en la que hacer commit
     * @throws SQLException en caso de error
     */
    public static void commit(Connection conexion) throws SQLException {
        if (conexion == null)
            throw new SQLException("Conexion es nula");
        try {
            System.out.println("Aplicando COMMIT...");
            conexion.commit();
            System.out.println("COMMIT realizado con éxito.");  
        } finally {
            try { 
                System.out.println("Restaurando modo auto-commit.");
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error menor al restaurar el auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Realiza rollback y restaura auto-commit a true.
     * @param conexion conexión en la que hacer rollback
     * @throws SQLException en caso de error
     */
    public static void rollback(Connection conexion) throws SQLException {
        if (conexion == null)
            throw new SQLException("Conexion es nula");
            
        try {
            System.err.println("Detectado error, iniciando ROLLBACK...");
            conexion.rollback();
            System.err.println("ROLLBACK completado.");
        } finally {
            try {
                System.out.println("Restaurando modo auto-commit.");
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error menor al restaurar el auto-commit: " + e.getMessage());
            }
        }
    }
}
