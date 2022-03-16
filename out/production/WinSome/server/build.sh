#!/bin/bash

echo -n "Building server..."

javac -d ../classes -classpath ../:../libs/gson-2.8.9.jar ./socialnetwork/exceptions/*.java ./socialnetwork/*.java;
javac -d ../classes -classpath ../:../libs/gson-2.8.9.jar ./rmi/*.java;
javac -d ../classes -classpath ../:../libs/gson-2.8.9.jar ./threads/*.java

javac -d ../classes -classpath ../:../libs/gson-2.8.9.jar ServerMain.java

# jar cvfme Server.jar ./server_manifest.mf server.ServerMain ./socialnetwork/*.class ./rmi/*.class ./threads/*.class ServerMain.class

echo "done"