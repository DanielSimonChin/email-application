USE EMAILCLIENT;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS EMAIL;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE EMAIL (
  EMAILID int(11) NOT NULL auto_increment,
  FROMADDRESS varchar(320) NOT NULL default '',
  SUBJECT varchar(255) default '',
  TEXTMESSAGE TEXT,
  HTMLMESSAGE TEXT,
  SENTDATE TIMESTAMP,
  RECEIVEDATE TIMESTAMP,
  FOLDERID int(11) NOT NULL DEFAULT 1,
  CONSTRAINT FK_EMAIL_FOLDER FOREIGN KEY (FOLDERID) REFERENCES FOLDERS(FOLDERID),
  PRIMARY KEY  (EMAILID)
);

INSERT INTO EMAIL (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,FOLDERID) values("danieldawsontest1@gmail.com","SQL TESTING","This is a plain text message","This is an html message","2020-09-26 18:15:41.000",2);

INSERT INTO EMAIL (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,FOLDERID) values("danieldawsontest2@gmail.com","SQL TESTING","This is a plain text message","This is an html message","2020-09-20 18:15:41.000",2);

INSERT INTO EMAIL (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,FOLDERID) values("danieldawsontest3@gmail.com","Jodd Test","This is a plain text message","This is an html message","2020-09-19 18:15:41.000",2);

INSERT INTO EMAIL (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,FOLDERID) values("recievedanieldawson1@gmail.com","Jodd Test","This is a plain text message","This is an html message",3);

INSERT INTO EMAIL (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,FOLDERID) values("danieldawsontest1@gmail.com","Draft Email Subject","This is a plain text message","This is an html message",3);

INSERT INTO EMAIL (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,RECEIVEDATE,FOLDERID) values("danieldawsontest2@gmail.com","Inbox Subject","This is a plain text message","This is an html message","2020-09-25 18:13:33.000","2020-09-25 18:15:41.000",1);