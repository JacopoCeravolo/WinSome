#!/bin/bash

echo -n "Building utilities..."

javac -d ../classes ./communication/*.java;
javac -d ../classes ./rmi/*.java;

echo "done"