-- phpMyAdmin SQL Dump
-- version 4.1.6
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Erstellungszeit: 15. Nov 2016 um 10:10
-- Server Version: 5.6.16
-- PHP-Version: 5.5.9

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Datenbank: `sfp`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tbl_performance_cpfp`
--

CREATE TABLE IF NOT EXISTS `tbl_performance_cpfp` (
  `Classifier` varchar(50) DEFAULT NULL,
  `auc` float DEFAULT NULL,
  `accuracy` float DEFAULT NULL,
  `recall` float DEFAULT NULL,
  `precision` float DEFAULT NULL,
  `type_i` float DEFAULT NULL,
  `type_ii` float DEFAULT NULL,
  `f1` float DEFAULT NULL,
  `fnrate` float DEFAULT NULL,
  `tprate` float DEFAULT NULL,
  `tp` int(11) DEFAULT NULL,
  `tn` int(11) DEFAULT NULL,
  `fp` int(11) DEFAULT NULL,
  `fn` int(11) DEFAULT NULL,
  `correct_classified` float DEFAULT NULL,
  `incorrect_classified` float DEFAULT NULL,
  `weighted_recall` float DEFAULT NULL,
  `weighted_precission` float DEFAULT NULL,
  `weighted_auc` float DEFAULT NULL,
  `pf` float DEFAULT NULL,
  `g_measure` float DEFAULT NULL,
  `error_measure` float DEFAULT NULL,
  `mcc` float DEFAULT NULL,
  `balance` float DEFAULT NULL,
  `train_test_split` float DEFAULT NULL,
  `resample` float DEFAULT NULL,
  `replace_resample` int(1) DEFAULT NULL,
  `bias_to_uniform` float DEFAULT NULL,
  `clasifier_options` varchar(200) DEFAULT NULL,
  `file` varchar(40) DEFAULT NULL,
  `file_testdata` varchar(40) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tbl_performance_sfp`
--

CREATE TABLE IF NOT EXISTS `tbl_performance_sfp` (
  `Classifier` varchar(50) DEFAULT NULL,
  `auc` float DEFAULT NULL,
  `accuracy` float DEFAULT NULL,
  `recall` float DEFAULT NULL,
  `precision` float DEFAULT NULL,
  `type_i` float DEFAULT NULL,
  `type_ii` float DEFAULT NULL,
  `f1` float DEFAULT NULL,
  `fnrate` float DEFAULT NULL,
  `tprate` float DEFAULT NULL,
  `tp` int(11) DEFAULT NULL,
  `tn` int(11) DEFAULT NULL,
  `fp` int(11) DEFAULT NULL,
  `fn` int(11) DEFAULT NULL,
  `correct_classified` float DEFAULT NULL,
  `incorrect_classified` float DEFAULT NULL,
  `weighted_recall` float DEFAULT NULL,
  `weighted_precission` float DEFAULT NULL,
  `weighted_auc` float DEFAULT NULL,
  `pf` float DEFAULT NULL,
  `g_measure` float DEFAULT NULL,
  `error_measure` float DEFAULT NULL,
  `mcc` float DEFAULT NULL,
  `balance` float DEFAULT NULL,
  `train_test_split` float DEFAULT NULL,
  `resample` float DEFAULT NULL,
  `replace_resample` int(1) DEFAULT NULL,
  `bias_to_uniform` float DEFAULT NULL,
  `clasifier_options` varchar(200) DEFAULT NULL,
  `file` varchar(40) DEFAULT NULL,
  `file_testdata` varchar(40) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
