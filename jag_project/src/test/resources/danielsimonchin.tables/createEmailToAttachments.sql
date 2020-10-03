/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
DROP TABLE IF EXISTS EMAILTOATTACHMENTS;

CREATE TABLE EMAILTOATTACHMENTS (
  EMAILTOATTACHMENTSKEY  int(11) NOT NULL auto_increment,
  EMAILID int(11) NOT NULL,
  ATTACHMENTID int(11) NOT NULL,
  CONSTRAINT fk_attachmentid FOREIGN KEY (ATTACHMENTID) REFERENCES ATTACHMENTS(ATTACHMENTID),
  CONSTRAINT fk_emailid FOREIGN KEY (EMAILID) REFERENCES EMAIL(EMAILID),
  PRIMARY KEY  (EMAILTOATTACHMENTSKEY)
);

--Attachments for the Email with the Email with the primary key 1
INSERT INTO EMAILTOATTACHMENTS(EMAILID,ATTACHMENTID) values (1,1),(1,2);
INSERT INTO EMAILTOATTACHMENTS(EMAILID,ATTACHMENTID) values (2,1),(2,2);
INSERT INTO EMAILTOATTACHMENTS(EMAILID,ATTACHMENTID) values (3,1);
INSERT INTO EMAILTOATTACHMENTS(EMAILID,ATTACHMENTID) values (4,2);
