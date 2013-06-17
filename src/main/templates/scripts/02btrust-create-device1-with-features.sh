#!/bin/bash
AR=access-request1
DEVICE=device1

export IFMAP_USER=sensor
export IFMAP_PASS=sensor

# create device
java -jar ar-dev.jar update $AR $DEVICE

# create categories and features
java -jar feature2.jar -i $DEVICE
