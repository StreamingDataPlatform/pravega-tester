# Pravega Tester

Pravega Tester can be used to quickly test Pravega connectivity, authentication, and authorization.
When executed, it will create a new stream, write events, read events, and delete the stream

## Usage

This section shows how to use a pre-built Docker image of the Pravega Tester.
There is no need to clone this repo or install any build tools.

Available Docker images can be found at
[Docker Hub](https://hub.docker.com/r/claudiofahey/pravega-tester/tags).

### Test Pravega on Nautilus from inside of Nautilus

Set environment variables.
```
export PRAVEGA_CONTROLLER_URI=tcp://nautilus-pravega-controller.nautilus-pravega.svc.cluster.local:9090
export PRAVEGA_SCOPE=examples
export IMAGE=claudiofahey/pravega-tester:0.11.13
```

Run Pravega Tester in Kubernetes.
```
kubectl run -n ${PRAVEGA_SCOPE} --rm -it \
--serviceaccount ${PRAVEGA_SCOPE}-pravega \
--env="PRAVEGA_CONTROLLER_URI=${PRAVEGA_CONTROLLER_URI}" \
--env="PRAVEGA_SCOPE=${PRAVEGA_SCOPE}" \
--env="JAVA_OPTS=-Droot.log.level=DEBUG" \
--image ${IMAGE} \
pravega-tester
```

### Test Pravega on Nautilus from outside of Nautilus

Set environment variables.
```
export PRAVEGA_CONTROLLER_URI=tcp://nautilus-pravega-controller.example.com:9090
export PRAVEGA_SCOPE=examples
export IMAGE=claudiofahey/pravega-tester:0.11.13
```

Obtain the Pravega authentication credentials (Keycloak).
```
kubectl get secret ${PRAVEGA_SCOPE}-pravega -n ${PRAVEGA_SCOPE} -o jsonpath="{.data.keycloak\.json}" | \
base64 -d > ${HOME}/keycloak.json
chmod go-rw ${HOME}/keycloak.json
```

Run Pravega Tester in a Docker container.
```
docker run --rm \
-v ${HOME}/keycloak.json:/keycloak.json \
-e PRAVEGA_CONTROLLER_URI \
-e PRAVEGA_SCOPE \
${IMAGE}
```

### Test Pravega without Nautilus

Set environment variables.
```
export PRAVEGA_CONTROLLER_URI=tcp://localhost:9090
export PRAVEGA_SCOPE=examples
export IMAGE=claudiofahey/pravega-tester:0.11.13
```

Run Pravega Tester in a Docker container.
```
docker run --rm --network host \
-e PRAVEGA_CONTROLLER_URI \
-e PRAVEGA_SCOPE \
-e pravega_client_auth_method= \
-e pravega_client_auth_loadDynamic= \
${IMAGE}
```

### Expected Output

Below shows the expected output with Pravega on Nautilus. Some lines are ommitted for brevity.

```
INFO  [2019-09-20 03:11:04.252] [main] c.d.n.p.c.auth.utils.ConfigFileUtils: Final file resolution attempt: /keycloak.json
INFO  [2019-09-20 03:11:05.339] [main] c.d.n.p.c.a.PravegaKeycloakCredentials: Loaded Keycloak Credentials
INFO  [2019-09-20 03:11:05.341] [main] io.pravega.client.ClientConfig: Client credentials were extracted from environment variables. They weren't explicitly supplied as a Credentials object or system properties.
INFO  [2019-09-20 03:11:05.342] [main] io.pravega.example.tester.Tester: config: AppConfiguration{clientConfig=ClientConfig(controllerURI=tcp://xxx.us-west-2.elb.amazonaws.com:9090, credentials=com.dellemc.nautilus.pravega.client.auth.PravegaKeycloakCredentials@2145b572, trustStore=null, validateHostName=true, maxConnectionsPerSegmentStore=10), defaultScope='examples'}
INFO  [2019-09-20 03:11:05.344] [main] io.pravega.example.tester.Tester: streamName=pravega-tester-56e8eec3-283e-47f2-9ef0-3b63c6618f47
INFO  [2019-09-20 03:11:08.020] [main] io.pravega.example.tester.Tester: Writing event 0
INFO  [2019-09-20 03:11:08.561] [main] io.pravega.example.tester.Tester: Writing event 1
INFO  [2019-09-20 03:11:13.014] [main] io.pravega.example.tester.Tester: Read event 0
INFO  [2019-09-20 03:11:13.115] [main] io.pravega.example.tester.Tester: Read event 1
INFO  [2019-09-20 03:11:17.281] [main] io.pravega.example.tester.Tester: PRAVEGA TESTER COMPLETED SUCCESSFULLY.
```

## Build Instructions

This is only needed if you wish to create a custom Docker image.

Obtain the file pravega-keycloak-credentials-*.jar and place it in the lib directory.

You may need to update gradle.properties and pravega-tester/Dockerfile with specific Pravega version numbers.

```
scripts/build-docker.sh
```
