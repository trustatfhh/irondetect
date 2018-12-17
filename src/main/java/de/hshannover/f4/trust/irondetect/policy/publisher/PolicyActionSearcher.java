/*
 * #%L
 * =====================================================
 *   _____                _     ____  _   _       _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
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
 * Website: http://trust.f4.hs-hannover.de/
 * 
 * This file is part of irondetect, version 0.0.10, 
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2018 Trust@HsH
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
package de.hshannover.f4.trust.irondetect.policy.publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.Identity;
import de.hshannover.f4.trust.ifmapj.identifier.IdentityType;
import de.hshannover.f4.trust.ifmapj.log.IfmapJLog;
import de.hshannover.f4.trust.ifmapj.messages.PollResult;
import de.hshannover.f4.trust.ifmapj.messages.ResultItem;
import de.hshannover.f4.trust.ifmapj.messages.SearchResult;
import de.hshannover.f4.trust.irondetect.gui.ResultObject;
import de.hshannover.f4.trust.irondetect.model.Action;
import de.hshannover.f4.trust.irondetect.model.Policy;
import de.hshannover.f4.trust.irondetect.model.Rule;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;
import de.hshannover.f4.trust.irondetect.util.Constants;
import de.hshannover.f4.trust.irondetect.util.Pair;
import de.hshannover.f4.trust.irondetect.util.PollResultReceiver;

/**
 * An {@link PolicyActionSearcher} searches for a new (alert)ESUKOM-feature metadata and add this iin policy-action
 * metadata.
 *
 * @author Marcel Reichenbach
 */
public class PolicyActionSearcher implements Runnable, PollResultReceiver {

	private static final Logger LOGGER = Logger.getLogger(PolicyActionSearcher.class);

	private static final String ESUKOM_CATEGORY_IDENTIFIER = "32939:category";

	private static final String FEATURE_TYPE_NAME = "feature";

	private static final String XMLNS_FEATURE_URL_PREFIX = "xmlns:esukom";

	private static final String ESUKOM_URL = "http://www.esukom.de/2012/ifmap-metadata/1";

	private LinkedBlockingQueue<Pair<ResultObject, Document>> mNewPolicyAction = new LinkedBlockingQueue<Pair<ResultObject, Document>>();

	private PolicyActionUpdater mPolicyActionUpdater;

	private Policy mPolicy;

	private Map<String, Integer> mAlertInstanceNumber;

	private Map<Identity, List<Document>> mAlertResults;

	private DocumentBuilder mDocumentBuilder;

	private Thread mPolicyActionSearcherThread;

	public PolicyActionSearcher(PolicyActionUpdater policyActionUpdater, Policy policy) throws IfmapErrorResult,
	IfmapException {
		mPolicyActionUpdater = policyActionUpdater;
		mPolicy = policy;
		mAlertInstanceNumber = new HashMap<String, Integer>();
		mAlertResults = new HashMap<Identity, List<Document>>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			mDocumentBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IfmapJLog.error("Could not get DocumentBuilder instance [" + e.getMessage() + "]");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Submit a new {@link PollResult} to this {@link PolicyActionSearcher}.
	 *
	 * @param pollResult A new {@link PollResult} to submit
	 */
	@Override
	public void submitNewPollResult(PollResult pollResult) {
		LOGGER.debug("new Poll-Result...");
		if (checkPollResultHasEsukomAlertFeatures(pollResult)) {
			Map<Identity, List<Document>> alertResults = preparePollResult(pollResult);
			synchronized (mAlertResults) {
				mAlertResults.putAll(alertResults);
			}
			if (mPolicyActionSearcherThread != null) {
				synchronized (mPolicyActionSearcherThread) {
					LOGGER.debug("notify() ... new PollResult");
					mPolicyActionSearcherThread.notify();
				}
			}
		} else {
			LOGGER.trace("poll-result has no EsukomFeatures");
		}
		LOGGER.trace("... new Poll-Result submitted");
	}

	private Map<Identity, List<Document>> preparePollResult(PollResult pollResult) {
		Map<Identity, List<Document>> alertResults = new HashMap<Identity, List<Document>>();

		for (SearchResult searchResult : pollResult.getResults()) {
			for (ResultItem resultItem : searchResult.getResultItems()) {
				Identifier identifier1 = resultItem.getIdentifier1();
				Identifier identifier2 = resultItem.getIdentifier2();
				Identifier identifier;

				// A feature can not stand between two identifier. One of them must be null.
				if (!(identifier1 != null && identifier2 == null)) {
					if (!(identifier1 == null && identifier2 != null)) {
						LOGGER.trace("A feature can not stand between two identifier. One of them must be null. Next result item ...");
						continue;
					} else {
						identifier = resultItem.getIdentifier2();
						LOGGER.trace("One of the two identifiers is null(Identifier1), greate");
					}
				} else {
					identifier = resultItem.getIdentifier1();
					LOGGER.trace("One of the two identifiers is null(Identifier2), greate");
				}

				if (checkOfEsukomAlertIdentity(identifier)) {
					Identity identity = (Identity) identifier;
					if (!alertResults.containsKey(identity)) {
						alertResults.put(identity, new ArrayList<Document>());
					}
					for (Document metadata : resultItem.getMetadata()) {
						if (checkOfEsukomFeatureMetadata(metadata)) {
							alertResults.get(identity).add(metadata);
						}
					}
				}
			}
		}

		return alertResults;
	}

	private boolean checkPollResultHasEsukomAlertFeatures(PollResult pollResult) {
		for (SearchResult searchResult : pollResult.getResults()) {
			for (ResultItem resultItem : searchResult.getResultItems()) {
				Identifier identifier1 = resultItem.getIdentifier1();
				Identifier identifier2 = resultItem.getIdentifier2();
				Identifier identifier;

				// A feature can not stand between two identifier. One of them must be null.
				if (!(identifier1 != null && identifier2 == null)) {
					if (!(identifier1 == null && identifier2 != null)) {
						LOGGER.trace("A feature can not stand between two identifier. One of them must be null. Next result item ...");
						continue;
					} else {
						identifier = resultItem.getIdentifier2();
						LOGGER.trace("One of the two identifiers is null(Identifier1), greate");
					}
				} else {
					identifier = resultItem.getIdentifier1();
					LOGGER.trace("One of the two identifiers is null(Identifier2), greate");
				}

				if (checkOfEsukomAlertIdentity(identifier)) {
					for (Document metadata : resultItem.getMetadata()) {
						if (checkOfEsukomFeatureMetadata(metadata)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean checkIdentityIdentifier(Identifier identifier){
		if (identifier instanceof Identity) {
			return true;
		} else {
			return false;
		}
	}

	private boolean checkIdentityType(Identity identity) {
		if (identity.getType() == IdentityType.other) {
			return true;
		} else {
			return false;
		}
	}

	private boolean checkIdentityOtherTypeDefinition(Identity identity) {
		if (identity.getOtherTypeDefinition().equals(ESUKOM_CATEGORY_IDENTIFIER)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean checkOfEsukomCategoryIdentity(Identifier identifier) {
		LOGGER.trace("check of esukom category identity");

		if (!checkIdentityIdentifier(identifier)) {
			LOGGER.trace("identifier is not a identity");
			return false;
		}

		Identity identity = (Identity) identifier;

		if (!checkIdentityType(identity)) {
			LOGGER.trace("identity type is no " + IdentityType.other);
			return false;
		}

		if (!checkIdentityOtherTypeDefinition(identity)) {
			LOGGER.trace("identity have a wrong esukom category");
			return false;
		}

		return true;
	}

	private boolean checkOfEsukomAlertIdentity(Identifier identifier) {

		if (!checkOfEsukomCategoryIdentity(identifier)) {
			return false;
		}

		Identity identity = (Identity) identifier;

		if (!checkIdentityName(identity)) {
			return false;
		}
		return true;
	}

	private boolean checkIdentityName(Identity identity) {
		if (!identity.getName().toLowerCase().startsWith((Constants.ALERT_IDENTIFIER_NAME))) {
			return false;
		}
		return true;
	}

	private boolean checkOfEsukomFeatureMetadata(Document document) {
		LOGGER.trace("check of esukom feature metadata");
		String typename = document.getDocumentElement().getLocalName();
		String url = document.getDocumentElement().getAttribute(XMLNS_FEATURE_URL_PREFIX);

		if (!FEATURE_TYPE_NAME.equals(typename)) {
			LOGGER.trace("is not a feature metadata");
			return false;
		}

		if (!ESUKOM_URL.equals(url)) {
			LOGGER.trace("wrong esukom feature metadata url");
			return false;
		}

		return true;
	}

	private Document clone(Document document) {
		Document documentClone = mDocumentBuilder.newDocument();

		Node rootNodeClone = document.getDocumentElement().cloneNode(true);
		documentClone.adoptNode(rootNodeClone);
		documentClone.appendChild(rootNodeClone);

		return documentClone;
	}

	@Override
	public void run() {
		LOGGER.info("run() ...");
		mPolicyActionSearcherThread = Thread.currentThread();
		while (!Thread.currentThread().isInterrupted()) {
			try {

				LOGGER.debug("wait for new policy-action metadata ...");
				Pair<ResultObject, Document> policyAction = mNewPolicyAction.take();
				LOGGER.trace("... take() new policy-action metadata");

				ResultObject ruleResult = policyAction.getFirstElement();
				Document policyActionMetadata = policyAction.getSecondElement();
				String device = ruleResult.getDevice();

				List<Action> actions = getActions(ruleResult.getId());

				for (Action action : actions) {
					Document policyActionMetadataCopy = clone(policyActionMetadata);
					int alertInstanceNumber = 0;
					if (mAlertInstanceNumber.containsKey(device)) {
						alertInstanceNumber = mAlertInstanceNumber.get(device);
					}

					Identity identity = Identifiers.createIdentity(IdentityType.other, Constants.ALERT_IDENTIFIER_NAME
							+ ":" + alertInstanceNumber, device, Constants.OTHER_TYPE_DEFINITION);

					List<Document> alertFeatures = mAlertResults.get(identity);

					if (alertFeatures == null) {
						do {
							synchronized (Thread.currentThread()) {
								LOGGER.debug("wait() for new PollResult...");
								Thread.currentThread().wait();
								LOGGER.trace("... new PollResult");
							}
							alertFeatures = mAlertResults.get(identity);
						} while (alertFeatures == null);
					}

					addActionFeatures(policyActionMetadataCopy, alertFeatures, identity);

					mPolicyActionUpdater.sendPolicyAction(policyActionMetadataCopy, ruleResult.getId(), action.getId());

					alertInstanceNumber++;
					mAlertInstanceNumber.put(device, alertInstanceNumber);

					synchronized (mAlertResults) {
						mAlertResults.remove(identity);
					}

				}
			} catch (InterruptedException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				LOGGER.fatal(e.getClass().getSimpleName() + " when take a new Poll-Result");
				break;
			} catch (IfmapErrorResult e) {
				LOGGER.error(e.getClass().getSimpleName() + " when send policy-action (" + e.toString() + ")");
			} catch (IfmapException e) {
				LOGGER.error(e.getClass().getSimpleName() + " when send policy-action (Message= " + e.getMessage()
				+ " |Description= " + e.getDescription() + ")");
			}
		}

		LOGGER.info("... run()");
	}

	private void addActionFeatures(Document policyActionMetadata, List<Document> alertFeatures, Identity identity)
			throws MarshalException {
		Element rootElement = policyActionMetadata.getDocumentElement();

		for (Document feature : alertFeatures) {
			Element revMetadataElement = policyActionMetadata.createElementNS(null, PolicyStrings.ACTION_EL_NAME);

			// build new metadata element
			// Create a duplicate node and transfer ownership of the new node into the destination document
			Node revMetadataRootElementClone = feature.getDocumentElement().cloneNode(true);
			policyActionMetadata.adoptNode(revMetadataRootElementClone);
			// Place the node in the new document
			revMetadataElement.appendChild(revMetadataRootElementClone);

			// build new Identity element
			Element identifierElement = Identifiers.toElement(identity, policyActionMetadata);
			revMetadataElement.appendChild(identifierElement);

			rootElement.appendChild(revMetadataElement);
		}
	}

	private List<Action> getActions(String policyRuleId) {
		for (Rule rule : mPolicy.getRuleSet()) {
			if (rule.getId().equalsIgnoreCase(policyRuleId)) {
				return rule.getActions();
			}
		}
		return null;
	}

	public void submitNewPolicyAction(Pair<ResultObject, Document> policyAction) {
		LOGGER.debug("new NewPolicyAction...");
		try {
			mNewPolicyAction.put(policyAction);
		} catch (InterruptedException e) {
			LOGGER.error("InterruptedException when submit new result-update: " + e.getMessage());
		}
	}

	public void submitChangedPolicy(Policy newPolicy) {
		mPolicy = newPolicy;
	}

}
