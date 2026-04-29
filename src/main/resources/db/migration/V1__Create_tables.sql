CREATE OR REPLACE FUNCTION is_not_empty_string(string VARCHAR)
    RETURNS BOOLEAN AS
$$
BEGIN
    RETURN string <> '';
END ;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS app_user
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(150) NOT NULL,
    role       VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT username_check
        CHECK (LENGTH(username) >= 3 AND is_not_empty_string(username))
);

CREATE TABLE IF NOT EXISTS task
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL CHECK ( is_not_empty_string(name) ),
    description TEXT         NOT NULL CHECK ( is_not_empty_string(description) ),
    status      VARCHAR(30)  NOT NULL DEFAULT 'NEW',
    created_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS time_record
(
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT    NOT NULL REFERENCES app_user (id),
    task_id             BIGINT    NOT NULL REFERENCES task (id),
    start_time          TIMESTAMP NOT NULL,
    end_time            TIMESTAMP NOT NULL,
    description_of_work TEXT CHECK ( is_not_empty_string(description_of_work) ),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS deactivated_token
(
    id         UUID PRIMARY KEY,
    keep_until TIMESTAMP NOT NULL CHECK (keep_until > NOW())
);
