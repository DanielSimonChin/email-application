USE EMAILCLIENT;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS Attachments;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE ATTACHMENTS (
  ATTACHMENTID INT(11) NOT NULL auto_increment,
  EMAILID INT(11) NOT NULL,
  FILENAME varchar(100) NOT NULL default '',
  CID varchar(100),
  ATTACHMENT MEDIUMBLOB, 
  IS_EMBEDDED int(1) NOT NULL default 0, 
  CONSTRAINT IS_EMBEDDED_CONSTRAINT CHECK (IS_EMBEDDED IN (1,0)),  
  PRIMARY KEY (ATTACHMENTID)
);

INSERT INTO ATTACHMENTS (EMAILID,FILENAME,CID,IS_EMBEDDED) values (1,'WindsorKen180.jpg','WindsorKen180.jpg',1);
INSERT INTO ATTACHMENTS (EMAILID,FILENAME,IS_EMBEDDED) values (1,'FreeFall.jpg',0);

INSERT INTO ATTACHMENTS (EMAILID,FILENAME,IS_EMBEDDED) values (2,'WindsorKen180.jpg',0);
INSERT INTO ATTACHMENTS (EMAILID,FILENAME,CID,IS_EMBEDDED) values (2,'FreeFall.jpg','FreeFall.jpg',1);

INSERT INTO ATTACHMENTS (EMAILID,FILENAME,CID,IS_EMBEDDED) values (3,'WindsorKen180.jpg','WindsorKen180.jpg',1);

INSERT INTO ATTACHMENTS (EMAILID,FILENAME,IS_EMBEDDED) values (4,'FreeFall.jpg',0);
