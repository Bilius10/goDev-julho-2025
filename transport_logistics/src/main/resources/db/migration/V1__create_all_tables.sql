-- Table: products
CREATE TABLE IF NOT EXISTS products (
    id SERIAL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    weight FLOAT NOT NULL,
    active boolean,
    UNIQUE(name),
    CONSTRAINT pk_product PRIMARY KEY (id)
);

-- Table: shipments
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

-- Table: trucks
CREATE TABLE IF NOT EXISTS trucks (
    id SERIAL,
    code VARCHAR(50) NOT NULL UNIQUE,
    model VARCHAR(100) NOT NULL,
    hub_id INTEGER,
    type VARCHAR(20) NOT NULL,
    body_type VARCHAR(20) NOT NULL,
    axle_setup VARCHAR(20) NOT NULL,
    load_capacity FLOAT NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    length DOUBLE PRECISION NOT NULL,
    width DOUBLE PRECISION NOT NULL,
    height DOUBLE PRECISION NOT NULL,
    average_fuel_consumption DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    features VARCHAR(100),
    CONSTRAINT pk_truck PRIMARY KEY (id)
    CONSTRAINT fk_truck_hub FOREIGN KEY (hub_id) REFERENCES hub(id)
);

-- Table: hubs
CREATE TABLE IF NOT EXISTS hubs (
    id SERIAL,
    name VARCHAR(100) NOT NULL UNIQUE,
    cnpj VARCHAR(20) NOT NULL UNIQUE,
    street VARCHAR(100) NOT NULL,
    number VARCHAR(6) NOT NULL,
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    cep CHAR(9) NOT NULL,
    CONSTRAINT pk_hub PRIMARY KEY (id)
);

-- Table: employees
CREATE TABLE IF NOT EXISTS employees (
    id SERIAL,
    name VARCHAR(100) NOT NULL,
    cnh CHAR(11) NOT NULL,
    cpf CHAR(11),
    email VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL,
    hub_id INTEGER,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT pk_employee PRIMARY KEY (id),
    CONSTRAINT fk_employee_hub FOREIGN KEY (hub_id) REFERENCES hubs (id)
);

-- Table: transports
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
