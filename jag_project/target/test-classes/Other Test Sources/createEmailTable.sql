/**
 * Author:  Daniel
 * Created: Sep. 25, 2020
 */
USE EMAILCLIENT;
DROP TABLE IF EXISTS Email;

CREATE TABLE Email (
  EmailID int(11) NOT NULL auto_increment,
  FromAddress varchar(45) NOT NULL default '',
  Subject varchar(50) default '',
  TextMessage varchar(255) default '',
  HtmlMessage varchar(255) default '',
  PRIMARY KEY  (EmailID)
);

INSERT INTO Email (EmailID,FromAddress,Subject,TextMessage,HtmlMessage) values("danieldawsontest1@gmail.com","Jodd Test","This is text message","This is html message");
