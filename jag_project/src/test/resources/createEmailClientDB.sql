DROP DATABASE IF EXISTS EMAILCLIENT;
CREATE DATABASE EMAILCLIENT;

USE EMAILCLIENT;

DROP USER IF EXISTS daniel@localhost;
CREATE USER daniel@'localhost' IDENTIFIED BY 'danielpw';
GRANT ALL ON EMAILCLIENT.* TO daniel@'localhost';

FLUSH PRIVILEGES;
