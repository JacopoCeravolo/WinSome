#!/bin/bash

cd shared; rm -rf communication/*.class rmi/*.class utils/*.class; cd ../;
cd server; rm -rf rmi/*.class socialnetwork/*.class threads/*.class ServerMain.class; cd ../;
cd client; rm -rf ClientMain.class; cd ../;