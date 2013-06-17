#!/bin/bash
PROG_NAME=$0
INIT_MAPS=0
SIG_BEHAVES_LIKE_SMARTPHONE=0
SIG_MALAPP_INSTALLED=0
SIG_OPEN_PORTS=0
SIG_REQUEST_SERVICE_ACCESS=0
PUBLISH_SNORT_FEATURES=0
DEVICE=device1

export IFMAP_USER=trend
export IFMAP_PASS=trend
export IFMAP_URL=http://localhost:7443

while getopts abcdefg: OPTION
do
    case ${OPTION} in
        a) INIT_MAPS=1;;
        b) SIG_BEHAVES_LIKE_SMARTPHONE=1;;
        c) SIG_MALAPP_INSTALLED=1;;
        d) SIG_OPEN_PORTS=1;;
        e) SIG_REQUEST_SERVICE_ACCESS=1;;
        f) PUBLISH_SNORT_FEATURES=1;;
        g) DEVICE=${OPTARG};;
      \?) echo "Usage: ${PROG_NAME} [ -a -b -c -d -e -f -h {device-name} ]"
      	  echo "-a: initialze MAP Server with AR<->IP/MAC/Device and auth-by/as"
      	  echo "-b: publish MacMon features (sigBehavesLikeSmartphone)"
      	  echo "-c: publish AppCrawler features (sigMalappInstalled)"
      	  echo "-d: publish OpenVAS features (sigOpenPorts)"
      	  echo "-e: publish Hospital mockup features (sigRequestServiceAccess)"
      	  echo "-f: publish Snort features"
      	  echo "-g: device-name"
           exit 2;;
    esac
done

echo "Working with following values:"
echo "initialize MAP Server ==" $INIT_MAPS

echo "publish MacMon features (sigBehavesLikeSmartphone) ==" $SIG_BEHAVES_LIKE_SMARTPHONE
echo "publish AppCrawler features (sigMalappInstalled) ==" $SIG_MALAPP_INSTALLED
echo "publish OpenVAS features (sigOpenPorts) ==" $SIG_OPEN_PORTS
echo "publish Hospital mockup features (sigRequestServiceAccess) ==" $SIG_REQUEST_SERVICE_ACCESS
echo "publish Snort features ==" $PUBLISH_SNORT_FEATURES
echo "device ==" $DEVICE
echo ""

if [ "$INIT_MAPS" = "1" ] ; then
	AR=access-request1
	ARMAC=aa:bb:cc:dd:ee:ff
	ARIP=10.0.0.1
	PDP=pdp1
	USER=Bob

	echo "Initializing IF-MAP server"
	java -jar ar-ip.jar update $AR $ARIP
	java -jar ar-mac.jar update $AR $ARMAC
	java -jar auth-by.jar update $AR $PDP
	java -jar auth-as.jar update $AR $USER
	java -jar ar-dev.jar update $AR $DEVICE
	echo ""
fi

if [ "$SIG_BEHAVES_LIKE_SMARTPHONE" = "1" ] ; then
	echo "Publishing MacMon features (sigBehavesLikeSmartphone)"
	java -jar featureSingle.jar -d $DEVICE -i macmon.connectedTo:0.name -t arbitrary -v aploc1 -u
	java -jar featureSingle.jar -d $DEVICE -i macmon.connectedTo:1.name -t arbitrary -v aploc2 -u
	java -jar featureSingle.jar -d $DEVICE -i macmon.connectedTo:2.name -t arbitrary -v aploc3 -u
	java -jar featureSingle.jar -d $DEVICE -i macmon.connectedTo:3.name -t arbitrary -v aploc4 -u
	echo ""
fi

if [ "$SIG_MALAPP_INSTALLED" = "1" ] ; then
	echo "Publishing AppCrawler features (sigMalappInstalled)"
	java -jar featureSingle.jar -d $DEVICE -i appcrawler.blacklisted.app:0.name -t arbitrary -v MalappName -u
	echo ""
fi

if [ "$SIG_OPEN_PORTS" = "1" ] ; then
	echo "Publishing OpenVAS (sigOpenPorts)"
	java -jar featureSingle.jar -d $DEVICE -i vulnerability-scan-result.vulnerability.port -t arbitrary -v 22 -u
	echo ""
fi

if [ "$SIG_REQUEST_SERVICE_ACCESS" = "1" ] ; then
	echo "Publishing Hospital mockup features (sigRequestServiceAccess)"
	java -jar featureSingle.jar -d $DEVICE -i service-notification.service:0.accessRequest -t arbitrary -v true -u
	echo ""
fi

if [ "$PUBLISH_SNORT_FEATURES" = "1" ] ; then
	echo "Publishing Snort features"
	./tc-publish-value-periodically.sh -a 50 -b 30 -c ids.snort.event -d $DEVICE -e arbitrary -f hightrafficdetected
#	./tc-publish-values-from-file.sh -a snort-values.txt -b 30 -c ids.snort.event -d $DEVICE -e arbitrary
	echo ""
fi