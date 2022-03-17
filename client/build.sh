#!/bin/bash

echo -n "Building client..."
javac -g -d ../classes -classpath ../ ./ClientMain.java
echo "done"