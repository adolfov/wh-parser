SET NAMES utf8mb4 ;
--
-- Table structure for table `blocked_ip`
--

DROP TABLE IF EXISTS `blocked_ip`;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `blocked_ip` (
  `id` int(11) NOT NULL,
  `comments` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `log_entry`
--
DROP TABLE IF EXISTS `log_entry`;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `log_entry` (
  `id` int(11) NOT NULL,
  `date` datetime DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `request` varchar(255) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
