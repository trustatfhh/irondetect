#!/bin/bash
AR=access-request1
DEVICE=device1

# create device
java -jar ar-dev.jar update $AR $DEVICE

# create categories and features
java -jar feature2.jar -i $DEVICE
