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