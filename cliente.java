import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

class Producto {
    int id;
    String nombre;
    double precio;
    int stock;
    int estadoId;
    String imagenBase64;
    
    public Producto(int id, String nombre, double precio, int stock, int estadoId, String imagenBase64) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.estadoId = estadoId;
        this.imagenBase64 = imagenBase64;
    }
}

public class cliente {
    private static List<Producto> productosDisponibles = new ArrayList<>();
    private static List<Producto> carrito = new ArrayList<>();
    
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Escribe la direccion del servidor: ");
            String direccion = br.readLine();
            System.out.println("Escribe el puerto: ");
            int puerto = Integer.parseInt(br.readLine());
            Socket s = new Socket(direccion, puerto);
            System.out.println("Conectado al servidor");
            
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            recibirProductos(in);

            System.out.println("\n");

            System.out.println("Opciones:");
            System.out.println("1. Agregar producto al carrito");
            System.out.println("2. Ver carrito");
            System.out.println("3. Realizar compra");
            System.out.println("4. Salir");

            Scanner scanner = new Scanner(System.in);
            int opcion;
            do {
                System.out.print("Selecciona una opcion: ");
                opcion = scanner.nextInt();
                switch (opcion) {
                    case 1:
                        agregarProductosAlCarrito(scanner);
                        break;
                    case 2:
                        
                        System.out.println("Mostrando carrito...");
                        mostrarCarrito();
                        break;
                    case 3:
                        
                        System.out.println("Realizando compra...");
                        break;
                    case 4:
                        System.out.println("Saliendo...");
                        break;
                    default:
                        System.out.println("Opcion no valida. Intenta de nuevo.");
                }
            } while (opcion != 4);
            
            out.close();
            in.close();
            s.close();
            
            System.out.println("Desconectado del servidor");
            limpiarArchivosTemporales();
        } catch(Exception e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }
    
    private static void agregarProductosAlCarrito(Scanner scanner) {
        System.out.println("Ingresa los IDs de los productos a agregar (separados por comas, ej: 1,2,3): ");
        scanner.nextLine(); 
        String entrada = scanner.nextLine();
        
        String[] idsString = entrada.split(",");
        
        for (String idStr : idsString) {
            try {
                int id = Integer.parseInt(idStr.trim());
                Producto producto = buscarProductoPorId(id);
                
                if (producto != null) {
                    carrito.add(producto);
                    System.out.println("Producto agregado: " + producto.nombre);
                } else {
                    System.out.println("Producto con ID " + id + " no encontrado.");
                }
            } catch (NumberFormatException e) {
                System.out.println("ID inválido: " + idStr);
            }
        }
        
        // Guardar carrito en archivo
        guardarCarritoEnArchivo();
        System.out.println("Carrito actualizado y guardado en 'carrito.txt'");
    }
    
    private static Producto buscarProductoPorId(int id) {
        for (Producto producto : productosDisponibles) {
            if (producto.id == id) {
                return producto;
            }
        }
        return null;
    }
    
    private static void mostrarCarrito() {
        if (carrito.isEmpty()) {
            System.out.println("El carrito está vacío.");
            return;
        }
        
        System.out.println("\n=== CARRITO DE COMPRAS ===");
        System.out.printf("%-5s %-40s %-15s%n", "ID", "Nombre", "Precio");
        System.out.println("---------------------------------------------------------");
        
        double total = 0;
        for (Producto producto : carrito) {
            System.out.printf("%-5d %-40s $%-14.2f%n", 
                producto.id, producto.nombre, producto.precio);
            total += producto.precio;
        }
        
        System.out.println("---------------------------------------------------------");
        System.out.printf("TOTAL: $%.2f%n", total);
    }
    
    private static void guardarCarritoEnArchivo() {
        try {
            StringBuilder contenido = new StringBuilder();
            contenido.append("=== CARRITO DE COMPRAS ===\n");
            contenido.append("Fecha: ").append(new java.util.Date()).append("\n\n");
            
            double total = 0;
            for (int i = 0; i < carrito.size(); i++) {
                Producto producto = carrito.get(i);
                
                contenido.append("PRODUCTO ").append(i + 1).append(":\n");
                contenido.append("ID: ").append(producto.id).append("\n");
                contenido.append("Nombre: ").append(producto.nombre).append("\n");
                contenido.append("Precio: $").append(producto.precio).append("\n");
                contenido.append("Stock disponible: ").append(producto.stock).append("\n");
                
                // Guardar imagen como archivo separado
                if (!"NO_IMAGE".equals(producto.imagenBase64)) {
                    String nombreImagen = "producto_" + producto.id + "_carrito.jpg";
                    guardarImagenDesdeBase64(producto.imagenBase64, nombreImagen);
                    contenido.append("Imagen guardada como: ").append(nombreImagen).append("\n");
                } else {
                    contenido.append("Sin imagen disponible\n");
                }
                
                contenido.append("----------------------------------------\n\n");
                total += producto.precio;
            }
            
            contenido.append("TOTAL DEL CARRITO: $").append(String.format("%.2f", total)).append("\n");
            contenido.append("Número de productos: ").append(carrito.size()).append("\n");
            
            // Escribir archivo de texto
            Files.write(Paths.get("carrito.txt"), contenido.toString().getBytes());
            
        } catch (Exception e) {
            System.out.println("Error al guardar el carrito: " + e.getMessage());
        }
    }
    
    private static void guardarImagenDesdeBase64(String imagenBase64, String nombreArchivo) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(imagenBase64);
            Files.write(Paths.get(nombreArchivo), imageBytes);
        } catch (Exception e) {
            System.out.println("Error al guardar imagen " + nombreArchivo + ": " + e.getMessage());
        }
    }
    
    private static void recibirProductos(BufferedReader in) {
        try {
            String line;
            line = in.readLine();
            if ("INICIO_PRODUCTOS".equals(line)) {
                System.out.println("PRODUCTOS\n");
                System.out.printf("%-5s %-40s %-15s %-10s%n", "ID", "Nombre", "Precio", "Stock");
                System.out.println("---------------------------------------------------------");

                while (!(line = in.readLine()).equals("FIN_PRODUCTOS")) {
                    String[] partes = line.split("\\|");
                    int id = Integer.parseInt(partes[0]);
                    String nombre = partes[1];
                    double precio = Double.parseDouble(partes[2]);
                    int stock = Integer.parseInt(partes[3]);
                    int estadoId = Integer.parseInt(partes[4]);
                    String imagenBase64 = partes.length > 5 ? partes[5] : "NO_IMAGE";
                    
                    // Guardar producto en lista
                    productosDisponibles.add(new Producto(id, nombre, precio, stock, estadoId, imagenBase64));
                    
                    System.out.println("---------------------------------------------------------");
                    System.out.printf("%-5d %-40s %-15.2f %-10d%n",
                        id, nombre, precio, stock);
                }
            } else if ("ERROR_DB".equals(line)) {
                System.out.println("Error al obtener productos de la base de datos en el servidor.");
            }
        } catch (Exception e) {
            System.out.println("Error al recibir productos: " + e.getMessage());
        }
    }

    private static void limpiarArchivosTemporales() {
        try {
            
            Files.deleteIfExists(Paths.get("carrito.txt"));
            
            // Eliminar imágenes del carrito
            for (Producto producto : carrito) {
                String nombreImagen = "producto_" + producto.id + "_carrito.jpg";
                Files.deleteIfExists(Paths.get(nombreImagen));
            }
            
           
            carrito.clear();
            productosDisponibles.clear();
            
            System.out.println("✓ Carrito y archivos temporales eliminados");
            
        } catch (Exception e) {
            System.out.println("Error al limpiar archivos: " + e.getMessage());
        }
    }
}