CREATE DATABASE  IF NOT EXISTS `gv_pers`
USE `gv_pers`;

DROP TABLE IF EXISTS `gv_recovery_property`;

CREATE TABLE `gv_recovery_property` (
  `REC_ID` int(11) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `VALUE` text NOT NULL,
  KEY `new_fk_constraint` (`REC_ID`),
  CONSTRAINT `new_fk_constraint` FOREIGN KEY (`REC_ID`) REFERENCES `gv_recovery_point` (`REC_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


LOCK TABLES `gv_recovery_property` WRITE;
UNLOCK TABLES;

DROP TABLE IF EXISTS `gv_recovery_point`;
CREATE TABLE `gv_recovery_point` (
  `ID` varchar(24) NOT NULL,
  `SERVER` varchar(50) NOT NULL,
  `SYSTEM` varchar(255) NOT NULL,
  `SERVICE` varchar(255) NOT NULL,
  `OPERATION` varchar(255) NOT NULL,
  `RECOVERY_NODE` varchar(255) NOT NULL,
  `ENVIRONMENT` blob NOT NULL,
  `CREATION_DATE` datetime NOT NULL,
  `UPDATE_DATE` datetime NOT NULL,
  `REC_ID` int(11) NOT NULL,
  `STATE` varchar(10) NOT NULL,
  PRIMARY KEY (`REC_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `gv_recovery_point` WRITE;
UNLOCK TABLES;

DROP TABLE IF EXISTS `sequence_name`;
CREATE TABLE `sequence_name` (
  `currval` int(11) NOT NULL,
  PRIMARY KEY (`currval`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

LOCK TABLES `sequence_name` WRITE;
INSERT INTO `sequence_name` VALUES (98);
UNLOCK TABLES;

--
----------------------------------------------------------------------------
----
-- Routine DDL
--
----------------------------------------------------------------------------
----
DELIMITER $$

CREATE DEFINER=`root`@`localhost` FUNCTION `recovery_seq`(i_pseudo char(7))
RETURNS int(11)
BEGIN
                declare o_return bigint unsigned;
                declare v_lock tinyint unsigned;
                select get_lock('sequence_name', 10) into v_lock;
                if i_pseudo='nextval' then
                        update sequence_name set currval=currval+1;
                end if;
                select currval into o_return from sequence_name limit 1;
                select release_lock('sequence_name') into v_lock;
                return o_return;

END