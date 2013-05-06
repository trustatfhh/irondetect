#!/bin/bash
PROG_NAME=$0
VALUES_FILENAME=values.txt
SLEEP_TIME=5
DEVICE=device1
FEATURE=trend.measurement.event
SECURITY_PROPERTIES_FILE=/home/tncuser/irondTrust-0.3.2/security.properties

export IFMAP_USER=trend
export IFMAP_PASS=trend
export IFMAP_URL=http://localhost:7443

while getopts a:b:c:d:e:f: OPTION
do
    case ${OPTION} in
    	a) FILENAME=${OPTARG};;
        b) SLEEP_TIME=${OPTARG};;
        c) FEATURE=${OPTARG};;
        d) DEVICE=${OPTARG};;
        e) SECURITY_PROPERTIES_FILE=${OPTARG};;
       \?) echo "Usage: ${PROG_NAME} [ -a {filename for values} -b {sleep-time between for-loops} -c {feature} -d {device} -e {path of security.properties file} ]"
       	   echo "-a: filename with vulnerabilityLevel-values (default: $VALUES_FILENAME)"
       	   echo "-b: sleep time between for-loop runs (default: $SLEEP_TIME)"
      	   echo "-c: feature (default: $FEATURE)"
      	   echo "-d: device-name (default: $DEVICE)"
      	   echo "-e: path of security.properties (default: $SECURITY_PROPERTIES_FILE)"
           exit 2;;
    esac
done

echo "Working with following values:"
echo "IFMAP_USER =" $IFMAP_USER
echo "IFMAP_PASS =" $IFMAP_PASS
echo "IFMAP_URL =" $IFMAP_URL
echo "SLEEP_TIME =" $SLEEP_TIME
echo "DEVICE =" $DEVICE
echo "FEATURE =" $FEATURE
echo "SECURITY_PROPERTIES_FILE =" $SECURITY_PROPERTIES_FILE
echo ""

echo "Loading values from file $VALUES_FILENAME"
VALUES=( `cat "$VALUES_FILENAME" `)

i=1
for T in "${VALUES[@]}"
do
	echo "Changing security.properties for the" $i". time."
	./tc-change-security-prop.sh $T $SECURITY_PROPERTIES_FILE $DEVICE $FEATURE
	echo "Sleeping for" $SLEEP_TIME "seconds."
	sleep $SLEEP_TIME
	echo ""
	i=$((i+1))
done

echo "Finished with reading and submitting file content"
