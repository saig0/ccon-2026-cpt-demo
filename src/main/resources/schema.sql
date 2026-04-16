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
