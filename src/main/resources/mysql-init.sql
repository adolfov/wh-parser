CREATE DATABASE IF NOT EXISTS wh_parser;
CREATE user IF NOT EXISTS 'adolfo'@'localhost' identified BY 'walletHub';
GRANT all ON wh_parser.* TO 'adolfo'@'localhost';

