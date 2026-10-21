#!/usr/bin/env bash
# Genera un keystore PKCS12 autofirmado para DESARROLLO LOCAL.
set -e
DIR="$(cd "$(dirname "$0")/.." && pwd)/src/main/resources/keystore"
mkdir -p "$DIR"
keytool -genkeypair \
  -alias secureapi \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore "$DIR/keystore.p12" \
  -storepass changeit -keypass changeit \
  -validity 365 \
  -dname "CN=localhost, OU=Dev, O=Example, L=Chihuahua, ST=Chihuahua, C=MX" \
  -ext "SAN=dns:localhost,ip:127.0.0.1"
echo "Keystore creado en $DIR/keystore.p12"
