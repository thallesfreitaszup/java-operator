FROM alpine/helm:3.5.1 as helm-base

FROM adoptopenjdk/openjdk11
ENV APP_TARGET target
ENV APP java-operator-sdk-1.0-SNAPSHOT-jar-with-dependencies.jar
RUN mkdir -p /out

COPY ${APP_TARGET}/${APP} /out
COPY --from=helm-base /usr/bin/helm /usr/bin/helm
RUN helm version
ENTRYPOINT exec java -jar /out/$APP

