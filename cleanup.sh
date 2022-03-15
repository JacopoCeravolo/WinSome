#!/bin/bash

cd classes/shared; rm -vrf communication/*.class rmi/*.class utils/*.class; cd ../../;
cd classes/server; rm -vrf rmi/*.class socialnetwork/exceptions/*.class socialnetwork/*.class threads/*.class ServerMain.class; cd ../../;
cd classes/client; rm -vrf *.class; cd ../../;
