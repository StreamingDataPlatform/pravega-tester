# Building Container

FROM gradle:4.10-jdk8 as GradleBuilder

USER 0

RUN apt-get update \
    && apt-get install -y \
        maven \
    && rm -rf /var/lib/apt/lists/*

USER gradle

ENV APP_NAME=pravega-tester

COPY --chown=gradle:gradle build.gradle /home/gradle/src/build.gradle
COPY --chown=gradle:gradle gradle.properties /home/gradle/src/gradle.properties
COPY --chown=gradle:gradle settings.gradle /home/gradle/src/settings.gradle
COPY --chown=gradle:gradle ${APP_NAME} /home/gradle/src/${APP_NAME}
COPY --chown=gradle:gradle lib /home/gradle/src/lib

WORKDIR /home/gradle/src

ENV GRADLE_USER_HOME=/home/gradle
ENV CREDENTIALS_VERSION=0.5.0-2306.a5a5cdf-0.11.10-002.985e705

RUN mvn install:install-file \
-Dfile=/home/gradle/src/lib/pravega-keycloak-credentials-${CREDENTIALS_VERSION}-shadow.jar \
-DgroupId=io.pravega \
-DartifactId=pravega-keycloak-credentials \
-Dversion=${CREDENTIALS_VERSION} -Dpackaging=jar

RUN gradle :${APP_NAME}:installDist \
--no-daemon --info --stacktrace \
-PincludePravegaCredentials=true \
-PpravegaCredentialsVersion=${CREDENTIALS_VERSION}

# Runtime Container

FROM openjdk:8-jre

ENV APP_NAME=pravega-tester

ENV pravega_client_auth_method=Bearer
ENV pravega_client_auth_loadDynamic=true
ENV KEYCLOAK_SERVICE_ACCOUNT_FILE=/keycloak.json

COPY --from=GradleBuilder /home/gradle/src/${APP_NAME}/build/install/${APP_NAME} /opt/${APP_NAME}
ENTRYPOINT /opt/${APP_NAME}/bin/${APP_NAME}
