CREATE TABLE IF NOT EXISTS transports (
                                          id SERIAL,
                                          truck_id INTEGER,
                                          shipment_id INTEGER,
                                          driver_id INTEGER,
                                          fuel_consumption FLOAT NOT NULL,
                                          distance FLOAT,
                                          exit_day DATE NOT NULL,
                                          expected_arrival_day DATE NOT NULL,
                                          status VARCHAR(50),
                                          origin_hub_id INTEGER,
                                          destination_hub_id INTEGER,
                                          CONSTRAINT pk_transport PRIMARY KEY (id),
                                          CONSTRAINT fk_transport_truck FOREIGN KEY (truck_id) REFERENCES trucks (id),
                                          CONSTRAINT fk_transport_shipment FOREIGN KEY (shipment_id) REFERENCES shipments (id),
                                          CONSTRAINT fk_transport_driver FOREIGN KEY (driver_id) REFERENCES employees (id),
                                          CONSTRAINT fk_transport_origin_hub FOREIGN KEY (origin_hub_id) REFERENCES hubs (id),
                                          CONSTRAINT fk_transport_destination_hub FOREIGN KEY (destination_hub_id) REFERENCES hubs (id)
);