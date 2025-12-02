import java.net.*;
import java.io.*;
import java.sql.*;

public class servidor {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1435;databaseName=Compras;encrypt=false;trustServerCertificate=true";
    private static final String DB_USER = "sa"; // Cambia por tu usuario
    private static final String DB_PASSWORD = "MiContrasena123!"; // Cambia por tu contraseña

    public static void main(String[] args) {
        try {
            ServerSocket s = new ServerSocket(1234);
            System.out.println("Server esperando al cliente: ");
            for (;;){
                Socket cl = s.accept();
                System.out.println("Cliente conectado: " + cl.getInetAddress().getHostAddress());
                
                PrintWriter out = new PrintWriter(cl.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                
         
                enviarProductos(out);
                
                System.out.println("Productos enviados al cliente");
                
                
                out.close();
                in.close();
                cl.close();
                
                System.out.println("Cliente desconectado\n");
            }
        } catch(Exception e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }    
    }
    
    private static void enviarProductos(PrintWriter out) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            CallableStatement stmt = conn.prepareCall("{call ObtenerProductosDisponibles}");
            ResultSet rs = stmt.executeQuery();
            
            out.println("INICIO_PRODUCTOS");
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                int stock = rs.getInt("stock");
                
              
                out.println(id + "|" + nombre + "|" + precio + "|" + stock);
            }
            
            out.println("FIN_PRODUCTOS");
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("Error conectando a la base de datos: " + e.getMessage());
            out.println("ERROR_DB");
        }
    }
}