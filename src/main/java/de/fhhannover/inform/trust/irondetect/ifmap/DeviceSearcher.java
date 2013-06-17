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
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import de.fhhannover.inform.trust.ifmapj.IfmapJ;
import de.fhhannover.inform.trust.ifmapj.IfmapJHelper;
import de.fhhannover.inform.trust.ifmapj.binding.IfmapStrings;
import de.fhhannover.inform.trust.ifmapj.channel.ARC;
import de.fhhannover.inform.trust.ifmapj.channel.SSRC;
import de.fhhannover.inform.trust.ifmapj.exception.EndSessionException;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapErrorResult;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapException;
import de.fhhannover.inform.trust.ifmapj.exception.InitializationException;
import de.fhhannover.inform.trust.ifmapj.identifier.Device;
import de.fhhannover.inform.trust.ifmapj.identifier.Identifiers;
import de.fhhannover.inform.trust.ifmapj.messages.PollResult;
import de.fhhannover.inform.trust.ifmapj.messages.Requests;
import de.fhhannover.inform.trust.ifmapj.messages.ResultItem;
import de.fhhannover.inform.trust.ifmapj.messages.SearchResult;
import de.fhhannover.inform.trust.ifmapj.messages.SubscribeUpdate;
import de.fhhannover.inform.trust.irondetect.util.Configuration;
import de.fhhannover.inform.trust.irondetect.util.Constants;
import de.fhhannover.inform.trust.irondetect.util.Helper;

/**
 * 
 * @author Ralf Steuerwald
 *
 */
public class DeviceSearcher implements Runnable {

	private static final Logger logger = Logger.getLogger(DeviceSearcher.class);
	
	private IfmapController mController;
	
	/**
	 * The only SSRC
	 */
	private SSRC mSsrc;

	/**
	 * The only ARC
	 */
	private ARC mArc;
	
	public DeviceSearcher(IfmapController controller) {
		mController = controller;
		
		try {
			initSsrc();
			initArc();
		} catch (FileNotFoundException e) {
			logger.error("Could not initialize truststore: " + e.getMessage());
			System.exit(Constants.RETURN_CODE_ERROR_TRUSTSTORE_LOADING_FAILED);
		} catch (InitializationException e) {
			logger.error("Initialization of SSRC and ARC failed: " + e.getMessage() + ", " + e.getCause());
			System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_INITIALIZATION_FAILED);
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
				logger.info("Creating SSRC using basic authentication.");
				this.mSsrc = IfmapJ.createSSRC(Configuration.ifmapUrlBasic(), Configuration.irondetectPdpSubscriberUser(),
						Configuration.irondetectPdpSubscriberPassword(), tms);
			}
			else if (authMethod.equalsIgnoreCase("cert")) {
				logger.info("Creating SSRC using certificate-based authentication.");
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
	
	/**
	 * @throws InitializationException
	 */
	private void initArc() throws InitializationException {
		mArc = mSsrc.getArc();
	}
	
	@Override
	public void run() {
		initSession();
		
		try {
			subscribeForPdps();
		} catch (IfmapErrorResult e1) {
			logger.error("Got IfmapError: " + e1.getMessage() + ", " + e1.getCause());
		} catch (IfmapException e1) {
			logger.error("Error at polling the MAP server: " + e1.getMessage() + ", " + e1.getCause());
			System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_EXCEPTION);
		}
		
		while (!Thread.currentThread().isInterrupted()) {
			logger.debug("polling ...");
			
			try {
				PollResult pollResult = mArc.poll();
				
				List<Device> devices = filterForEnpointUpdates(pollResult.getResults()); // TODO filterForEndpointDeletes?
				
				if (devices.size() > 0) {
					for (Device d : devices) {
						logger.debug("submitting new device " + devices + "...");
						mController.submitNewDevice(d);
					}
				}
				
			} catch (IfmapErrorResult e) {
				logger.error("Got IfmapError: " + e.getMessage() + ", " + e.getCause());
			} catch (EndSessionException e) {
				logger.error("The session with the MAP server was closed: " + e.getMessage() + ", " + e.getCause());
				System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_EXCEPTION);
			} catch (IfmapException e) {
				logger.error("Error at polling the MAP server: " + e.getMessage() + ", " + e.getCause());
				System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_EXCEPTION);
			}
		}
	}
	
	/**
	 * Filters the list of {@link SearchResult} for {@link Device} identifier.
	 * The resulting list contains only devices updates and search results,
	 * no delete or notify updates.
	 * 
	 * @param results
	 * @return
	 */
	public List<Device> filterForEnpointUpdates(List<SearchResult> results) {
		List<Device> endpoints = new ArrayList<Device>();
		
		for (SearchResult sr : results) {
			
			// process updates/searchs only
			if ( (sr.getType().equals(SearchResult.Type.updateResult)) ||
				 (sr.getType().equals(SearchResult.Type.searchResult)) ) {
				for (ResultItem item : sr.getResultItems()) {
					for (Document doc : item.getMetadata()) {
						
						// search for access-request-device link
						if (doc.getDocumentElement().getLocalName().equals("access-request-device")) {
							
							// extract the device identifier
							if (item.getIdentifier1() instanceof Device) {
								Device dev = (Device) item.getIdentifier1();
								endpoints.add(dev);
							}
							else {
								Device dev = (Device) item.getIdentifier2();
								endpoints.add(dev);
							}
						}
					}
				}
			}
		}
		return endpoints;
	}
	
	private void subscribeForPdps() throws IfmapErrorResult, IfmapException {
		SubscribeUpdate subscribeUpdate = Requests.createSubscribeUpdate();

		// TODO do the following for each PDP

		// set subscription parameters for pdp
		subscribeUpdate.setName(Configuration.subscriberPdp());
		subscribeUpdate.setStartIdentifier(Identifiers.createDev(Configuration
				.subscriberPdp()));
		subscribeUpdate.setMatchLinksFilter(Constants.MATCH_LINKS_PDP);
		subscribeUpdate.setMaxDepth(2);
		subscribeUpdate.setMaxSize(16384);
		subscribeUpdate.setResultFilter(Constants.RESULT_FILTER_PDP);

		// add default namespaces
		subscribeUpdate.addNamespaceDeclaration(IfmapStrings.BASE_PREFIX,
				IfmapStrings.BASE_NS_URI);
		subscribeUpdate.addNamespaceDeclaration(
				IfmapStrings.STD_METADATA_PREFIX,
				IfmapStrings.STD_METADATA_NS_URI);

		logger.debug("Subscribe for new PDP device ...");
		mSsrc.subscribe(Requests.createSubscribeReq(subscribeUpdate));
		logger.debug("Subscription done!");
	}
	
	private void initSession() {
		try {
			mSsrc.newSession(Configuration.ifmapMaxResultSize());
		} catch (IfmapErrorResult e) {
			logger.error("Got IfmapErrorResult: " + e.getMessage() + ", " + e.getCause());
		} catch (IfmapException e) {
			logger.error("Got IfmapException: " + e.getMessage() + ", " + e.getCause());
		}
		
		logger.debug("Session initialized: Session ID: " + mSsrc.getSessionId()
				+ " - Publisher ID : " + mSsrc.getPublisherId() + " (with MAX_POLL_RESULT_SIZE = " + Configuration.ifmapMaxResultSize() + ")");
	}
}
