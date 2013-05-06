#!/bin/bash
PROG_NAME=$0
COUNTER=30
SLEEP_TIME=5
DEVICE=device1
FEATURE=ids.snort.event
TYPE=arbitrary
VALUE=none

export IFMAP_USER=trend
export IFMAP_PASS=trend
export IFMAP_URL=http://localhost:7443

while getopts a:b:c:d:e:f: OPTION
do
    case ${OPTION} in
        a) COUNTER=${OPTARG};;
        b) SLEEP_TIME=${OPTARG};;
        c) FEATURE=${OPTARG};;
        d) DEVICE=${OPTARG};;
        e) TYPE=${OPTARG};;
        f) VALUE=${OPTARG};;
       \?) echo "Usage: ${PROG_NAME} [ -a {number of times, value shall be published} -b {sleep-time between publishes} -c {feature} -d {device} -e {type} -f {value} ]"
       	   echo "-a: number of times, the value shall be published (default: $COUNTER)"
       	   echo "-b: sleep time between for-loop runs (default: $SLEEP_TIME)"
      	   echo "-c: feature (default: $FEATURE)"
      	   echo "-d: device-name (default: $DEVICE)"
      	   echo "-e: type (default: $TYPE)"
      	   echo "-f: value (default: $VALUE)"
           exit 2;;
    esac
done

echo "Working with following values:"
echo "IFMAP_USER =" $IFMAP_USER
echo "IFMAP_PASS =" $IFMAP_PASS
echo "IFMAP_URL =" $IFMAP_URL
echo "COUNTER =" $COUNTER
echo "SLEEP_TIME =" $SLEEP_TIME
echo "DEVICE =" $DEVICE
echo "FEATURE =" $FEATURE
echo "TYPE =" $TYPE
echo "VALUE =" $VALUE
echo ""

for (( i = 1; i <= $COUNTER ; i++ )); do
	echo "Publishing value for the" $i". time."
	java -jar featureSingle.jar -d $DEVICE -i $FEATURE -t $TYPE -v $VALUE -u
	
	echo "Sleeping for" $SLEEP_TIME "seconds."
	sleep $SLEEP_TIME
	
	echo ""
done

echo "Finished with publishing values"
