CREATE TABLE IF NOT EXISTS trucks (
                                      id SERIAL,
                                      code VARCHAR(50) NOT NULL UNIQUE,
                                      model VARCHAR(100) NOT NULL,
                                      hub_id INTEGER,
                                      type VARCHAR(20) NOT NULL,
                                      body VARCHAR(20) NOT NULL,
                                      axle_setup VARCHAR(20) NOT NULL,
                                      load_capacity FLOAT NOT NULL,
                                      weight DOUBLE PRECISION NOT NULL,
                                      length DOUBLE PRECISION NOT NULL,
                                      width DOUBLE PRECISION NOT NULL,
                                      height DOUBLE PRECISION NOT NULL,
                                      average_fuel_consumption DOUBLE PRECISION NOT NULL,
                                      status VARCHAR(20) NOT NULL,
                                      features VARCHAR(100),
                                      CONSTRAINT pk_truck PRIMARY KEY (id),
                                      CONSTRAINT fk_truck_hub FOREIGN KEY (hub_id) REFERENCES hubs(id)
);