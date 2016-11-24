Introduction
============
Within this archive you find the replication package for the paper "Performance Tuning for Automotive Software Fault Prediction" by Harald Altinger, Steffen Herbold, Friederike Schneemann, Jens Grabowski, and Franz Wotawa currently submitted to the SANER 2017 Industry Track.  The aim of this replication package is to allow other researchers to replicate our results with minimal effort. 

Requirements
============
- Java 8.
- R 3.3.2
- Optional: JDK and Ant for compilation of the source code.
- Optional: MySQL 5.6.16 for storage of results in a database in addition to CSV files.
- Only tested with Windows 7 and 10, but should work with other Windows versions and operating systems.

Contents
========
- The pre-compiled weka_predictor.jar file for the execution of the experiments.
- The config folder with the configuration for the experiment.
- The scripts runexperiments.bat and runexperiments_mysql.bat for the execution of the experiments under Windows with and without using a MySQL data.
- The src folder with the source code used to define the experiment setup and data storage. 
- The lib folder with the referenced libraries.
- The additional-results folder with complete results figures for all three projects A, K, and L.

How does it work?
=================

In order to execute the replication, you first have to configure your environment as follows. First, you need to prepare your R environment for the execution of Java code by typing the following two commands into your R console.
```R
install.libraries("rJava")
.jinit()
install.libraries("xgboost")
install.libraries("SwarmSVM")
install.libraries("JavaGD")
```
Then, you need to prepare the environment variables of your system. First, add two environment variables and modify the system path.
- R_HOME: [R install path]
- R_LIBS_USER: [R install path]\library
- Add [R install path]\bin to the system path
- You may have to also add [R install path]\bin\x86 or [R install path]\bin\i386 if your R installation if you have both 32bit and 64 bit installations of R. 

Once the configuration of the environment is done, you may execute the experiments using the runexperiments.bat script. *Make sure that the JAVA binaries are also part of the system path, otherwise execution will fail!*

Building from source
====================
The source code and libraries required for compilation are provided within the replication kit. The weka_predictor.jar can be built using the provided Ant script by calling "ant dist". The build results will then appear in the folder "dist". 
