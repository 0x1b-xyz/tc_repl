FROM openjdk:8-jre

ENV APP_HOME=/opt/app

RUN mkdir -p ${APP_HOME}
COPY target/*.jar ${APP_HOME}

WORKDIR ${APP_HOME}
CMD exec java -Xmx512m -jar *.jar