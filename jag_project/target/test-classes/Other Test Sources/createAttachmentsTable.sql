/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS Attachments;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Attachments (
  AttachmentID int(11) NOT NULL auto_increment,
  FileName varchar(50) NOT NULL default '',
  CID varchar(50) NOT NULL default '',
  Image BLOB,
  is_Embedded int(1) NOT NULL default 0, --1 is embedded, 0 means regular
  CONSTRAINT is_embedded_constraint CHECK (is_Embedded IN (1,0)),
  PRIMARY KEY (AttachmentID)
);
--Inserting the regular attachment
INSERT INTO Attachments (FileName,CID,is_Embedded) values ('WindsorKen180.jpg','WindsorKen180.jpg',0);
--Inserting the embedded attachment
INSERT INTO Attachments (FileName,CID,is_Embedded) values ('FreeFall.jpg','FreeFall.jpg',1);
