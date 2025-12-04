CREATE DATABASE Compras;

use Compras;
CREATE TABLE  Productos (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL
    
);

create table EstadoProducto (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nombre_estado VARCHAR(50) NOT NULL
);

ALTER TABLE Productos
ADD estado_id INT,
FOREIGN KEY (estado_id) REFERENCES EstadoProducto(id);

insert into  EstadoProducto (nombre_estado) values ('Disponible'), ('Agotado'), ('Descontinuado');

alter table Productos
add ruta_imagen VARCHAR(255);

INSERT INTO productos (nombre, precio, stock, estado_id, ruta_imagen) VALUES
('Audífonos Bluetooth Sony WH-CH520', 1299.00, 25, 1, NULL),
('Mouse Inalámbrico Logitech M185', 249.00, 5, 1, NULL),
('Smartwatch Amazfit Bip U Pro', 1499.00, 18, 1, NULL),
('Cargador Rápido Anker 20W USB-C', 399.00, 40, 1, NULL),
('Laptop Lenovo IdeaPad 3 15"', 8999.00, 7, 1, NULL),
('Cámara Web Logitech C920 HD Pro', 1599.00, 4, 1, NULL),
('Teclado Mecánico Redragon K552 Kumara', 899.00, 30, 1, NULL),
('Power Bank Xiaomi Mi 10000mAh', 499.00, 35, 1, NULL),
('Alexa Echo Dot 5ta Generación', 1299.00, 20, 1, NULL),
('SSD Kingston NV2 1TB NVMe', 1099.00, 15, 1, NULL);



