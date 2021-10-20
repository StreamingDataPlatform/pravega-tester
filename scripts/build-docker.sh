#! /bin/bash
set -ex

ROOT_DIR=$(dirname $0)/..

export DOCKER_REPOSITORY=${DOCKER_REPOSITORY:-claudiofahey}
# Below should match the Pravega version
export IMAGE_TAG=${IMAGE_TAG:-0.9.1}

: ${DOCKER_REPOSITORY?"You must export DOCKER_REPOSITORY"}
: ${IMAGE_TAG?"You must export IMAGE_TAG"}

docker build -f ${ROOT_DIR}/pravega-tester/Dockerfile ${ROOT_DIR} --tag ${DOCKER_REPOSITORY}/pravega-tester:${IMAGE_TAG}
docker push ${DOCKER_REPOSITORY}/pravega-tester:${IMAGE_TAG}
