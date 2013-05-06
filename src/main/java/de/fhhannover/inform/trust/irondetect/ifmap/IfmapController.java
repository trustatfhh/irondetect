/**
 * 
 */
package de.fhhannover.inform.trust.irondetect.ifmap;

/*
 * #%L
 * ====================================================
 *   _____                _     ____  _____ _   _ _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \|  ___| | | | | | |
 *    | | | '__| | | / __| __|/ / _` | |_  | |_| | |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _| |  _  |  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_|   |_| |_|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Hochschule Hannover 
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.inform.fh-hannover.de/
 * 
 * This file is part of irongui, version 0.0.3, implemented by the Trust@FHH 
 * research group at the Hochschule Hannover, a program to visualize the content
 * of a MAP Server (MAPS), a crucial component within the TNC architecture.
 * 
 * The development was started within the bachelor
 * thesis of Tobias Ruhe at Hochschule Hannover (University of
 * Applied Sciences and Arts Hannover). irongui is now maintained
 * and extended within the ESUKOM research project. More information
 * can be found at the Trust@FHH website.
 * %%
 * Copyright (C) 2010 - 2013 Trust@FHH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.fhhannover.inform.trust.ifmapj.IfmapJ;
import de.fhhannover.inform.trust.ifmapj.IfmapJHelper;
import de.fhhannover.inform.trust.ifmapj.binding.IfmapStrings;
import de.fhhannover.inform.trust.ifmapj.channel.SSRC;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapErrorResult;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapException;
import de.fhhannover.inform.trust.ifmapj.exception.InitializationException;
import de.fhhannover.inform.trust.ifmapj.identifier.AccessRequest;
import de.fhhannover.inform.trust.ifmapj.identifier.Device;
import de.fhhannover.inform.trust.ifmapj.identifier.Identifier;
import de.fhhannover.inform.trust.ifmapj.identifier.Identifiers;
import de.fhhannover.inform.trust.ifmapj.identifier.IdentityType;
import de.fhhannover.inform.trust.ifmapj.messages.MetadataLifetime;
import de.fhhannover.inform.trust.ifmapj.messages.PublishElement;
import de.fhhannover.inform.trust.ifmapj.messages.PublishNotify;
import de.fhhannover.inform.trust.ifmapj.messages.PublishRequest;
import de.fhhannover.inform.trust.ifmapj.messages.PublishUpdate;
import de.fhhannover.inform.trust.ifmapj.messages.Requests;
import de.fhhannover.inform.trust.ifmapj.messages.ResultItem;
import de.fhhannover.inform.trust.ifmapj.messages.SearchRequest;
import de.fhhannover.inform.trust.ifmapj.messages.SearchResult;
import de.fhhannover.inform.trust.ifmapj.messages.SubscribeUpdate;
import de.fhhannover.inform.trust.irondetect.util.Configuration;
import de.fhhannover.inform.trust.irondetect.util.Constants;
import de.fhhannover.inform.trust.irondetect.util.Helper;
import de.fhhannover.inform.trust.irondetect.util.Triple;

/**
 * The {@link IfmapController} is responsible for fetching all changes related
 * to Feature or Category metadata from the MAPS. This is basically done by a
 * two-step approach:</br>
 * <ol>
 * <li>
 * For each PDP that is configured, a Subscription on the PDPs Device Identifier
 * is created (max-depth=2). This way, we are notified each time a new device is
 * authenticated by the PDP and we learn the Device Identifier of the new
 * endpoint.</li>
 * <li>
 * For each new endpoint, another Subscription is created to monitor the
 * Category and Feature metadata.</li>
 * </ol>
 * 
 * 
 * @author ibente
 * 
 */
public class IfmapController {

	private static final Logger logger = Logger
			.getLogger(IfmapController.class);

	/**
	 * The only SSRC
	 */
	private SSRC mSsrc;

	/**
	 * The only IF-MAP to Feature-Mapper
	 */
	private IfmapToFeatureMapper mMapper;

	private String mPublisherId;

	private DocumentBuilder documentBuilder;

	private NewEventPoller mNewEventPoller;

	private NewDevicePoller mNewDevicePoller;

	private Map<String, Integer> mAlertInstanceNumber;
	
	/**
	 * @param ifmapToFeatureMapper
	 * 
	 */
	public IfmapController(IfmapToFeatureMapper ifmapToFeatureMapper) {
		mMapper = ifmapToFeatureMapper;
		mAlertInstanceNumber = new HashMap<String, Integer>();

		try {
			initSsrc();
		} catch (FileNotFoundException e) {
			logger.error("Could not initialize truststore: " + e.getMessage());
			System.exit(Constants.RETURN_CODE_ERROR_TRUSTSTORE_LOADING_FAILED);
		} catch (InitializationException e) {
			logger.error("Could not initialize ifmapj: " + e.getMessage() + ", " + e.getCause());
			System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_INITIALIZATION_FAILED);
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			this.documentBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Could not configure XML parser: " + e.getMessage() + ", " + e.getCause());
			System.exit(Constants.RETURN_CODE_ERROR_PARSER_CONFIGURATION_FAILED);
		}
		
		logger.info(IfmapController.class.getSimpleName() + " has started.");

		initSession();

		startWorker();
	}

	public void submitNewDevice(Device device) {
		try {
			this.mNewDevicePoller.put(device);
			logger.info("New device was submitted.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
				this.mSsrc = IfmapJ.createSSRC(Configuration.ifmapUrlBasic(), Configuration.irondetectDeviceSubscriberUser(), Configuration.irondetectDeviceSubscriberPassword(), tms);
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
		mPublisherId = mSsrc.getPublisherId();
	}

	private void startWorker() {
		logger.debug("starting worker threads ...");

		DeviceSearcher searcher = new DeviceSearcher(this);
		Thread searcherThread = new Thread(searcher,
				DeviceSearcher.class.getSimpleName());
		searcherThread.start();

		EndpointPoller poller = null;
		try {
			poller = new EndpointPoller(mMapper, mSsrc.getArc());
		} catch (InitializationException e) {
			logger.error("Could not initialize ifmapj: " + e.getMessage() + ", " + e.getCause());
			System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_INITIALIZATION_FAILED);
		}

		Thread pollerThread = new Thread(poller,
				EndpointPoller.class.getSimpleName());
		
		this.mNewEventPoller = new NewEventPoller(this);
		Thread eventPollerThread = new Thread(this.mNewEventPoller, NewEventPoller.class.getSimpleName());
		
		this.mNewDevicePoller = new NewDevicePoller(this);
		Thread devicePollerThread = new Thread(this.mNewDevicePoller, NewDevicePoller.class.getSimpleName());

		pollerThread.start();
		eventPollerThread.start();
		devicePollerThread.start();
	}

	/**
	 * @param device
	 * @throws IfmapErrorResult
	 * @throws IfmapException
	 */
	void subscribeForSmartphone(Device device) throws IfmapErrorResult,
			IfmapException {
		logger.debug("Subscribe for new smartphone device " + device + " ...");

		SubscribeUpdate subscribeUpdate = Requests.createSubscribeUpdate();

		try {
			subscribeUpdate.setName(createSHA1(device.getName()).substring(0, 
                                device.getName().length() > 20 ? 19 : device.getName().length()-1));	// TODO FIXME hackhackhack
		} catch (NoSuchAlgorithmException e) {
			logger.error("SHA1ing failed.");
		}
		subscribeUpdate.setStartIdentifier(device);
		subscribeUpdate.setMaxDepth(10000); // FIXME maxDepth needed?
		subscribeUpdate.setMaxSize(Configuration.ifmapMaxResultSize());
		subscribeUpdate.setMatchLinksFilter(Constants.MATCH_LINKS_SMARTPHONE);

		subscribeUpdate.addNamespaceDeclaration("esukom", Constants.ESUKOM_NAMESPACE_URI);

		synchronized (mSsrc) {			
			mSsrc.subscribe(Requests.createSubscribeReq(subscribeUpdate));
		}
		logger.debug("Subscription done!");
	}

	/**
	 * @param target
	 * @param event
	 */
	public void submitNewEvent(String device, List<Document> metadataList, boolean ifmapEvent) {
		try {
			this.mNewEventPoller.put(device, metadataList, ifmapEvent);
			logger.info("New Event was inserted.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param event
	 */
	void publishEvent(Triple<String, List<Document>, Boolean> event) {
		logger.info("Trying to publish new action metadata");

		List<PublishElement> elements = new ArrayList<PublishElement>();
		
		String device = event.getFirstElement();
		List<Document> metadataList = event.getSecondElement();
		boolean ifmapEvent = event.getThirdElement();
		
		Identifier target = null;
		int tmpInstanceNumber = 0;
		
		if (mAlertInstanceNumber.containsKey(device)) {
			tmpInstanceNumber = mAlertInstanceNumber.get(device);
		}
		
		if (ifmapEvent) {
			target = searchAccessRequestDeviceIdentifier(device);
		} else {
			target = Identifiers.createIdentity(IdentityType.other,
					Constants.ALERT_IDENTIFIER_NAME + ":" + tmpInstanceNumber, device, Constants.OTHER_TYPE_DEFINITION);
			tmpInstanceNumber++;
			mAlertInstanceNumber.put(device, tmpInstanceNumber);
			
			logger.trace("Device name for new event: " + device);
			elements.add(createLinksForDevice(device, target));
		}
		
		if (Configuration.publishNotify()) {
			for (Document metadata : metadataList) {
				elements.add(Requests.createPublishNotify(target, metadata));
			}
		} else {
			for (Document metadata : metadataList) {
				elements.add(Requests.createPublishUpdate(target, metadata));
			}
		}

		if (elements.size() > 0) {
			PublishRequest request = Requests.createPublishReq(elements);

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
		} else {
			logger.error("PublishElement was null.");
		}
	}

	/**
	 * @return
	 */
	public String getPublisherId() {
		return this.mPublisherId;
	}
	
	/**
	 * @param device
	 * @return
	 */
	private PublishElement createLinksForDevice(String device, Identifier alertFeature) {
		Document deviceCategory = this.documentBuilder.newDocument();
		Element e = deviceCategory.createElementNS(Constants.ESUKOM_NAMESPACE_URI, Constants.ESUKOM_NAMESPACE_PREFIX + ":" + "device-category");
		e.setAttributeNS(null, "ifmap-cardinality", "singleValue");
		
		deviceCategory.appendChild(e);
		
		logger.trace("Adding link to new alert-instance = " + mAlertInstanceNumber.get(device));
		
		if (Configuration.publishNotify()) {
			PublishNotify result = Requests.createPublishNotify();
			result.setIdentifier1(Identifiers.createDev(device));
			result.setIdentifier2(alertFeature);
			result.addMetadata(deviceCategory);
			return result;
		} else {
			PublishUpdate result = Requests.createPublishUpdate();
			result.setIdentifier1(Identifiers.createDev(device));
			result.setIdentifier2(alertFeature);
			result.addMetadata(deviceCategory);
			result.setLifeTime(MetadataLifetime.session);
			return result;
		}
	}
	
	/**
	 * @param device
	 * @return
	 */
	private Identifier searchAccessRequestDeviceIdentifier(String device) {
		Identifier startIdentifier = Identifiers.createDev(device);
		
		SearchRequest searchRequest = Requests.createSearchReq(Constants.MATCH_LINKS_ACCESS_REQUEST, 1, null, Configuration.ifmapMaxResultSize(), null, startIdentifier);
		searchRequest.addNamespaceDeclaration(IfmapStrings.BASE_PREFIX, IfmapStrings.BASE_NS_URI);
		searchRequest.addNamespaceDeclaration(IfmapStrings.STD_METADATA_PREFIX, IfmapStrings.STD_METADATA_NS_URI);
		try {
			SearchResult search;
			synchronized (mSsrc) {				
				search = mSsrc.search(searchRequest);
			}
			List<ResultItem> resultItems = search.getResultItems();
			for (ResultItem resultItem : resultItems) {
				Identifier[] identifiers = resultItem.getIdentifier();
				for (int i = 0; i < identifiers.length; i++) {
					if (identifiers[i] instanceof AccessRequest) {
						return identifiers[i];
					}
				}
			}
		} catch (IfmapErrorResult e) {
			logger.error("Got IfmapErrorResult: "+  e.getMessage() + ", " + e.getCause());
		} catch (IfmapException e) {
			logger.error("Got IfmapExecption: "+  e.getMessage() + ", " + e.getCause());
		}
		
		return null;
	}
	
	/**
	 * From: http://www.sha1-online.com/sha1-java/
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private String createSHA1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
         
        return sb.toString();
    }
}
