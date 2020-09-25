/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
DROP TABLE IF EXISTS Addresses;

CREATE TABLE Addresses (
  AddressID int(11) NOT NULL auto_increment,
  EmailAddress varchar(50) NOT NULL,
  PRIMARY KEY  (AddressID)
);
