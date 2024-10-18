-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: javafxapp2
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `mutual_funds`
--

DROP TABLE IF EXISTS `mutual_funds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mutual_funds` (
  `fund_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `fund_name` varchar(500) DEFAULT NULL,
  `amount_invested` decimal(15,2) DEFAULT NULL,
  `current_value` decimal(15,2) DEFAULT NULL,
  `investment_date` date DEFAULT NULL,
  `scheme_code` varchar(150) DEFAULT NULL,
  `nav` decimal(10,2) DEFAULT NULL,
  `units` decimal(10,2) DEFAULT NULL,
  `costperunit` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`fund_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `mutual_funds_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mutual_funds`
--

LOCK TABLES `mutual_funds` WRITE;
/*!40000 ALTER TABLE `mutual_funds` DISABLE KEYS */;
INSERT INTO `mutual_funds` VALUES (6,2,'Aditya Birla Sun Life Nifty India Defence Index Fund-Direct Growth',5000.00,4999.96,'2024-10-03','152798',9.50,526.14,9.50),(7,2,'HDFC Balanced Advantage Fund - Growth Plan',18000.00,18001.09,'2024-10-03','100119',514.76,34.97,514.76),(8,2,'SBI CONSUMPTION OPPORTUNITIES FUND - REGULAR PLAN - DIVIDEND',10000.00,9999.77,'2024-10-03','100645',212.81,46.99,212.81),(9,2,'DSP Flexi Cap Fund - Regular Plan - Dividend',5000.00,4999.96,'2024-10-04','100080',71.11,70.31,71.11),(10,2,'HDFC Balanced Advantage Fund - Growth Plan',10000.00,9999.27,'2024-10-04','100119',509.65,19.62,509.65),(11,2,'HDFC Balanced Advantage Fund - Growth Plan',10000.00,9999.27,'2024-10-04','100119',509.65,19.62,509.65),(12,2,'Axis Liquid Fund - Regular Plan - Growth Option',9000.00,9003.57,'2024-10-04','112210',2761.83,3.26,2761.83),(13,2,'ICICI Prudential Large & Mid Cap Fund - Dividend',6000.00,5999.85,'2024-10-04','100348',34.81,172.36,34.81),(14,2,'HDFC Balanced Advantage Fund - IDCW Plan',2000.00,2000.10,'2024-10-04','100120',40.84,48.98,40.84),(15,2,'Axis Liquid Fund - Regular Plan - Growth Option',7000.00,6987.43,'2024-10-04','112210',2761.83,2.53,2761.83),(16,2,'DSP  Ultra Short Fund - Regular Plan - Monthly Dividend',12500.00,12496.90,'2024-10-04','117063',1070.86,11.67,1070.86),(17,2,'ICICI Prudential FMCG Fund - Dividend',45321.00,45320.83,'2024-10-04','100351',101.14,448.10,101.14),(18,2,'PGIM India Ultra Short Duration Fund - Growth',11000.00,11000.10,'2024-10-04','138343',32.40,339.46,32.40),(19,2,'Aditya Birla Sun Life Liquid Fund - Institutional Dividend',20000.00,20000.46,'2024-10-04','100041',108.02,185.15,108.02),(20,2,'Reliance Growth Fund-Dividend Plan-(D)',3000.00,3000.21,'2024-10-04','100375',131.76,22.77,131.76);
/*!40000 ALTER TABLE `mutual_funds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio`
--

DROP TABLE IF EXISTS `portfolio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `portfolio` (
  `portfolio_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `fund_name` varchar(100) DEFAULT NULL,
  `amount_invested` decimal(15,2) DEFAULT NULL,
  `current_value` decimal(15,2) DEFAULT NULL,
  `investment_date` date DEFAULT NULL,
  `type` varchar(100) DEFAULT NULL,
  `units` decimal(10,5) DEFAULT NULL,
  `scheme_code` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`portfolio_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `portfolio_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio`
--

LOCK TABLES `portfolio` WRITE;
/*!40000 ALTER TABLE `portfolio` DISABLE KEYS */;
INSERT INTO `portfolio` VALUES (2,2,'DSP Gilt Fund - Regular Plan - IDCW',5826.60,5826.60,NULL,'SIP',477.00000,'100085'),(3,4,'Aditya Birla Sun Life Liquid Fund -Institutional - IDCW',10000.00,10000.00,NULL,'SIP',92.57288,'100041'),(4,2,'Kotak Gilt-Investment Regular-Growth',1488.42,1488.42,NULL,'SIP',16.01000,'100265'),(11,2,'Aditya Birla Sun Life Nifty India Defence Index Fund-Direct Growth',5000.00,4999.96,NULL,'Mutual Funds',526.14000,'152798'),(12,2,'HDFC Balanced Advantage Fund - Growth Plan',18000.00,18001.09,NULL,'Mutual Funds',34.97000,'100119'),(13,2,'SBI CONSUMPTION OPPORTUNITIES FUND - REGULAR PLAN - DIVIDEND',10000.00,9999.77,NULL,'Mutual Funds',46.99000,'100645'),(14,2,'Nippon India Vision Fund-GROWTH PLAN-Growth Option',1022.26,1022.26,NULL,'SIP',0.70000,'100380'),(15,2,'Nippon India Growth Fund-Growth Plan-Growth Option',120000.00,120000.00,NULL,'SIP',28.68830,'100377'),(16,2,'DSP Flexi Cap Fund - Regular Plan - Dividend',5000.00,4999.96,NULL,'Mutual Funds',70.31000,'100080'),(17,2,'HDFC Balanced Advantage Fund - Growth Plan',10000.00,9999.27,NULL,'Mutual Funds',19.62000,'100119'),(18,2,'NIPPON INDIA VISION FUND - IDCW Option',5705.39,5705.39,NULL,'SIP',79.71000,'100378'),(19,2,'HDFC Balanced Advantage Fund - Growth Plan',10000.00,9999.27,NULL,'Mutual Funds',19.62000,'100119'),(20,2,'ICICI Prudential Large & Mid Cap Fund - IDCW',5000.00,5000.00,NULL,'SIP',143.63689,'100348'),(21,2,'Axis Liquid Fund - Regular Plan - Growth Option',9000.00,9003.57,NULL,'Mutual Funds',3.26000,'112210'),(22,2,'ICICI Prudential Large & Mid Cap Fund - Dividend',6000.00,5999.85,NULL,'Mutual Funds',172.36000,'100348'),(23,2,'HDFC Balanced Advantage Fund - IDCW Plan',2000.00,2000.10,NULL,'Mutual Funds',48.98000,'100120'),(24,2,'Axis Liquid Fund - Regular Plan - Growth Option',7000.00,6987.43,NULL,'Mutual Funds',2.53000,'112210'),(25,2,'DSP  Ultra Short Fund - Regular Plan - Monthly Dividend',12500.00,12496.90,NULL,'Mutual Funds',11.67000,'117063'),(26,2,'ICICI Prudential FMCG Fund - Dividend',45321.00,45320.83,NULL,'Mutual Funds',448.10000,'100351'),(27,2,'PGIM India Ultra Short Duration Fund - Growth',11000.00,11000.10,NULL,'Mutual Funds',339.46000,'138343'),(28,2,'Aditya Birla Sun Life Liquid Fund - Institutional Dividend',20000.00,20000.46,NULL,'Mutual Funds',185.15000,'100041'),(29,2,'Reliance Growth Fund-Dividend Plan-(D)',3000.00,3000.21,NULL,'Mutual Funds',22.77000,'100375');
/*!40000 ALTER TABLE `portfolio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sip`
--

DROP TABLE IF EXISTS `sip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sip` (
  `sip_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `fund_Name` varchar(255) DEFAULT NULL,
  `frequency` varchar(50) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `sip_amount` double DEFAULT NULL,
  `total_units` decimal(10,2) DEFAULT NULL,
  `fund_id` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`sip_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `sip_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sip`
--

LOCK TABLES `sip` WRITE;
/*!40000 ALTER TABLE `sip` DISABLE KEYS */;
INSERT INTO `sip` VALUES (1,2,'DSP Gilt Fund - Regular Plan - IDCW','Monthly','2024-10-01','2027-10-20',5826.6026999999995,477.00,'100085'),(2,4,'Aditya Birla Sun Life Liquid Fund -Institutional - IDCW','Monthly','2024-10-01','2028-10-31',10000,92.57,'100041'),(3,2,'Kotak Gilt-Investment Regular-Growth','Monthly','2024-10-01','2028-10-24',1488.4208820000001,16.01,'100265'),(7,2,'Nippon India Vision Fund-GROWTH PLAN-Growth Option','Monthly','2024-10-04','2026-10-16',1022.25928,0.70,'100380'),(8,2,'Nippon India Growth Fund-Growth Plan-Growth Option','Quarterly','2024-10-04','2026-10-04',120000,28.69,'100377'),(9,2,'NIPPON INDIA VISION FUND - IDCW Option','Quarterly','2024-10-04','2025-10-04',5705.386728,79.71,'100378'),(10,2,'ICICI Prudential Large & Mid Cap Fund - IDCW','Yearly','2024-10-04','2027-10-04',5000,143.64,'100348');
/*!40000 ALTER TABLE `sip` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `Amount` double DEFAULT NULL,
  `units` int DEFAULT NULL,
  `type1` varchar(50) DEFAULT NULL,
  `transaction_date` date DEFAULT NULL,
  `fund_name` varchar(255) DEFAULT NULL,
  `fund_type` varchar(50) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`userid`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES (1,10000,936,'Buy','2024-10-01','DSP Government Securities Fund - Regular Plan - IDCW - Monthly','Mutual Fund',2),(2,20000,1637,'Buy','2024-10-01','DSP Gilt Fund - Regular Plan - IDCW','SIP',2),(3,10000,93,'Buy','2024-10-01','Aditya Birla Sun Life Liquid Fund -Institutional - IDCW','SIP',4),(4,10000,107,'Buy','2024-10-01','Kotak Gilt-Investment Regular-Growth','SIP',2),(5,9332.35,7,'Sell','2024-10-02','Kotak Gilt-Investment Regular-Growth','SIP',2),(6,9146.855545225,2,'Sell','2024-10-02','Kotak Gilt-Investment Regular-Growth','SIP',2),(7,8026.754235,12,'Sell','2024-10-02','Kotak Gilt-Investment Regular-Growth','SIP',2),(8,19929.082789,10,'Sell','2024-10-02','DSP Gilt Fund - Regular Plan - IDCW','SIP',2),(9,7680.182789,1000,'Sell','2024-10-02','DSP Gilt Fund - Regular Plan - IDCW','SIP',2),(10,7067.737789000001,50,'Sell','2024-10-02','DSP Gilt Fund - Regular Plan - IDCW','SIP',2),(11,6160.284235,20,'Sell','2024-10-02','Kotak Gilt-Investment Regular-Growth','SIP',2),(12,10000,750,'Buy','2024-10-02','Aditya Birla Sun Life Income Fund - Regular - Quarterly IDCW','Mutual Fund',2),(13,660.8635999999995,50,'Sell','2024-10-02','Aditya Birla Sun Life Income Fund - Regular - Quarterly IDCW','Mutual Fund',2),(14,12500,303,'Buy','2024-10-02','HDFC Balanced Advantage Fund - IDCW Plan','SIP',2),(15,4251.01908,200,'Sell','2024-10-02','HDFC Balanced Advantage Fund - IDCW Plan','SIP',2),(16,10000,242,'Buy','2024-10-03','HDFC Balanced Advantage Fund - IDCW Plan','Mutual Fund',2),(17,10000,750,'Buy','2024-10-03','Aditya Birla Sun Life Income Fund - Regular - Quarterly IDCW','SIP',2),(18,9332.91291,50,'Sell','2024-10-03','Aditya Birla Sun Life Income Fund - Regular - Quarterly IDCW','SIP',2),(19,7067.6153,0,'Sell','2024-10-03','DSP Gilt Fund - Regular Plan - IDCW','SIP',2),(20,12000,291,'Buy','2024-10-03','HDFC Balanced Advantage Fund - IDCW Plan','Mutual Fund',2),(21,6664.61291,200,'Sell','2024-10-03','Aditya Birla Sun Life Income Fund - Regular - Quarterly IDCW','SIP',2),(22,10000,93,'Buy','2024-10-03','Aditya Birla Sun Life Liquid Fund - Institutional Dividend','Mutual Fund',2),(23,10000,569,'Buy','2024-10-03','SBI Magnum Income Fund - Regular Plan - Half Yearly - Income Distribution cum Capital Withdrawal Option (IDCW)','SIP',2),(24,0,0,'Sell','2024-10-03','DSP Government Securities Fund - Regular Plan - IDCW - Monthly','Mutual Fund',2),(25,0,0,'Sell','2024-10-03','Aditya Birla Sun Life Income Fund - Regular - Quarterly IDCW','Mutual Fund',2),(26,0,0,'Sell','2024-10-03','HDFC Balanced Advantage Fund - IDCW Plan','Mutual Fund',2),(27,0,0,'Sell','2024-10-03','Aditya Birla Sun Life Liquid Fund - Institutional Dividend','Mutual Fund',2),(28,5000,526,'Buy','2024-10-03','Aditya Birla Sun Life Nifty India Defence Index Fund-Direct Growth','Mutual Fund',2),(29,18000,35,'Buy','2024-10-03','HDFC Balanced Advantage Fund - Growth Plan','Mutual Fund',2),(30,15000,70,'Buy','2024-10-03','SBI Consumption Opportunities Fund - Regular Plan - Income Distribution cum Capital Withdrawal Option (IDCW)','Mutual Fund',2),(31,10000,47,'Buy','2024-10-03','SBI CONSUMPTION OPPORTUNITIES FUND - REGULAR PLAN - DIVIDEND','Mutual Fund',2),(32,10000,7,'Buy','2024-10-04','Nippon India Vision Fund-GROWTH PLAN-Growth Option','SIP',2),(33,120000,29,'Buy','2024-10-04','Nippon India Growth Fund-Growth Plan-Growth Option','SIP',2),(34,8324.111280000001,1,'Sell','2024-10-04','Nippon India Vision Fund-GROWTH PLAN-Growth Option','SIP',2),(35,5000,70,'Buy','2024-10-04','DSP Flexi Cap Fund - Regular Plan - Dividend','Mutual Fund',2),(36,10000,20,'Buy','2024-10-04','HDFC Balanced Advantage Fund - Growth Plan','Mutual Fund',2),(37,5826.6026999999995,100,'Sell','2024-10-04','DSP Gilt Fund - Regular Plan - IDCW','SIP',2),(38,10000,140,'Buy','2024-10-04','NIPPON INDIA VISION FUND - IDCW Option','SIP',2),(39,6421.154728000001,50,'Sell','2024-10-04','NIPPON INDIA VISION FUND - IDCW Option','SIP',2),(40,10000,20,'Buy','2024-10-04','HDFC Balanced Advantage Fund - Growth Plan','Mutual Fund',2),(41,5000,144,'Buy','2024-10-04','ICICI Prudential Large & Mid Cap Fund - IDCW','SIP',2),(42,9000,3,'Buy','2024-10-04','Axis Liquid Fund - Regular Plan - Growth Option','Mutual Fund',2),(43,3347.7848820000004,30,'Sell','2024-10-04','Kotak Gilt-Investment Regular-Growth','SIP',2),(44,6000,172,'Buy','2024-10-04','ICICI Prudential Large & Mid Cap Fund - Dividend','Mutual Fund',2),(45,2418.1028819999997,10,'Sell','2024-10-04','Kotak Gilt-Investment Regular-Growth','SIP',2),(46,2000,49,'Buy','2024-10-04','HDFC Balanced Advantage Fund - IDCW Plan','Mutual Fund',2),(47,5705.386728,10,'Sell','2024-10-04','NIPPON INDIA VISION FUND - IDCW Option','SIP',2),(48,7000,3,'Buy','2024-10-04','Axis Liquid Fund - Regular Plan - Growth Option','Mutual Fund',2),(49,6863.74088,1,'Sell','2024-10-04','Nippon India Vision Fund-GROWTH PLAN-Growth Option','SIP',2),(50,5403.3704800000005,1,'Sell','2024-10-04','Nippon India Vision Fund-GROWTH PLAN-Growth Option','SIP',2),(51,12500,12,'Buy','2024-10-04','DSP  Ultra Short Fund - Regular Plan - Monthly Dividend','Mutual Fund',2),(52,2482.6296800000005,2,'Sell','2024-10-04','Nippon India Vision Fund-GROWTH PLAN-Growth Option','SIP',2),(53,45321,448,'Buy','2024-10-04','ICICI Prudential FMCG Fund - Dividend','Mutual Fund',2),(54,11000,339,'Buy','2024-10-04','PGIM India Ultra Short Duration Fund - Growth','Mutual Fund',2),(55,20000,185,'Buy','2024-10-04','Aditya Birla Sun Life Liquid Fund - Institutional Dividend','Mutual Fund',2),(56,1022.25928,1,'Sell','2024-10-04','Nippon India Vision Fund-GROWTH PLAN-Growth Option','SIP',2),(57,3000,23,'Buy','2024-10-04','Reliance Growth Fund-Dividend Plan-(D)','Mutual Fund',2),(58,1488.4208820000001,10,'Sell','2024-10-04','Kotak Gilt-Investment Regular-Growth','SIP',2);
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `userid` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone_number` varchar(15) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `bank_account_number` varchar(20) DEFAULT NULL,
  `ifsc_code` varchar(11) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `pan` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`userid`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (2,'Servesh','Servesh#21','sfdsdfs','255556542','wqw','2024-10-01','czczxc','11223','2024-10-01 15:17:51','qwwq'),(3,'Servesh','Servesh#210','Servesh@gmak;','1255323323','wqw','2024-10-31','czczxc','11223','2024-10-01 17:41:37','qwwq'),(4,'Vedant','Vedant@9','xcs','5556696553',NULL,'2009-10-29',NULL,NULL,'2024-10-01 17:44:59',NULL),(5,'Omkar','Omkar@11','svcxz','47848655','xvxv','2024-10-31','wffdxv','09i00','2024-10-02 09:42:55','cxc'),(6,'abc','Abc123@123','abc@eg.com','7859589698',NULL,'2022-10-13',NULL,NULL,'2024-10-02 19:51:30',NULL),(7,'Rakesh','Rakesh@13','rakesh12@gmail.com','2528521485201','Mulund','2024-11-07','6651651321','VEfmjkdb2','2024-10-03 08:28:28','5113'),(8,'rakesh','Rakesh@12','sddssd','1234567890',NULL,'2024-06-10',NULL,NULL,'2024-10-03 14:36:10',NULL),(9,'register','Register@1','register1@gmil.com','7574312345','Kalyan','2024-10-22','214546','356467','2024-10-04 03:57:45','EN12456576'),(10,'ved','Vedant@1','ved1@gmail.com','1234567891',NULL,'2024-10-01',NULL,NULL,'2024-10-04 04:15:23',NULL),(11,'sun','Dbit@1234','dbit@gmail.com','9988765467','mumbai','2024-10-17','4893778047','hdfc57869','2024-10-04 05:29:39','DCNPC5647a'),(12,'sun12','Sun@1234','Sun@gmail.com','8898990278','mumbai','2024-10-02','90288','djjx','2024-10-04 05:33:48','dhuci00');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-10-15 23:55:47
