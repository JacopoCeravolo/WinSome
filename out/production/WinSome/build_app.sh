#!/bin/bash

# compiling shared directory
#javac -d classes src/shared/communication/*.java src/shared/rmi/*.java;

# compiling client 
#javac -d classes src/client/*.java;

# compiling server
#javac -cp libs/gson-2.8.9.jar -d classes src/server/socialnetwork/*.java src/server/rmi/*.java src/server/threads/*.java src/server/ServerMain.java;

# creating server jar file
#jar vmcfe ./server_manifest.mf Server.jar src.server.ServerMain classes/server/



cd shared; chmod +x ./build.sh; ./build.sh; cd ../;
cd server; chmod +x ./build.sh; ./build.sh; cd ../;
cd client; chmod +x ./build.sh; ./build.sh; cd ../;