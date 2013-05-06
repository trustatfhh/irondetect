#!/bin/bash
VULNERABILITY_VALUE=$1
SECURITY_PROPERTIES_FILE=$2
DEVICE=$3
FEATURE=$4

echo "Replacing vulnerabilityLevel in $SECURITY_PROPERTIES_FILE to $VULNERABILITY_VALUE"
sed -i -e "s|vulnerabilityLevel = -[0-9]*|vulnerabilityLevel = -$VULNERABILITY_VALUE|" $SECURITY_PROPERTIES_FILE

echo "Sending SIG_HUP to IROND"
pkill -1 -f "java .* irond.jar"

echo "Publishing feature update for $FEATURE on device $DEVICE"
java -jar featureSingle.jar -d $DEVICE -i $FEATURE -t arbitrary -v trend -u