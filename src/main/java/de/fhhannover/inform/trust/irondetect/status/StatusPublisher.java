package de.fhhannover.inform.trust.irondetect.status;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import de.fhhannover.inform.trust.ifmapj.IfmapJ;
import de.fhhannover.inform.trust.ifmapj.IfmapJHelper;
import de.fhhannover.inform.trust.ifmapj.channel.SSRC;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapErrorResult;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapException;
import de.fhhannover.inform.trust.ifmapj.exception.InitializationException;
import de.fhhannover.inform.trust.ifmapj.identifier.Identifier;
import de.fhhannover.inform.trust.ifmapj.identifier.Identifiers;
import de.fhhannover.inform.trust.ifmapj.messages.PublishElement;
import de.fhhannover.inform.trust.ifmapj.messages.PublishRequest;
import de.fhhannover.inform.trust.ifmapj.messages.Requests;
import de.fhhannover.inform.trust.ifmapj.metadata.Cardinality;
import de.fhhannover.inform.trust.ifmapj.metadata.StandardIfmapMetadataFactory;
import de.fhhannover.inform.trust.irondetect.gui.ResultObject;
import de.fhhannover.inform.trust.irondetect.util.Configuration;
import de.fhhannover.inform.trust.irondetect.util.Constants;
import de.fhhannover.inform.trust.irondetect.util.Helper;
import de.fhhannover.inform.trust.irondetect.util.event.Event;
import de.fhhannover.inform.trust.irondetect.util.event.EventReceiver;
import de.fhhannover.inform.trust.irondetect.util.event.EventType;
import de.fhhannover.inform.trust.irondetect.util.event.ResultUpdateEvent;

/**
 * RemoteResultVisualizer
 *
 * @author Marcel Reichenbach
 * @version 0.9
 * @since 0.1
 */
public class StatusPublisher implements EventReceiver {

	private static final Logger logger = Logger.getLogger(StatusPublisher.class);

	private static final String STATUS_METADATA_PREFIX = "irondetect-status";

	private static final String STATUS_METADATA_URI = "http://www.trust.f4.hs-hannover.de/2013/IRONDETECT-STATUS";

	private static final String ATTRIBUTE_INDEX = "Index";

	private static final String ATTRIBUTE_DEVICE = "Device";

	private static final String ATTRIBUTE_ID = "ID";

	private static final String ATTRIBUTE_VALUE = "Value";

	private static final String ATTRIBUTE_TIME_STAMP = "TimeStamp";


	private SSRC mSsrc;

	private StandardIfmapMetadataFactory mF;


	public StatusPublisher(){

		this.mF = IfmapJ.createStandardMetadataFactory();

		try {
			initSsrc();
		} catch (FileNotFoundException e) {
			logger.error("Could not initialize truststore: " + e.getMessage());
			System.exit(Constants.RETURN_CODE_ERROR_TRUSTSTORE_LOADING_FAILED);
		} catch (InitializationException e) {
			logger.error("Could not initialize ifmapj: " + e.getMessage() + ", " + e.getCause());
			System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_INITIALIZATION_FAILED);
		}


		logger.info(StatusPublisher.class.getSimpleName() + " has started.");

		initSession();
	}

	/**
	 * Load {@link TrustManager} instances and create {@link SSRC}.
	 * 
	 * @throws FileNotFoundException
	 * @throws InitializationException
	 */
	private void initSsrc() throws FileNotFoundException,
	InitializationException {
		InputStream isTrustManager = Helper.prepareTruststoreIs(Configuration
				.keyStorePath());
		InputStream isKeyManager = Helper.prepareTruststoreIs(Configuration
				.keyStorePath());

		TrustManager[] tms = null;
		KeyManager[] km = null;

		try {
			tms = IfmapJHelper.getTrustManagers(isTrustManager, Configuration.keyStorePassword());
			km = IfmapJHelper.getKeyManagers(isKeyManager, Configuration.keyStorePassword());
		} catch (InitializationException e1) {
			logger.error("Initialization of keystore failed: " + e1.getMessage() + ", " + e1.getCause());
			System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_INITIALIZATION_FAILED);
		}

		String authMethod = Configuration.ifmapAuthMethod();
		try {
			if (authMethod.equalsIgnoreCase("basic")) {
				logger.info("Creating SSRC using basic authentication to " + Configuration.ifmapUrlBasic());
				this.mSsrc = IfmapJ.createSSRC(Configuration.ifmapUrlBasic(), Configuration.irondetectStatusPublisherUser(), Configuration.irondetectStatusPublisherPassword(), tms);
			}
			else if (authMethod.equalsIgnoreCase("cert")) {
				logger.info("Creating SSRC using certificate-based authentication to " + Configuration.ifmapUrlCert());
				this.mSsrc= IfmapJ.createSSRC(Configuration.ifmapUrlCert(), km, tms);
			}
			else {
				throw new IllegalArgumentException("unknown authentication method '" + authMethod + "'");
			}
		} catch (InitializationException e) {
			logger.error("Could not initialize ifmapj: " + e.getMessage() + ", " + e.getCause());
			System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_INITIALIZATION_FAILED);
		}
	}

	private void initSession() {
		try {
			mSsrc.newSession(Configuration.ifmapMaxResultSize());
		} catch (IfmapErrorResult e) {
			logger.error("Got IfmapErrorResult: " + e.getMessage() + ", " + e.getCause());
		} catch (IfmapException e) {
			logger.error("Got IfmapException: " + e.getMessage() + ", " + e.getCause());
			System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_EXCEPTION);
		}

		logger.debug("Session initialized: Session ID: " + mSsrc.getSessionId()
				+ " - Publisher ID : " + mSsrc.getPublisherId() + " (with MAX_RESULT_SIZE = " + Configuration.ifmapMaxResultSize() + ")");
	}

	@Override
	public void submitNewEvent(Event event) {

		if (event.getType() == EventType.RESULT_UPDATE) {

			ResultObject ro = ((ResultUpdateEvent) event).getPayload();

			PublishElement status = buildPublishStatusElement(ro.getType(), getAttributesStatusMap(ro));

			PublishRequest request = Requests.createPublishReq(status);

			publishStatus(request);

			logger.trace("Received result update: device==" + ro.getDevice()
					+ ", type==" + ro.getType() + ", id==" + ro.getId()
					+ ", value==" + ro.getValue() + ", timestamp==" + ro.getTimeStamp());
		}

	}

	private HashMap<String, String> getAttributesStatusMap(ResultObject rObject){

		HashMap<String, String> attributes = new HashMap<String, String>();

		attributes.put(ATTRIBUTE_INDEX, rObject.getIndex());
		attributes.put(ATTRIBUTE_DEVICE, rObject.getDevice());
		attributes.put(ATTRIBUTE_ID, rObject.getId());
		attributes.put(ATTRIBUTE_VALUE, String.valueOf(rObject.getValue()));
		attributes.put(ATTRIBUTE_TIME_STAMP, rObject.getTimeStamp());

		return attributes;
	}

	private PublishElement buildPublishStatusElement(String elementName, HashMap<String, String> attributes){

		Identifier target = Identifiers.createDev(Configuration.subscriberPdp());

		Document metadata = mF.create(elementName, STATUS_METADATA_PREFIX, STATUS_METADATA_URI, Cardinality.singleValue, attributes);

		PublishElement request = Requests.createPublishNotify(target, metadata);

		return request;

	}

	private void publishStatus(PublishRequest request) {

		try {
			synchronized (mSsrc) {
				mSsrc.publish(request);
			}
			logger.info("New action metadata was published.");
		} catch (IfmapErrorResult e) {
			logger.error("Got IfmapErrorResult: "+  e.getMessage() + ", " + e.getCause());
		} catch (IfmapException e) {
			logger.error("Got IfmapException: "+  e.getMessage() + ", " + e.getCause());
		}

	}

}
