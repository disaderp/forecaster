#!/bin/bash
set -ev

if [ "${BUILD}" = "android" ]; then
	bash ./ANDROID/ForecasterApp/gradlew build 
	# TODO? connectedCheck
elif [ "${BUILD}" = "avr" ]; then
	platformio ci --board=uno
fi
