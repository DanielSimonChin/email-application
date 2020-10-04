/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS Attachments;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE ATTACHMENTS (
  ATTACHMENTID int(11) NOT NULL auto_increment,
  FILENAME varchar(50) UNIQUE NOT NULL default '',
  CID varchar(50) UNIQUE default '',
  IMAGE MEDIUMBLOB, --leaving this as null for phase 2
  IS_EMBEDDED int(1) NOT NULL default 0, --1 is embedded, 0 means regular
  CONSTRAINT IS_EMBEDDED_CONSTRAINT CHECK (IS_EMBEDDED IN (1,0)),
  PRIMARY KEY (ATTACHMENTID)
);
--Inserting the regular attachment, not giving a CID since it is a regular attachment
INSERT INTO ATTACHMENTS (FILENAME,IS_EMBEDDED) values ('WindsorKen180.jpg',0);
--Inserting the embedded attachment
INSERT INTO ATTACHMENTS (FILENAME,CID,IS_EMBEDDED) values ('FreeFall.jpg','FreeFall.jpg',1);
