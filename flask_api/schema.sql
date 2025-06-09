CREATE DATABASE cs446db;
USE cs446db;

DROP TABLE IF EXISTS user;
CREATE TABLE user (
	username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (username)
);

-- just some dummy data
INSERT INTO user (username, password)
VALUES
('deto', 'epic');
    

