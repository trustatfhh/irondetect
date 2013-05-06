#!/bin/bash
PROG_NAME=$0
VALUES_FILENAME=values.txt
SLEEP_TIME=5
DEVICE=device1
FEATURE=ids.snort.event
TYPE=arbitrary

export IFMAP_USER=trend
export IFMAP_PASS=trend
export IFMAP_URL=http://localhost:7443

while getopts a:b:c:d:e:f: OPTION
do
    case ${OPTION} in
    	a) VALUES_FILENAME=${OPTARG};;
        b) SLEEP_TIME=${OPTARG};;
        c) FEATURE=${OPTARG};;
        d) DEVICE=${OPTARG};;
        e) TYPE=${OPTARG};;
       \?) echo "Usage: ${PROG_NAME} [ -a {filename for values} -b {sleep-time between for-loops} -c {feature} -d {device} -e {type} ]"
       	   echo "-a: file with values (default: $VALUES_FILENAME)"
       	   echo "-b: sleep time between for-loop runs (default: $SLEEP_TIME)"
      	   echo "-c: feature (default: $FEATURE)"
      	   echo "-d: device-name (default: $DEVICE)"
      	   echo "-e: type (default: $TYPE)"
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
echo "TYPE =" $TYPE
echo ""

echo "Loading values from file $VALUES_FILENAME"
VALUES=( `cat "$VALUES_FILENAME" `)
echo ""

i=1
for T in "${VALUES[@]}"
do
	echo "Publishing value #" $i "("$T")"
	java -jar featureSingle.jar -d $DEVICE -i $FEATURE -t $TYPE -v $T -u
	
	echo "Sleeping for" $SLEEP_TIME "seconds."
	sleep $SLEEP_TIME
	
	echo ""
	i=$((i+1))
done

echo "Finished with reading and submitting file content"
