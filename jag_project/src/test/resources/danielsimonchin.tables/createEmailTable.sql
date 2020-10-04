/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
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
  SENTDATE TIMESTAMP NOT NULL,
  RECEIVEDATE TIMESTAMP,
  FOLDERID int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY  (EMAILID)
);
--Emails in the inbox folder
INSERT INTO EMAIL (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,FOLDERID) values("danieldawsontest1@gmail.com","SQL TESTING","This is a plain text message","This is an html message","2020-09-26 18:15:41.000",2);

INSERT INTO EMAIL (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,FOLDERID) values("danieldawsontest2@gmail.com","SQL TESTING","This is a plain text message","This is an html message","2020-09-20 18:15:41.000",2);

INSERT INTO EMAIL (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,FOLDERID) values("danieldawsontest3@gmail.com","Jodd Test","This is a plain text message","This is an html message","2020-09-19 18:15:41.000",2);

INSERT INTO EMAIL (FROMADDRESS,SUBJECT,TEXTMESSAGE,HTMLMESSAGE,SENTDATE,FOLDERID) values("recievedanieldawson1@gmail.com","Jodd Test","This is a plain text message","This is an html message","2020-09-19 18:15:41.000",3);