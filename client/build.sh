#!/bin/bash

echo -n "Building client..."
javac -d ../classes -classpath ../ ./ClientMain.java
echo "done"