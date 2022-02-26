#!/bin/bash

cd shared; rm -vrf communication/*.class rmi/*.class utils/*.class; cd ../;
cd server; rm -vrf rmi/*.class socialnetwork/*.class threads/*.class ServerMain.class; cd ../;
cd client; rm -vrf ClientMain.class; cd ../;