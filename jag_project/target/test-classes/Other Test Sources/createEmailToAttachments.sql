/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
DROP TABLE IF EXISTS EmailToAttachments ;

CREATE TABLE EmailToAttachments (
  EmailToAttachmentsKey  int(11) NOT NULL auto_increment,
  AttachmentID int(11) NOT NULL,
  EmailID int(11) NOT NULL,
  CONSTRAINT fk_attachmentid FOREIGN KEY (AttachmentID) REFERENCES Attachments(AttachmentID),
  CONSTRAINT fk_emailid FOREIGN KEY (EmailID) REFERENCES Email(EmailID),
  PRIMARY KEY  (EmailToAttachmentsKey)
);

