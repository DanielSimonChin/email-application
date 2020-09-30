/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
DROP TABLE IF EXISTS EmailToAttachments ;

CREATE TABLE EmailToAttachments (
  EmailToAttachmentsKey  int(11) NOT NULL auto_increment,
  EmailID int(11) NOT NULL,
  AttachmentID int(11) NOT NULL,
  CONSTRAINT fk_attachmentid FOREIGN KEY (AttachmentID) REFERENCES Attachments(AttachmentID),
  CONSTRAINT fk_emailid FOREIGN KEY (EmailID) REFERENCES Email(EmailID),
  PRIMARY KEY  (EmailToAttachmentsKey)
);

--Attachments for the Email with the Email with the primary key 1
INSERT INTO EmailToAttachments(EmailID,AttachmentID) values (1,1),(1,2);
INSERT INTO EmailToAttachments(EmailID,AttachmentID) values (2,1),(2,2);
INSERT INTO EmailToAttachments(EmailID,AttachmentID) values (3,1);
INSERT INTO EmailToAttachments(EmailID,AttachmentID) values (4,2);
