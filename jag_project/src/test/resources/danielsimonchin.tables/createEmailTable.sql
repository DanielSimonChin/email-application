/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS Email;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Email (
  EmailID int(11) NOT NULL auto_increment,
  FromAddress varchar(50) NOT NULL default '',
  Subject varchar(50) default '',
  TextMessage varchar(255) default '',
  HtmlMessage varchar(255) default '',
  Folder varchar(25) default 'Inbox',
  SentDate TIMESTAMP NOT NULL,
  PRIMARY KEY  (EmailID)
);
--Emails in the inbox folder
INSERT INTO Email (FromAddress,Subject,TextMessage,HtmlMessage,Folder,SentDate) values("danieldawsontest1@gmail.com","SQL TESTING","This is a plain text message","This is an html message","Inbox","2020-09-26 18:15:41.000");

INSERT INTO Email (FromAddress,Subject,TextMessage,HtmlMessage,Folder,SentDate) values("danieldawsontest2@gmail.com","SQL TESTING","This is a plain text message","This is an html message","Inbox","2020-09-20 18:15:41.000");

INSERT INTO Email (FromAddress,Subject,TextMessage,HtmlMessage,Folder,SentDate) values("danieldawsontest3@gmail.com","Jodd Test","This is a plain text message","This is an html message","Inbox","2020-09-19 18:15:41.000");

INSERT INTO Email (FromAddress,Subject,TextMessage,HtmlMessage,Folder,SentDate) values("recievedanieldawson1@gmail.com","Jodd Test","This is a plain text message","This is an html message","Draft","2020-09-19 18:15:41.000");