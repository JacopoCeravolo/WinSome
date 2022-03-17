#!/bin/bash

echo -n "Building utilities..."

javac -g -d ../classes ./communication/*.java;
javac -g -d ../classes ./rmi/*.java;

echo "done"