#!/bin/bash

echo -n "Building server..."
javac -classpath ../ ./socialnetwork/*.java;
javac -classpath ../ ./rmi/*.java;
javac -classpath ../ ./threads/*.java

javac -classpath ../ ./ServerMain.java
echo "done"