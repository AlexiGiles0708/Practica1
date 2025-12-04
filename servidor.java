import java.net.*;
import java.io.*;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class servidor {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1435;databaseName=Compras;encrypt=false;trustServerCertificate=true";
    private static final String DB_USER = "sa"; 
    private static final String DB_PASSWORD = "MiContrasena123!"; 

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
                System.out.println(in.readLine());
                
                out.close();
                in.close();
                cl.close();
            }
        } catch(Exception e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }    
    }
    
    private static void enviarProductos(PrintWriter out) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("EXEC ObtenerProductosDisponibles");
            
            out.println("INICIO_PRODUCTOS");
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                int stock = rs.getInt("stock");
                int estadoId = rs.getInt("estado_id");
                String rutaImagen = rs.getString("ruta_imagen");
                
                // Leer la imagen y convertir a Base64
                String imagenBase64 = leerImagenComoBase64(rutaImagen);
                
                
                out.println(id + "|" + nombre + "|" + precio + "|" + stock + "|" + estadoId + "|" + imagenBase64);
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
    
    private static String leerImagenComoBase64(String rutaImagen) {
        try {
            if (rutaImagen == null || rutaImagen.isEmpty()) {
                return "NO_IMAGE";
            }
            
           
            byte[] imageBytes = Files.readAllBytes(Paths.get(rutaImagen));
            
           
            return Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (Exception e) {
            System.out.println("Error leyendo imagen: " + rutaImagen + " - " + e.getMessage());
            return "NO_IMAGE";
        }
    }
}