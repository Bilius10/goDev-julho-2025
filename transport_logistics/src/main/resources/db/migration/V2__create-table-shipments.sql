CREATE TABLE IF NOT EXISTS shipments (
                                         id SERIAL,
                                         product_id INTEGER,
                                         weight FLOAT NOT NULL,
                                         quantity INTEGER NOT NULL,
                                         notes VARCHAR(100),
                                         is_hazardous BOOLEAN NOT NULL,
                                         CONSTRAINT pk_shipment PRIMARY KEY (id),
                                         CONSTRAINT fk_shipment_product FOREIGN KEY (product_id) REFERENCES products (id)
);