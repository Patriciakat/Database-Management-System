CREATE TABLE Customer (
    Name VARCHAR NOT NULL,
    Surname VARCHAR NOT NULL,
    Email VARCHAR NOT NULL UNIQUE,
    Address VARCHAR NOT NULL
);

CREATE TABLE Orders (
    Order_nr SERIAL PRIMARY KEY,
    Email VARCHAR NOT NULL REFERENCES Customer (Email),
    Total_price DECIMAL(10, 2) DEFAULT 0,
    Date DATE NOT NULL DEFAULT CURRENT_DATE,
    Status VARCHAR NOT NULL DEFAULT 'Paid' CHECK (Status IN ('Paid', 'Delivered'))
);

CREATE TABLE Phone (
    Phone_ID SERIAL PRIMARY KEY,
    Model VARCHAR NOT NULL,
    Price DECIMAL(10, 2) NOT NULL CHECK (Price > 0),
    Color VARCHAR NOT NULL,
    Storage INTEGER NOT NULL CHECK (Storage >= 32 AND Storage % 32 = 0)
);

CREATE TABLE Order_phone (
    Order_nr INTEGER NOT NULL REFERENCES Orders (Order_nr),
    Phone_ID INTEGER NOT NULL REFERENCES Phone (Phone_ID),
    Quantity INTEGER NOT NULL CHECK (Quantity > 0),
    Status VARCHAR NOT NULL DEFAULT 'Paid' CHECK (Status IN ('Paid', 'Delivered')),
    PRIMARY KEY (Order_nr, Phone_ID)
);

CREATE TABLE Shipment (
    Shipment_ID SERIAL PRIMARY KEY,
    Order_nr INTEGER NOT NULL REFERENCES Orders (Order_nr),
    Tracking_number VARCHAR NOT NULL,
    Delivery_status VARCHAR NOT NULL DEFAULT 'In Transit' CHECK (Delivery_status IN ('In Transit', 'Delivered'))
);

CREATE TABLE Order_Shipment (
    Shipment_ID INTEGER NOT NULL REFERENCES Shipment (Shipment_ID),
    Order_nr INTEGER NOT NULL REFERENCES Orders (Order_nr),
    PRIMARY KEY (Shipment_ID, Order_nr)
);

CREATE UNIQUE INDEX idx_customer_email ON Customer (Email);
CREATE INDEX idx_order_status ON Orders (Status);

