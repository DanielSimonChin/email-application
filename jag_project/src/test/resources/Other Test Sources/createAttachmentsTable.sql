/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
DROP TABLE IF EXISTS Attachments;

CREATE TABLE Attachments (
  AttachmentID int(11) NOT NULL auto_increment,
  Image BLOB NOT NULL,
  is_Embedded int(1) NOT NULL default 0, --Is the attachment regular or embedded
  CONSTRAINT is_embedded_constraint CHECK (is_Embedded IN (1,0)),
  PRIMARY KEY (AttachmentID)
);

