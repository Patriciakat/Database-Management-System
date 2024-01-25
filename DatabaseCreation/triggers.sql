--Automatically calculate the total price of order
CREATE OR REPLACE FUNCTION calculate_order_total()
RETURNS TRIGGER AS $$
DECLARE
    totalPrice DECIMAL(10, 2);
BEGIN
    SELECT SUM(p.Price * op.Quantity) INTO totalPrice
    FROM phone p
    JOIN Order_phone op ON p.Phone_ID = op.Phone_ID
    WHERE op.Order_nr = NEW.Order_nr;

    UPDATE Orders
    SET total_price = totalPrice
    WHERE Order_nr = NEW.Order_nr;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER calculate_order_total_trigger
AFTER INSERT OR UPDATE OR DELETE ON Order_phone
FOR EACH ROW
EXECUTE FUNCTION calculate_order_total();

-- to prevent the deletion of an order with shipped status
CREATE OR REPLACE FUNCTION prevent_order_deletion_with_shipped_status()
RETURNS TRIGGER AS $$
BEGIN
    -- Check if the order has been shipped (Delivered)
    IF EXISTS (
        SELECT 1
        FROM Shipment
        WHERE Order_nr = OLD.Order_nr AND Delivery_status = 'Delivered'
    ) THEN
        RAISE EXCEPTION 'Cannot delete an order that has already been shipped.';
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_order_deletion_trigger
BEFORE DELETE ON Orders
FOR EACH ROW
EXECUTE FUNCTION prevent_order_deletion_with_shipped_status();

-- Business Rule: Do not allow the deletion of a customer who has placed orders.
CREATE OR REPLACE FUNCTION prevent_customer_deletion_with_placed_orders()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM Orders
        WHERE Email = OLD.Email
    ) THEN
        RAISE EXCEPTION 'Cannot delete a customer who has placed orders.';
    END IF;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_customer_deletion_trigger
BEFORE DELETE ON Customer
FOR EACH ROW
EXECUTE FUNCTION prevent_customer_deletion_with_placed_orders();

-- when the Status column in the Order table is updated, will update rows in the Order_phone table, setting their Status to the new value.
CREATE OR REPLACE FUNCTION update_order_phone_status() 
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Order_phone SET Status = NEW.Status
    WHERE Order_nr = NEW.Order_nr AND Status <> NEW.Status;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER order_status_change
AFTER UPDATE OF Status ON Orders
FOR EACH ROW
EXECUTE FUNCTION update_order_phone_status();

-- updates the order status based on the status of all associated phones in an order
CREATE OR REPLACE FUNCTION update_order_status_from_phones() RETURNS TRIGGER AS $$
DECLARE
    total_count INTEGER;
    matching_status_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_count FROM Order_phone WHERE Order_nr = NEW.Order_nr;
    SELECT COUNT(*) INTO matching_status_count FROM Order_phone WHERE Order_nr = NEW.Order_nr AND Status = NEW.Status;

    IF total_count = matching_status_count THEN
        UPDATE Orders SET Status = NEW.Status 
        WHERE Order_nr = NEW.Order_nr AND Status <> NEW.Status;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER order_phone_status_change
AFTER UPDATE OF Status ON Order_phone
FOR EACH ROW
EXECUTE FUNCTION update_order_status_from_phones();

-- to prevent the manual insertion of Total_Price in Order
CREATE OR REPLACE FUNCTION prevent_manual_price_insert()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.Total_price IS NOT NULL AND NEW.Total_price <> 0 THEN
        RAISE EXCEPTION 'Manual setting of Total_price is not allowed.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_price_insert
BEFORE INSERT ON Orders
FOR EACH ROW
EXECUTE FUNCTION prevent_manual_price_insert();