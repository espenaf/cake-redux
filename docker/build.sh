#!/bin/bash

cd ..
mvn install -Dmaven.test.skip
cp target/cake-redux-1.0.TDC-jar-with-dependencies.jar docker/cake-redux.jar
docker build -t trondheimdc/cake docker
rm docker/cake-redux.jar
docker push trondheimdc/cake