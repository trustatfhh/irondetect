#!/bin/bash
DEVICE=device1
FEATURE=smartphone.android.app:3.permission:0.Name
TYPE=arbitrary
VALUE=qwertz

# delete feature
java -jar featureSingle.jar -d $DEVICE -i $FEATURE -t $TYPE -v $VALUE
