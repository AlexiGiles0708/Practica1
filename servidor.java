import java.net.*;
import java.io.*;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class servidor {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1434;databaseName=Compras;encrypt=false;trustServerCertificate=true";
    private static final String DB_USER = "sa"; 
    private static final String DB_PASSWORD = "MiContrasena123!'"; 

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
                procesarCompra(in, out);
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
    
    private static void procesarCompra(BufferedReader in, PrintWriter out) {
    Map<Integer, Integer> carrito = new LinkedHashMap<>();
    try {
        String linea;
        while ((linea = in.readLine()) != null) {
            if ("FIN_COMPRA".equals(linea)) break;
            if (linea.trim().isEmpty()) continue;

            String[] partes = linea.split("\\|");
            if (partes.length != 2) continue;

            try {
                int id = Integer.parseInt(partes[0].trim());
                int cantidad = Integer.parseInt(partes[1].trim());
                if (cantidad > 0) {
                    carrito.put(id, carrito.getOrDefault(id, 0) + cantidad);
                }
            } catch (NumberFormatException ex) {
                System.out.println("Línea de compra inválida: " + linea);
            }
        }

        if (carrito.isEmpty()) {
            out.println("ERROR_COMPRA|El carrito está vacío.");
            return;
        }

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        conn.setAutoCommit(false);

        double total = 0.0;
        StringBuilder ticket = new StringBuilder();
        ticket.append("TICKET DE COMPRA\n\n");
        ticket.append(String.format("%-5s %-25s %-10s %-10s %-10s%n",
                "ID", "Producto", "Cant", "P.Unit", "Subtotal"));

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("EXEC restarStockProducto");

            if(rs.next()){
                    for (Map.Entry<Integer, Integer> e : carrito.entrySet()) {
                    int id = e.getKey();
                    int cantidad = e.getValue();
                    double precioUnit = 0;   // deberías recuperarlo de BD
                    String nombre = "Prod " + id; // idem
                    double subtotal = cantidad * precioUnit;
                    total += subtotal;

                    ticket.append(String.format("%-5d %-25s %-10d %-10.2f %-10.2f%n",
                            id, nombre, cantidad, precioUnit, subtotal));
                }
                ticket.append(String.format("%nTOTAL: %.2f%n", total));
                conn.commit();
                out.println("OK_COMPRA");
                out.println("TICKET");
                for (String l : ticket.toString().split("\\n")) {
                    out.println(l);
                }
                out.println("FIN_TICKET");
            }

        } catch (Exception e) {
            conn.rollback();
            System.out.println("Error durante la compra, rollback: " + e.getMessage());
            out.println("ERROR_COMPRA|Error durante la compra: " + e.getMessage());
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }

    } catch (SQLException e) {
        System.out.println("Error con la base de datos al procesar compra: " + e.getMessage());
        out.println("ERROR_COMPRA|Error con la base de datos: " + e.getMessage());
    } catch (IOException e) {
        System.out.println("Error leyendo datos de la compra: " + e.getMessage());
        out.println("ERROR_COMPRA|Error leyendo datos de la compra: " + e.getMessage());
    } catch (ClassNotFoundException e) {
        System.out.println("Driver SQL Server no encontrado: " + e.getMessage());
        out.println("ERROR_COMPRA|Driver SQL Server no encontrado.");
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