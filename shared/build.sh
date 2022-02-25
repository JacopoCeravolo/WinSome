#!/bin/bash

echo -n "Building utilities..."

javac ./communication/*.java;
javac ./rmi/*.java;
javac ./utils/*.java

echo "done"