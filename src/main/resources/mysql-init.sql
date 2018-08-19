CREATE DATABASE wh_parser;
CREATE user 'adolfo'@'localhost' identified BY 'walletHub';
GRANT all ON wh_parser.* TO 'adolfo'@'localhost';

CREATE TABLE `wh_parser`.`log_entry` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `date` DATETIME NOT NULL,
  `ip` VARCHAR(45) NOT NULL,
  `request` VARCHAR(45) NULL,
  `status` VARCHAR(45) NULL,
  `user_agent` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));
