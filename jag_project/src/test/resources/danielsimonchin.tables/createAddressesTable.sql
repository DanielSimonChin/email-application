/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS ADDRESSES;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE ADDRESSES (
  ADDRESSID int(11) NOT NULL auto_increment,
  EMAILADDRESS varchar(320) UNIQUE NOT NULL,
  PRIMARY KEY  (ADDRESSID)
);

INSERT INTO ADDRESSES (EMAILADDRESS) values("danieldawsontest1@gmail.com");
INSERT INTO ADDRESSES (EMAILADDRESS) values("danieldawsontest2@gmail.com");
INSERT INTO ADDRESSES (EMAILADDRESS) values("danieldawsontest3@gmail.com");
INSERT INTO ADDRESSES (EMAILADDRESS) values("recievedanieldawson1@gmail.com");

