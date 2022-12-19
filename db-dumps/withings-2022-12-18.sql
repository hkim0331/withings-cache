-- MySQL dump 10.19  Distrib 10.3.37-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: 127.0.0.1    Database: withings
-- ------------------------------------------------------
-- Server version	10.3.37-MariaDB-0ubuntu0.20.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `measures`
--

DROP TABLE IF EXISTS `measures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `measures` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `value` int(11) NOT NULL,
  `description` varchar(255) NOT NULL,
  `j_desc` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `measures`
--

LOCK TABLES `measures` WRITE;
/*!40000 ALTER TABLE `measures` DISABLE KEYS */;
INSERT INTO `measures` VALUES (1,1,'Weight (kg)','体重 (kg)'),(2,4,'Height (meter)',NULL),(3,5,'Fat Free Mass (kg)',NULL),(4,6,'Fat Ratio (%)',NULL),(5,8,'Fat Mass Weight (kg)',NULL),(6,9,'Diastolic Blood Pressure (mmHg)',NULL),(7,10,'Systolic Blood Pressure (mmHg)',NULL),(8,11,'Heart Pulse (bpm) - only for BPM and scale devices',NULL),(9,12,'Temperature (celsius)',NULL),(10,54,'SP02 (%)',NULL),(11,71,'Body Temperature (celsius)',NULL),(12,73,'Skin Temperature (celsius)',NULL),(13,76,'Muscle Mass (kg)',NULL),(14,77,'Hydration (kg)',NULL),(15,88,'Bone Mass (kg)',NULL),(16,91,'Pulse Wave Velocity (m/s)',NULL),(17,123,'VO2 max is a numerical measurement of your bodys ability to consume oxygen (ml/min/kg).',NULL),(18,135,'QRS interval duration based on ECG signal',NULL),(19,136,'PR interval duration based on ECG signal',NULL),(20,137,'QT interval duration based on ECG signal',NULL),(21,138,'Corrected QT interval duration based on ECG signal',NULL),(22,139,'Atrial fibrillation result from PPG',NULL);
/*!40000 ALTER TABLE `measures` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schema_migrations`
--

DROP TABLE IF EXISTS `schema_migrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schema_migrations` (
  `id` bigint(20) NOT NULL,
  `applied` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `description` varchar(1024) DEFAULT NULL,
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schema_migrations`
--

LOCK TABLES `schema_migrations` WRITE;
/*!40000 ALTER TABLE `schema_migrations` DISABLE KEYS */;
INSERT INTO `schema_migrations` VALUES (20220823032338,'2022-08-23 10:29:44','users');
/*!40000 ALTER TABLE `schema_migrations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `valid` tinyint(1) DEFAULT 1,
  `name` varchar(255) NOT NULL,
  `belong` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `cid` varchar(255) DEFAULT NULL,
  `secret` varchar(255) DEFAULT NULL,
  `access` varchar(255) DEFAULT NULL,
  `refresh` varchar(255) DEFAULT NULL,
  `userid` varchar(255) DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `line_id` varchar(255) DEFAULT NULL,
  `bot_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (16,1,'hkimura','','hkim@hkim.jp','74b97ffd2b2dc05540d5fea9e26fa8bcc79899979f77484f0c4356d5b5089e28','67373f420abf55a3365bd3f509fbefd20ca2220e79a6d1ffebd5c8f9e50f2580','e5ab1a2dd97b3fee8bc28186afb64b2e06b51fa2','282e5e0cbf7557eabcab9da23b548a2e91ffa808','13662696','2022-12-18 00:00:14','2022-08-31 04:28:27','Ue6431fc148f9e7856610eff00422b24a','SAGA-JUDO'),(17,0,'お名前','インチキです','e@mail','適当','インチキ','適当',NULL,NULL,'2022-09-01 23:45:29','2022-08-31 08:53:26',NULL,NULL),(26,1,'Tkanori Ishii',NULL,NULL,'7b1737295da2e0386e28641ca9bbd114cec391ade722e00780f10b48382eb6fb','95d7865a32459407da777b847f9973a2507740de25c041136ad0527323070b11','2117f644f3c1002baac420c2f1389ad2a502eb75','866ac0280f9025a81b11f3b32bd1cb767515b817','15818365','2022-11-07 00:22:02','2022-11-07 00:21:41',NULL,'SAGA-JUDO'),(27,1,'近藤美月',NULL,'kondomitsu2004@icloud.com','10123f0d3826cf9fd10a40ddc4f12b3390578beda953fc4317ab07d572bb71a4','67bdd3a33a27e00d3a17a9045bf7879bf838f06f3b3e3525823e6a2b7ae0ddd3','6452ea6e17da6af195c8d11b6ad7a78f9041fdd7','2e2cf46d22da3ac357f998583085d7f95b94014d','32037610','2022-11-08 00:55:03','2022-11-08 00:54:46','Uc62759cce6c4db9baf43d5c1c7efda42','SAGA-JUDO'),(28,1,'水間仁子',NULL,'gintama.0503@icloud.com','af41a14beb75688a2acd45b4a32494a1e3ba16e0911aaf0ab34c1d98af754b0a','f04baa5135adf4518693b9c85d1b013d73f9c2b380a522336f5890d70857c500','e98298413a0a0ac36035871818f1904b2394b73d','94d6c32ebb5a9062870d6ae803569a8eece62fc2','32037149','2022-11-08 00:56:55','2022-11-08 00:56:47','U7e2908f5448e3b8b5e664afffa577fe6','SAGA-JUDO'),(29,1,'香田桜次郎',NULL,'oujirou0109@icloud.com','885b601d34fa02efa016383cadc0e4ab450d17f9117c583c948f721672aff9fd','a757c68159485783f8eb9fdb27c42030f947da9361f57f91b2c4b2e822ae4694','90604afa9e236db77ffcf1deafa12d5b956ccd3b','217cc07d1ef0565b594ceebceb48a7db64909dfc','32037204','2022-11-08 01:00:14','2022-11-08 01:00:08','U687ccd32255e75423ef374b794489e30','SAGA-JUDO'),(32,1,'山﨑悠翔',NULL,'yama4800yuto5200@icloud.com','ff80ae0f26bd9d5a052da0b790d04a6fefeaf4b8f7624bbc783aa4d968d0e976','4103773326ad7263c53a888eba1d0567e5baec81e18e4108955fb22a0b0ea4d9','dec411315e9fd749ccbf6f3a4de835c81c155fce','ddba8e407406fd9097fa86459d40621755e7346e','32037236','2022-11-08 01:24:11','2022-11-08 01:24:03','Ucffb7b024faba907d9a56083035187fa','SAGA-JUDO'),(33,1,'森静玖',NULL,'shizuku17411@softbank.ne.jp','4375ac2a3082cd7a4d5d90bba6b725cf201c45f6f041582ca45daf7e1688943f','1384e2261d0d0a9c955ecfc6425f4a6e2ee2480149643a788425d51dfabf4751','43b384ab9b25117322cd99723c2b1fc9e5e0e4be','3213022464a9a8bd76e09582c4dcd3aee67af896','32074633','2022-11-08 01:29:05','2022-11-08 01:28:58','U4c2a6d5eab750c8f9eb14f2475b87183','SAGA-JUDO'),(34,1,'宮永啓吾',NULL,'keigo.1118@icloud.com','cbbe8a0984cc94b645b2e9d79a5f08dc8e411a2672193c81af67822ccf2f36ca','a37f2429f93098c7e53360f0949d03d72d989347683280c55c49203d9b1757fc','8ad82cfa157dbd95e5466ce3c080b559e124201a','be528c5b73ca9a3007b74e1dcddf653a6f5ab803','32074563','2022-11-08 01:31:04','2022-11-08 01:30:58','U4d5ef4268bfa764780853c59691fa7ec','SAGA-JUDO'),(35,1,'永松莉菜',NULL,'rina.n.20040626@icloud.com','aa6bbd9314359ec2024446ef92e30d6436fba142b4e88f4c04331700b5795ee7','f6c4884913493e6bac7b53c98975539a278f77172bae699f1ed21dc25f3505e6','70ba70a9010f015031d5820cbbff72ac80530653','5062c2a66a892f06a28d889d16fc9d3787b66b0a','32075880','2022-11-08 01:34:15','2022-11-08 01:34:08','Ucbcbd75e1596cd354c74b4b7c1aa7214','SAGA-JUDO'),(36,1,'清瀬雄大',NULL,'yudai.4279@icloud.com','2e46f3dcef064d20cb446e20154571bd4862ebe393e99ebf3929d6630c251016','cfda15a387282d794c3fb9bfa65cb5f480dc95c5a205811a580b3a804551203b','4ddb48e442e1a93b67e83abb2dbacae677f077d5','ae0bce0401aaf5d0397b4c3a09dba2edd81d07d1','32074600','2022-11-08 01:36:57','2022-11-08 01:36:51','U313602fc209750b2f02540ef252137af','SAGA-JUDO'),(37,1,'本村莉菜',NULL,'rinamotomura@icloud.com','7c703e8e16bac5d8a83bb7e54cc0f353fd692d51da0144103880fe1c51d2c74e','a87f6bb959b08d511194e376b1a1af8380e3ccd7fb33f922bab133813612978f','acee0a177713ea1c4fdad40370bdc97224e84568','81d717c10c2a4c344de8cda585dc90da11d4a693','32037380','2022-11-10 13:36:11','2022-11-08 01:38:19','U60defff4e54547db43c9a78b7e7d47c1','SAGA-JUDO'),(38,1,'古賀学',NULL,'judo0402@i.softbank.jp','74bbae41b780cb2b133acca1d15c605743d5e283b5bc302cf685668a738debaf','7411b2cc251873165b4bc9f81150725af7bab28aaaf889765a9b625fb2b0c675','52ac9205f9b272b3b3be588d94b9595d4c064d73','89da5e9a85de124be31efb75eb6c55704ec7fcc6','32037021','2022-11-08 01:41:49','2022-11-08 01:41:41','U60f8252d0f676489fd15a606b729d0cb','SAGA-JUDO'),(40,1,'田中龍雅',NULL,'ryuga712judo@gmail.com','5b62bdf7fac3ef2b9115400ab5b72503fa3e391560117a0ec90057f3c9cec611','27a0041203469c8c0b597a40192fdd92edffa59ec953a1f67ec25b0a4236d91f','3517cc46dda155b813fe32f7bbb925e5a6811d29','bc79b95bce3b7636847347048dd4ec056363f243','32037943','2022-11-10 23:46:00','2022-11-10 23:45:51','Ud3f5ecd507bc9fdc8c9cbc65cc0d1587','SAGA-JUDO'),(41,1,'長谷心美',NULL,'kokomihase@yahoo.co.jp','f7a43d317dd0b987a3ac9221f03db32e81a62805d5aea40ae12a8b668100d39f','e808fddfd091e6af5d5d7d74d65254971c028535f78339276e929b3a09250d93','77263f7a198ab805b1514f9f22608878771b382d','87ae4d6a1c975faee56bea348e51883d8b673393','32037150','2022-11-10 23:49:31','2022-11-10 23:49:22','U8d36b9439cc5dfa620ce8d885c23a95f','SAGA-JUDO'),(42,1,'西明虹',NULL,'anji.judo@icloud.com','89c70768d1244357d31a9d2f3f43c2cb31ffc0c963241305162dbcf75a8b95d2','c1671e822d3ada91dc434cf0f42edfb53427a0a06657bc4f59d4dd08c630465c','9b8783f32d00e4602f18e5d2b5e886a6bbac844f','8810d9ec1436f7e03ead339b46533ed6cfe77970','32037082','2022-11-10 23:51:10','2022-11-10 23:51:03','U41d098d6863e91c9ee6c41d710e8eb4a','SAGA-JUDO'),(44,1,'板橋真子',NULL,'makojudo1009@icloud.com','5a7886e97f96c57025bce352876fbb409db9324cc73b410656945346a77d8500','7cd0117cc4147a76e72bf5113602004a078306c7478c741cb3d874ace8ce92ae','7953f960c722f8c2fac6e88997ec3442423837c6','af4770ecaeeff69150225bb0e7c5a683255487c1','32037279','2022-11-11 07:15:07','2022-11-10 23:53:44','Ue583b549d2ce4eb075d6e3053b73b3e1','SAGA-JUDO'),(45,1,'清水優陸',NULL,'youluqingshui@gmail.com','36b196158c227213acc79a607dee2955de69791f757f7c38557ac4eb1eb49cec','31fa04a5bd1c917f47b6279cfcc8cff3321c3de8f8370cb4d8eb45ebbe2dd630','617d3ae5d14d4f45d61f08edb05fe2ee2fd151ad','4f826ff9d070f3c03f49244df68ee8ed5341a4c7','32074549','2022-11-10 23:58:06','2022-11-10 23:57:59','Ue2e3a63b5ba29aaf21300b243924b9ee','SAGA-JUDO'),(46,1,'中野弥花',NULL,'michael916@ezweb.ne.jp','c7b54d1990b0d9a199a745ac57d72c523f468aeb627377b46f5297034b3a0f7c','4c2f288f2059133b5afc8605acaace24e78135d5030dd02320d7712e9592be24','4f7cadaaa6f14b27576212516e930095b4ed8545','6cf038ced0a93f0c870fedc8a729d5ec3adedeb5','32056429','2022-11-11 00:13:35','2022-11-11 00:13:27','Uf9d78be649f74b5bcf4587423dfe7387','SAGA-JUDO'),(47,1,'長瀬めぐみ',NULL,'n.mgm1008@gmail.com','8f4b3fe1e27ea1d20145d266a0efb90c17d45e80e1d060324f8d5ab6e69af54f','cc0b66a17c6f4b5a6766bed848466fd6fc564396dd95844c6352cd341e40ea42','3521878de84a9866a0fc34bb4956f99f0702d54c','0da4d2c1a852e04a723e797f34d4ba40acef342a','32044887','2022-11-12 05:47:29','2022-11-12 05:47:09',NULL,NULL),(48,1,'野中颯汰',NULL,'nonasou0925@icloud.com','cf841eafe6eb7543ceaac2d573fb3b82c3f2912e6c43d3b2c450c668e2bc2040','ce192784b7ec608b22746b97c9a44c050622fea740bb00632c5526aa3dc9c345','95ed3c8593e167641487ae0590df5c753ab736ef','ba347eed2297ef9e3c0fa61dc79d79a2090c918c','32062522','2022-11-13 14:08:57','2022-11-12 11:41:40',NULL,NULL);
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

-- Dump completed on 2022-12-18 22:05:53
