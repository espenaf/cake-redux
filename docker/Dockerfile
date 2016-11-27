FROM java:8
EXPOSE 8082
ADD config /
ADD cake-redux.jar /
cmd java ${JAVA_OPTS} -jar /cake-redux.jar /config
