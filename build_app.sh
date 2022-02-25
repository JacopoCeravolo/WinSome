#!/bin/bash

cd shared; chmod +x ./build.sh; ./build.sh; cd ../;
cd server; chmod +x ./build.sh; ./build.sh; cd ../;
cd client; chmod +x ./build.sh; ./build.sh; cd ../;