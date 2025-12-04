/*
Modificar el estado a agotado (2) de todos los productos cuyo stock sea 0
EXEC ModificarEstadoProductosAgotados;
*/
CREATE PROCEDURE ModificarEstadoProductosAgotados
AS
BEGIN
    UPDATE Productos
    SET estado_id = 2
    WHERE stock = 0;
END
GO