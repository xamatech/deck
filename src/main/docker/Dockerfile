FROM openjdk:11-jdk-slim

EXPOSE 8761

ENV ACTIVE_PROFILES development

ENV JVM_ARGS -Xms128m -Xmx512M

ENV LOG_LEVEL INFO

CMD java    $JVM_ARGS \
            -jar \
            -Dspring.profiles.active=$ACTIVE_PROFILES \
            $JVM_VARS \
            /*.jar \
            --logging.level.com.xama=$LOG_LEVEL

RUN mkdir -p /meta
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT
RUN echo $GIT_COMMIT > /meta/version

COPY target/*.jar /