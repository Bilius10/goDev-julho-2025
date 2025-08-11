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