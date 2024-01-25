CREATE MATERIALIZED VIEW PhoneSalesSummary AS
SELECT 
    p.Model,
    SUM(op.Quantity) AS Total_Quantity_Sold,
    SUM(p.Price * op.Quantity) AS Total_Sales
FROM 
    Phone p
JOIN 
    Order_phone op ON p.Phone_ID =op.Phone_ID
GROUP BY 
    p.Model;

CREATE VIEW OrdersDetails AS
SELECT 
    ord.Order_nr,
    c.Name,
    c.Surname,
    ord.Total_price,
    ord.Date,
    ord.Status,
    s.Tracking_number,
    s.Delivery_status
FROM 
    Orders ord
JOIN 
    Customer c ON ord.Email = c.Email
LEFT JOIN 
    Order_Shipment os ON ord.Order_nr = os.Order_nr
LEFT JOIN 
    Shipment s ON os.Shipment_ID = s.Shipment_ID;

REFRESH MATERIALIZED VIEW PhoneSalesSummary;


