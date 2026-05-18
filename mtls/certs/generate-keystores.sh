#!/usr/bin/env bash
# Generate ONE standard .jks per app, each holding BOTH:
#   - the app's own private key (PrivateKeyEntry)
#   - the peer app's public cert (trustedCertEntry)
#
# Result:
#   a.jks -> { PrivateKeyEntry "a", trustedCertEntry "b" }
#   b.jks -> { PrivateKeyEntry "b", trustedCertEntry "a" }
#
# Each .jks is used as BOTH key-store AND trust-store by its owning app.
# SAN dns:localhost is critical so TLS hostname verification passes
# when A connects to https://localhost:8443.

set -euo pipefail
cd "$(dirname "$0")"

STOREPASS="changeit"
VALIDITY=3650

rm -f a.jks b.jks a.cer b.cer

# 1) Generate A's keypair into a.jks
keytool -genkeypair \
  -alias a -keyalg RSA -keysize 2048 -validity ${VALIDITY} \
  -dname "CN=app-a, OU=Training, O=VictorRentea, L=Bucharest, C=RO" \
  -ext "san=dns:localhost,ip:127.0.0.1" \
  -keystore a.jks -storetype JKS \
  -storepass ${STOREPASS} -keypass ${STOREPASS}

# 2) Generate B's keypair into b.jks
keytool -genkeypair \
  -alias b -keyalg RSA -keysize 2048 -validity ${VALIDITY} \
  -dname "CN=app-b, OU=Training, O=VictorRentea, L=Bucharest, C=RO" \
  -ext "san=dns:localhost,ip:127.0.0.1" \
  -keystore b.jks -storetype JKS \
  -storepass ${STOREPASS} -keypass ${STOREPASS}

# 3) Export A's public cert
keytool -exportcert -rfc \
  -alias a -keystore a.jks -storepass ${STOREPASS} \
  -file a.cer

# 4) Export B's public cert
keytool -exportcert -rfc \
  -alias b -keystore b.jks -storepass ${STOREPASS} \
  -file b.cer

# 5) Import B's cert INTO a.jks (so A trusts B's server cert)
keytool -importcert -noprompt \
  -alias b -file b.cer \
  -keystore a.jks -storetype JKS \
  -storepass ${STOREPASS}

# 6) Import A's cert INTO b.jks (so B accepts A's client cert)
keytool -importcert -noprompt \
  -alias a -file a.cer \
  -keystore b.jks -storetype JKS \
  -storepass ${STOREPASS}

echo
echo "=== Generated files ==="
ls -la *.jks *.cer
echo
echo "=== a.jks (A's private key + B's trusted cert) ==="
keytool -list -keystore a.jks -storepass ${STOREPASS}
echo
echo "=== b.jks (B's private key + A's trusted cert) ==="
keytool -list -keystore b.jks -storepass ${STOREPASS}
