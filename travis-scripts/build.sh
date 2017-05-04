#!/bin/bash
set -ev

if [ "${BUILD}" = "android" ]; then
	./ANDROID/ForecasterApp/gradlew build connectedCheck
elif [ "${BUILD}" = "avr" ]; then
	platformio ci --board=uno
fi