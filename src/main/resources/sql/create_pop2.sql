CREATE DATABASE IF NOT EXISTS `pop2`;

GRANT ALL PRIVILEGES ON `pop2`.* TO 'popUser'@'%'IDENTIFIED BY 'pop' WITH GRANT OPTION;

DROP TABLE IF EXISTS `pop2`.`messages`;
CREATE TABLE  `pop2`.`messages` (
  `guid` varchar(45) NOT NULL,
  `fromAddress` varchar(45) NOT NULL,
  `toAddress` varchar(45) NOT NULL,
  `sentTime` bigint(20) unsigned NOT NULL,
  `subject` varchar(200) NOT NULL,
  `message` longtext NOT NULL,
  PRIMARY KEY (`guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;