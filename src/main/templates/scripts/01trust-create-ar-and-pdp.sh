#!/bin/bash
AR=access-request1
ARMAC=aa:bb:cc:dd:ee:ff
ARIP=10.0.0.1
PDP=pdp1
USER=Bob

export IFMAP_USER=test
export IFMAP_PASS=test

# create PDP
java -jar ar-ip.jar update $AR $ARIP
java -jar ar-mac.jar update $AR $ARMAC
java -jar auth-by.jar update $AR $PDP
java -jar auth-as.jar update $AR $USER
# device-characteristics fehlen noch
