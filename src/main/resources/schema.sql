-- =============================================================================
-- Camunda Robotics – Product Catalog: Schema
-- =============================================================================

CREATE TABLE IF NOT EXISTS upgrades (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255)    NOT NULL,
    description VARCHAR(1000),
    price       DECIMAL(10, 2)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS robots (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    model_id      VARCHAR(255)    NOT NULL,
    model_version VARCHAR(255)    NOT NULL,
    name          VARCHAR(255)    NOT NULL,
    description   VARCHAR(1000),
    intent        VARCHAR(255)    NOT NULL,
    price         DECIMAL(10, 2)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS robot_compatible_upgrades (
    robot_id   BIGINT NOT NULL,
    upgrade_id BIGINT NOT NULL,
    PRIMARY KEY (robot_id, upgrade_id),
    CONSTRAINT fk_rcu_robot   FOREIGN KEY (robot_id)   REFERENCES robots(id),
    CONSTRAINT fk_rcu_upgrade FOREIGN KEY (upgrade_id) REFERENCES upgrades(id)
);

-- =============================================================================
-- Camunda Robotics – Customer Database: Schema
-- =============================================================================

CREATE TABLE IF NOT EXISTS customers (
    id                 BIGINT          NOT NULL AUTO_INCREMENT,
    name               VARCHAR(255)    NOT NULL,
    email              VARCHAR(255)    NOT NULL,
    address_street     VARCHAR(255)    NOT NULL,
    address_city       VARCHAR(255)    NOT NULL,
    address_country    VARCHAR(255)    NOT NULL,
    payment_method     VARCHAR(50)     NOT NULL,
    payment_reference  VARCHAR(255)    NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS orders (
    id                         BIGINT          NOT NULL AUTO_INCREMENT,
    customer_id                BIGINT          NOT NULL,
    order_date                 DATE            NOT NULL,
    shipment_address_street    VARCHAR(255)    NOT NULL,
    shipment_address_city      VARCHAR(255)    NOT NULL,
    shipment_address_country   VARCHAR(255)    NOT NULL,
    shipment_date              DATE            NOT NULL,
    payment_date               DATE            NOT NULL,
    payment_amount             DECIMAL(10, 2)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id          BIGINT  NOT NULL AUTO_INCREMENT,
    order_id    BIGINT  NOT NULL,
    robot_id    BIGINT,
    upgrade_id  BIGINT,
    quantity    INT     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id),
    CONSTRAINT fk_order_items_robot   FOREIGN KEY (robot_id)   REFERENCES robots(id),
    CONSTRAINT fk_order_items_upgrade FOREIGN KEY (upgrade_id) REFERENCES upgrades(id)
);
