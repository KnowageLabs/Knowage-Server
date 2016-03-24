DROP TABLE IF EXISTS `cachepersisttest`;

CREATE TABLE `cachepersisttest` (
  `bit` bit(1) DEFAULT NULL,
  `bit8` bit(8) DEFAULT NULL,
  `coltinyint` tinyint(4) DEFAULT NULL,
  `colsmallint` smallint(6) DEFAULT NULL,
  `colmediumint` mediumint(9) DEFAULT NULL,
  `colint` int(11) DEFAULT NULL,
  `colbigint` bigint(20) DEFAULT NULL,
  `colfloat` float DEFAULT NULL,
  `coldouble` double DEFAULT NULL,
  `coldecimal` decimal(10,0) DEFAULT NULL,
  `coldate` date DEFAULT NULL,
  `coldatetime` datetime DEFAULT NULL,
  `coltimestamp` timestamp NULL DEFAULT NULL,
  `coltime` time DEFAULT NULL,
  `colyear` year(4) DEFAULT NULL,
  `colchar8` char(8) DEFAULT NULL,
  `colvarchar8` varchar(8) DEFAULT NULL,
  `colbinary8` binary(8) DEFAULT NULL,
  `colvarbinary8` varbinary(8) DEFAULT NULL,
  `coltinyblob` tinyblob,
  `coltinytext` tinytext,
  `colblob` blob,
  `coltext` text,
  `colmediumblob` mediumblob,
  `colmediumtext` mediumtext,
  `collongblob` longblob,
  `collongtext` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `cachepersisttest` VALUES (0x01,0x02,3,4,5,6,7,8,9,10,'2016-03-21','2016-03-21 11:36:20','2016-03-21 10:36:20','11:36:20',2016,'char8xxx','varchar8',0xAA00000000000000,0x0A,0x74696E79626C6F62,'tinytext',0x626C6F62,'text',0x6D656469756D626C6F62,'mediumtext',0x6C6F6E67626C6F62,'longtext'),(0x01,0x02,3,4,5,6,7,8,9,10,'2016-03-21','2016-03-21 11:36:20','2016-03-21 10:36:20','11:36:20',2016,'char8xxx','varchar8',0xAA00000000000000,0x0A,0x74696E79626C6F62,'tinytext',0x626C6F62,'text',0x6D656469756D626C6F62,'mediumtext',0x6C6F6E67626C6F62,'longtext');
