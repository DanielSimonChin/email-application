/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS Addresses;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Addresses (
  AddressID int(11) NOT NULL auto_increment,
  EmailAddress varchar(50) NOT NULL,
  PRIMARY KEY  (AddressID)
);

INSERT INTO Addresses (EmailAddress) values("danieldawsontest1@gmail.com");
INSERT INTO Addresses (EmailAddress) values("danieldawsontest2@gmail.com");
INSERT INTO Addresses (EmailAddress) values("danieldawsontest3@gmail.com");
INSERT INTO Addresses (EmailAddress) values("recievedanieldawson1@gmail.com");

