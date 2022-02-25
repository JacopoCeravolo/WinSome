#!/bin/bash

javac -classpath ../ ./socialnetwork/*.java;
javac -classpath ../ ./rmi/*.java;
javac -classpath ../ ./threads/*.java

javac -classpath ../ ./ServerMain.java