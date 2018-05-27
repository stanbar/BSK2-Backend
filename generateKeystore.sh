#!/bin/sh
keytool -keystore keystoreBSK2.jks -genkeypair -alias bsk2 -keyalg RSA -keysize 4096 -validity 5000 -storepass Password123 -keypass Password123 \
-dname 'CN=Stanislaw Baranski, OU=milbar, O=milbar, L=Gdansk, ST=Pomorskie, C=PL'