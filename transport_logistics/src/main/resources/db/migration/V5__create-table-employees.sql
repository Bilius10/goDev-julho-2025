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