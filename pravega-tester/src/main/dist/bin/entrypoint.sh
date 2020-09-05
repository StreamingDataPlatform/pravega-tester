#! /bin/bash
set -e

if [[ ! -z "${CACERT}" ]]; then
  echo Adding custom CA certificate.
  echo "${CACERT}" > /usr/local/share/ca-certificates/ca.crt
  update-ca-certificates
fi

exec /opt/${APP_NAME}/bin/${APP_NAME} "$@"
