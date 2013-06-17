#!/bin/bash
PROG_NAME=$0
COUNTER=10
SLEEP_TIME=5
MAX_RANGE=10
DEVICE=device1
FEATURE=trend.measurement.event
SECURITY_PROPERTIES_FILE=/home/tncuser/irondTrust-0.3.2/security.properties

export IFMAP_USER=trend
export IFMAP_PASS=trend
export IFMAP_URL=http://localhost:7443

while getopts a:b:c:d:e:f: OPTION
do
    case ${OPTION} in
        a) COUNTER=${OPTARG};;
        b) SLEEP_TIME=${OPTARG};;
        c) MAX_RANGE=${OPTARG};;
        d) FEATURE=${OPTARG};;
        e) DEVICE=${OPTARG};;
        f) SECURITY_PROPERTIES_FILE=${OPTARG};;
       \?) echo "Usage: ${PROG_NAME} [ -a {number of for-loops} -b {sleep-time between for-loops} -c {max-range} -d {feature} -e {device} -f {path of security.properties file} ]"
       	   echo "-a: number of for-loop runs (default: $COUNTER)"
       	   echo "-b: sleep time between for-loop runs (default: $SLEEP_TIME)"
       	   echo "-c: maximum value of range (wihtout leading minus) [0..-MAX_RANGE] (default: $MAX_RANGE)"
      	   echo "-d: feature (default: $FEATURE)"
      	   echo "-e: device-name (default: $DEVICE)"
      	   echo "-f: path of security.properties (default: $SECURITY_PROPERTIES_FILE)"
           exit 2;;
    esac
done

echo "Working with following values:"
echo "IFMAP_USER =" $IFMAP_USER
echo "IFMAP_PASS =" $IFMAP_PASS
echo "IFMAP_URL =" $IFMAP_URL
echo "COUNTER =" $COUNTER
echo "SLEEP_TIME =" $SLEEP_TIME
echo "MAX_RANGE =" $MAX_RANGE
echo "DEVICE =" $DEVICE
echo "FEATURE =" $FEATURE
echo "SECURITY_PROPERTIES_FILE =" $SECURITY_PROPERTIES_FILE
echo ""

for (( i = 1; i <= $COUNTER ; i++ )); do
	echo "Changing security.properties for the" $i". time."
	VALUE=$((RANDOM % $MAX_RANGE))
	./tc-change-security-prop.sh $VALUE $SECURITY_PROPERTIES_FILE $DEVICE $FEATURE
	echo "Sleeping for" $SLEEP_TIME "seconds."
	sleep $SLEEP_TIME
	echo ""
done
