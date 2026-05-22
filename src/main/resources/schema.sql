CREATE TABLE member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(255) NOT NULL,
    name     VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(10)  NOT NULL CHECK (role = 'USER' OR role = 'MANAGER' OR role = 'ADMIN'),
    PRIMARY KEY (id)
);

CREATE TABLE token
(
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    member_id  BIGINT        NOT NULL,
    token      VARCHAR(1000) NOT NULL,
    expired_at TIMESTAMP     NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE
);

CREATE TABLE token_blacklist
(
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    token      VARCHAR(1000) NOT NULL,
    expired_at TIMESTAMP     NOT NULL
);

CREATE TABLE store
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE manager_store
(
    id         BIGINT NOT NULL AUTO_INCREMENT,
    manager_id BIGINT NOT NULL,
    store_id   BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (manager_id) REFERENCES member (id),
    FOREIGN KEY (store_id) REFERENCES store (id)
);

CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE theme
(
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255)  NOT NULL,
    description VARCHAR(255)  NOT NULL,
    image_url   VARCHAR(2000) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id        BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    date      DATE   NOT NULL,
    time_id   BIGINT NOT NULL,
    theme_id  BIGINT NOT NULL,
    store_id  BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (store_id) REFERENCES store (id)
);
