-- Obtener la lista de productos diponibles o agotados
/*
ejemplo de ejecución
EXEC ObtenerProductosDisponibles;
*/

ALTER PROCEDURE ObtenerProductosDisponibles
AS
BEGIN
    SELECT id,nombre, precio, stock 
    FROM productos WHERE estado_id IN (1,2);
END
GO

