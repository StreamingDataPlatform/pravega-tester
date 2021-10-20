#! /bin/bash
set -ex

ROOT_DIR=$(dirname $0)/..

docker build --no-cache -f ${ROOT_DIR}/pravega-tester/Dockerfile ${ROOT_DIR}
