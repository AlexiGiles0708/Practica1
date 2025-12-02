CREATE DATABASE Compras;

USE Compras;

CREATE TABLE  Productos (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table EstadoProducto (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nombre_estado VARCHAR(50) NOT NULL,
    id_producto INT,
    FOREIGN KEY (id_producto) REFERENCES Productos(id)
);

ALTER TABLE Productos
drop column descripcion