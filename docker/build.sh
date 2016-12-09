#!/bin/bash

cd ..
mvn install -Dmaven.test.skip
cp target/cake-redux-1.0.SOS-jar-with-dependencies.jar docker/cake-redux.jar
docker build -t soskonf/cake docker
rm docker/cake-redux.jar
docker push soskonf/cake