#!/bin/bash
AR=access-request1
DEVICE=device2

# create device
java -jar ar-dev.jar update $AR $DEVICE

# create categories and features
java -jar feature.jar -i $DEVICE -d 2 -c 2 -f 2
