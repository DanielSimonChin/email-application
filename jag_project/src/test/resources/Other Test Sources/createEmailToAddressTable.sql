/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
DROP TABLE IF EXISTS EmailToAddress;

CREATE TABLE EmailToAddress (
  EmailToAddressKey int(11) NOT NULL auto_increment,
  EmailID int(11) NOT NULL,
  AddressID int(11) NOT NULL,
  CONSTRAINT fkemailid FOREIGN KEY (EmailID) REFERENCES Email(EmailID),
  CONSTRAINT fkaddressid FOREIGN KEY (AddressID) REFERENCES Addresses(AddressID),
  PRIMARY KEY  (EmailToAddressKey)
);


