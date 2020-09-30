/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */

-- This script needs to run only once

DROP DATABASE IF EXISTS EMAILCLIENT;
CREATE DATABASE EMAILCLIENT;

USE EMAILCLIENT;

DROP USER IF EXISTS daniel@localhost;
CREATE USER daniel@'localhost' IDENTIFIED BY 'danielUser';
GRANT ALL ON EMAILCLIENT.* TO daniel@'localhost';

-- This creates a user with access from any IP number except localhost
-- Use only if your MyQL database is on a different host from localhost
-- DROP USER IF EXISTS fish; -- % user
-- CREATE USER fish IDENTIFIED BY 'kfstandard';
-- GRANT ALL ON AQUARIUM TO fish@'%';

FLUSH PRIVILEGES;

