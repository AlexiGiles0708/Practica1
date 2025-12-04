ALTER PROCEDURE restarStockProducto
    @producto VARCHAR(100),
    @cantidad INT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE Productos
    SET stock = stock - @cantidad
    WHERE nombre = @producto
      AND stock >= @cantidad;

    IF @@ROWCOUNT = 0
    BEGIN
        RAISERROR('No hay suficiente stock o el producto no existe.', 16, 1);
        RETURN;
    END
END
GO

EXEC restarStockProducto 'Mouse Inalámbrico Logitech M185', 2;
