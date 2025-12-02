import java.net.*;
import java.io.*;
import java.util.Scanner;

public class cliente {
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Escribe la direccion del servidor: ");
            String direccion = br.readLine();
            System.out.println("Escribe el puerto: ");
            int puerto = Integer.parseInt(br.readLine());
            Socket s =  new Socket(direccion, puerto);
            System.out.println("Conectado al servidor");
            
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            recibirProductos(in);

            System.out.println("\n");

            System.out.println ("Opciones:");
            System.out.println ("2. Agregar producto al carrito");
            System.out.println ("3. Ver carrito");
            System.out.println ("4. Realizar compra");
            System.out.println ("5. Salir");

            Scanner scanner = new Scanner(System.in);
            int opcion;
            do {
                System.out.print("Selecciona una opcion: ");
                opcion = scanner.nextInt();
                switch (opcion) {
                    case 2:
                        // Lógica para agregar producto al carrito
                        System.out.print("Ingresa el ID del producto a agregar: ");
                        break;
                    case 3:
                        // Lógica para ver carrito
                        System.out.print("Mostrando carrito...");
                        break;
                    case 4:
                        // Lógica para realizar compra
                        System.out.print("Realizando compra...");
                        break;
                    case 5:
                        System.out.println("Saliendo...");
                        break;
                    default:
                        System.out.println("Opcion no valida. Intenta de nuevo.");
                }
            } while (opcion != 5); 
            
            
            out.close();
            in.close();
            s.close();
            
            System.out.println("Desconectado del servidor");
        } catch(Exception e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }
    
    private static void recibirProductos(BufferedReader in) {
        try {
            String line;
            line = in.readLine();
            if ("INICIO_PRODUCTOS".equals(line)) {
                System.out.println("PROCUTOS "+'\n');
                System.out.printf("%-5s %-40s %-15s %-10s%n", "ID", "Nombre", "Precio", "Stock");
                System.out.println("---------------------------------------------------------");

                while (!(line = in.readLine()).equals("FIN_PRODUCTOS")) {
                    
                    String[] partes = line.split("\\|");
                    int id = Integer.parseInt(partes[0]);
                    String nombre = partes[1];
                    double precio = Double.parseDouble(partes[2]);
                    int stock = Integer.parseInt(partes[3]);

                    
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
}