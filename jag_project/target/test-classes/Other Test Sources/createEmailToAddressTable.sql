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
  RecipientCategory varchar(3) NOT NULL default 'TO',
  CONSTRAINT fkemailid FOREIGN KEY (EmailID) REFERENCES Email(EmailID),
  CONSTRAINT fkaddressid FOREIGN KEY (AddressID) REFERENCES Addresses(AddressID),
  PRIMARY KEY  (EmailToAddressKey)
);
--All the recipients of the email with the primary Key 1
INSERT INTO EmailToAddress (EmailID,AddressID,RecipientCategory) values (1,2,'TO'),(1,3,'CC'),(1,4,'BCC');
--All the recipients of the email with the primary Key 2
INSERT INTO EmailToAddress (EmailID,AddressID,RecipientCategory) values (2,1,'TO'),(2,3,'CC'),(2,4,'CC');
--This email is only sent to two recipients
INSERT INTO EmailToAddress (EmailID,AddressID,RecipientCategory) values (3,1,'BCC'),(3,2,'BCC');
--This email is only sent to 1 person
INSERT INTO EmailToAddress (EmailID,AddressID,RecipientCategory) values (4,1,'TO');
