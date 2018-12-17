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

import static de.hshannover.f4.trust.irondetect.gui.ResultObjectType.ANOMALY;
import static de.hshannover.f4.trust.irondetect.gui.ResultObjectType.HINT;
import static de.hshannover.f4.trust.irondetect.gui.ResultObjectType.POLICY;
import static de.hshannover.f4.trust.irondetect.gui.ResultObjectType.RULE;
import static de.hshannover.f4.trust.irondetect.gui.ResultObjectType.SIGNATURE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identity;
import de.hshannover.f4.trust.ifmapj.identifier.IdentityType;
import de.hshannover.f4.trust.ifmapj.messages.MetadataLifetime;
import de.hshannover.f4.trust.ifmapj.messages.PollResult;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.hshannover.f4.trust.ifmapj.messages.PublishUpdate;
import de.hshannover.f4.trust.ifmapj.messages.Requests;
import de.hshannover.f4.trust.ifmapj.messages.ResultItem;
import de.hshannover.f4.trust.ifmapj.messages.SearchResult;
import de.hshannover.f4.trust.ironcommon.properties.Properties;
import de.hshannover.f4.trust.irondetect.Main;
import de.hshannover.f4.trust.irondetect.gui.ResultObject;
import de.hshannover.f4.trust.irondetect.gui.ResultObjectType;
import de.hshannover.f4.trust.irondetect.ifmap.EndpointPoller;
import de.hshannover.f4.trust.irondetect.model.Action;
import de.hshannover.f4.trust.irondetect.model.Anomaly;
import de.hshannover.f4.trust.irondetect.model.ConditionElement;
import de.hshannover.f4.trust.irondetect.model.FeatureExpression;
import de.hshannover.f4.trust.irondetect.model.Hint;
import de.hshannover.f4.trust.irondetect.model.HintExpression;
import de.hshannover.f4.trust.irondetect.model.Policy;
import de.hshannover.f4.trust.irondetect.model.Rule;
import de.hshannover.f4.trust.irondetect.model.Signature;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.handler.PolicyDataManager;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.metadata.PolicyMetadataFactory;
import de.hshannover.f4.trust.irondetect.util.BooleanOperator;
import de.hshannover.f4.trust.irondetect.util.Configuration;
import de.hshannover.f4.trust.irondetect.util.Pair;
import de.hshannover.f4.trust.irondetect.util.PollResultReceiver;
import de.hshannover.f4.trust.irondetect.util.event.Event;
import de.hshannover.f4.trust.irondetect.util.event.EventReceiver;
import de.hshannover.f4.trust.irondetect.util.event.EventType;
import de.hshannover.f4.trust.irondetect.util.event.ResultUpdateEvent;

/**
 * An {@link PolicyActionUpdater} publishes for a new (alert)ESUKOM-feature metadata a new policy-action metadata with
 * reference.
 *
 * @author Marcel Reichenbach
 */
public class PolicyActionUpdater implements Runnable, PollResultReceiver, EventReceiver {

	private static final Logger LOGGER = Logger.getLogger(PolicyActionUpdater.class);

	private Properties mConfig = Main.getConfig();

	private static final String ESUKOM_CATEGORY_IDENTIFIER = "32939:category";

	private static final String FEATURE_TYPE_NAME = "feature";

	private static final String XMLNS_FEATURE_URL_PREFIX = "xmlns:esukom";

	private static final String ESUKOM_URL = "http://www.esukom.de/2012/ifmap-metadata/1";

	private LinkedBlockingQueue<PollResult> mNewPollResults;

	private LinkedBlockingQueue<ResultUpdateEvent> mNewResultUpdateEvent;

	private PolicyActionSearcher mPolicyActionSearcher;

	private Policy mPolicy;

	private SSRC mSsrc;

	private static PolicyMetadataFactory mMetadataFactory;

	private boolean policyActionForNoFiredRules = false;

	public PolicyActionUpdater(Policy policy, SSRC ssrc) throws IfmapErrorResult, IfmapException {
		init(policy, ssrc, new PolicyActionSearcher(this, policy));

		EndpointPoller.getInstance().addPollResultReceiver(mPolicyActionSearcher);

		Thread actionSearcherThread = new Thread(mPolicyActionSearcher, PolicyActionSearcher.class.getSimpleName()
				+ "-Thread");

		actionSearcherThread.start();

	}

	protected void init(Policy policy, SSRC ssrc, PolicyActionSearcher policyActionSearcher) throws IfmapErrorResult,
	IfmapException {
		mSsrc = ssrc;
		mPolicy = policy;
		mPolicyActionSearcher = policyActionSearcher;
		mNewPollResults = new LinkedBlockingQueue<PollResult>();
		mNewResultUpdateEvent = new LinkedBlockingQueue<ResultUpdateEvent>();
		mMetadataFactory = new PolicyMetadataFactory();
		policyActionForNoFiredRules = mConfig.getBoolean(Configuration.KEY_PUBLISHER_POLICY_NOFIREDRULES, Configuration.DEFAULT_VALUE_PUBLISHER_POLICY_NOFIREDRULES);
	}

	protected void sendPublishUpdate(List<PublishUpdate> publishUpdates) throws IfmapErrorResult, IfmapException {
		PublishRequest req = Requests.createPublishReq();

		if (publishUpdates.size() > 0) {
			for (PublishUpdate updateElement : publishUpdates) {
				req.addPublishElement(updateElement);
			}
			mSsrc.publish(req);
			LOGGER.debug("sended publish request for " + publishUpdates.size() + " policy-rev-metadata");
		} else {
			LOGGER.trace("Nothig was sended. Wait for new poll results...");
		}
	}

	private PublishUpdate buildPublishUpdate(Identifier identifier, Document metadata) {
		PublishUpdate update = Requests.createPublishUpdate();

		update.setIdentifier1(identifier);
		update.addMetadata(metadata);
		update.setLifeTime(MetadataLifetime.session);

		return update;
	}

	private String findFeatureIdElement(Element root) {
		NodeList childs = root.getChildNodes();

		Node idNode = childs.item(0);
		if (idNode != null && "id".equals(idNode.getLocalName())) {
			Node idValueNode = idNode.getFirstChild();
			if (idValueNode != null && idValueNode.getNodeType() == Node.TEXT_NODE) {
				return idValueNode.getTextContent();
			} else {
				LOGGER.debug("First element of 'id' Node, is no TEXT_NODE");
				return ""; // TODO EXCEPTION
			}

		} else {
			LOGGER.debug("First element child item 0, is no 'id' Node");
			return ""; // TODO EXCEPTION
		}

	}

	public synchronized void sendPolicyAction(Document policyAction, String ruleId, String actionId)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IfmapErrorResult,
			IfmapException {
		List<PublishUpdate> publishUpdates = new ArrayList<PublishUpdate>();
		for (Rule rule : mPolicy.getRuleSet()) {
			if (rule.getId().equals(ruleId)) {
				for (Action action : rule.getActions()) {
					if (action.getId().equals(actionId)) {
						ExtendedIdentifier identfierAction = PolicyDataManager.transformPolicyData(action);
						publishUpdates.add(buildPublishUpdate(identfierAction, policyAction));
						break;
					}
				}
				break;
			}
		}

		sendPublishUpdate(publishUpdates);
	}

	public synchronized void sendPolicyAction(Document policyAction, String ruleId)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IfmapErrorResult,
			IfmapException {
		List<PublishUpdate> publishUpdates = new ArrayList<PublishUpdate>();
		for (Rule rule : mPolicy.getRuleSet()) {
			if (rule.getId().equals(ruleId)) {
				for (Action action : rule.getActions()) {
					ExtendedIdentifier identfierAction = PolicyDataManager.transformPolicyData(action);
					publishUpdates.add(buildPublishUpdate(identfierAction, policyAction));
				}
				break;
			}
		}

		sendPublishUpdate(publishUpdates);
	}

	/**
	 * Submit a new {@link PollResult} to this {@link PolicyActionUpdater}.
	 *
	 * @param pollResult A new {@link PollResult} to submit
	 */
	@Override
	public void submitNewPollResult(PollResult pollResult) {
		LOGGER.debug("new Poll-Result...");
		try {
			if (checkPollResultHasEsukomFeatures(pollResult)) {
				mNewPollResults.put(pollResult);
			} else {
				LOGGER.trace("poll-result has no EsukomFeatures");
			}
		} catch (InterruptedException e) {
			LOGGER.error("InterruptedException when submit new Poll-Result: " + e.getMessage());
		}
		LOGGER.trace("... new Poll-Result submitted");
	}

	private boolean checkPollResultHasEsukomFeatures(PollResult pollResult) {
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

				if (checkOfEsukomCategoryIdentity(identifier)) {
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

	protected void newPollResult() throws InterruptedException {
		LOGGER.debug("wait for new pollResult ...");
		PollResult pollResult = mNewPollResults.take();
		LOGGER.trace("... take() pollResult");
		ResultUpdateEvent updateEvent;
		Set<String> ruleFeatures = new HashSet<String>();
		Map<ResultObject, List<String>> signatureFeatureMap = new HashMap<ResultObject, List<String>>();
		Map<ResultObject, Map<ResultObject, List<String>>> anomalyMap = new HashMap<ResultObject, Map<ResultObject, List<String>>>();
		Map<ResultObject, List<String>> hintFeatureMap = new HashMap<ResultObject, List<String>>();
		do {
			LOGGER.debug("wait for new result-update-event ...");
			updateEvent = mNewResultUpdateEvent.take();
			LOGGER.trace("... take() mNewResultUpdateEvent");

			ResultObject result = updateEvent.getPayload();
			if (checkResultType(result, SIGNATURE)) {
				List<String> features = findFeatures(result);
				signatureFeatureMap.put(result, features);
				ruleFeatures.addAll(features);
			} else if (checkResultType(result, HINT)) {
				List<String> features = findFeatures(result);
				hintFeatureMap.put(result, features);
				ruleFeatures.addAll(features);
			} else if (checkResultType(result, ANOMALY)) {
				anomalyMap.put(result, new HashMap<ResultObject, List<String>>(hintFeatureMap));
				hintFeatureMap.clear();
			} else if (checkResultType(result, RULE)) {

				Map<Document, Identity> featureDocuments = getFeatureMetadata(
						pollResult, ruleFeatures, result.getDevice());


				if (result.getValue()) {
					// search feature-alerts while rule fires
					try {
						Document policyAction =
								mMetadataFactory.createPolicyActionMetadata(
										result, signatureFeatureMap, anomalyMap,
										featureDocuments);

						mPolicyActionSearcher.submitNewPolicyAction(
								new Pair<ResultObject, Document>(result,
										policyAction));
					} catch (DOMException | MarshalException e) {
						LOGGER.error(e.getClass().getSimpleName()
								+ " when create Policy-Action-Metadata");
					}

				} else if (policyActionForNoFiredRules) {
					// send without feature-alerts
					try {
						try {
							Document policyPartial = mMetadataFactory
									.createPolicyPartialMetadata(result,
											signatureFeatureMap, anomalyMap,
											featureDocuments);

							sendPolicyAction(policyPartial, result.getId());
						} catch (DOMException | MarshalException e) {
							LOGGER.error(e.getClass().getSimpleName()
									+ " when create Policy-Action-Metadata");
						}
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						LOGGER.error(e.getClass().getSimpleName() + " when send policy-action(Message= "
								+ e.getMessage() + ")");
					} catch (IfmapErrorResult e) {
						LOGGER.error(e.getClass().getSimpleName() + " when send policy-action ("
								+ e.toString() + ")");
					} catch (IfmapException e) {
						LOGGER.error(e.getClass().getSimpleName() + " when send policy-action (Message= "
								+ e.getMessage() + " |Description= " + e.getDescription() + ")");
					}
				}

				signatureFeatureMap.clear();
				anomalyMap.clear();
				hintFeatureMap.clear();
				ruleFeatures.clear();
			}

		} while (!checkPollResultIsFinished(updateEvent));
	}

	@Override
	public void run() {
		LOGGER.info("run() ...");

		while (!Thread.currentThread().isInterrupted()) {
			try {

				newPollResult();

			} catch (InterruptedException e) {
				LOGGER.error(e.getClass().getSimpleName() + " when take a new Poll-Result");
				break;
			}
		}

		LOGGER.info("... run()");
	}

	private Map<Document, Identity> getFeatureMetadata(PollResult pollResult, Set<String> ruleFeatures, String device) {
		Map<Document, Identity> featureDocuments = new HashMap<Document, Identity>();

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

				if (checkOfEsukomCategoryIdentity(identifier)) {
					Identity identity = (Identity) identifier;
					if (device.equals(identity.getAdministrativeDomain())) {
						for (Document metadata : resultItem.getMetadata()) {
							if (checkOfEsukomFeatureMetadata(metadata)) {
								String metadataId = findFeatureIdElement(metadata.getDocumentElement());
								if (ruleFeatures.contains(metadataId.toLowerCase())) {
									featureDocuments.put(metadata, identity);
								}
							}
						}
					}
				}
			}
		}

		return featureDocuments;
	}

	private List<String> findFeatures(ResultObject result) {
		String policyId = result.getId();
		ResultObjectType type = result.getType();
		if (checkResultType(result, SIGNATURE, HINT)) {
			return findFeatures(policyId, type);
		}
		return null;
	}

	private List<String> findFeatures(String policyElementId, ResultObjectType type) {
		switch (type) {
			case SIGNATURE:
				return findSignatureFeatures(policyElementId);
			case HINT:
				return findHintFeatures(policyElementId);
			default:
				throw new UnsupportedOperationException("findFeatures only for ResultObjectType SIGNATURE & HINT");
		}
	}

	private List<String> findSignatureFeatures(String policySignatureId) {
		List<String> foundFeatures = new ArrayList<String>();

		for (Rule r : mPolicy.getRuleSet()) {
			for (Pair<ConditionElement, BooleanOperator> p : r.getCondition().getConditionSet()) {
				ConditionElement conditionElement = p.getFirstElement();
				if (conditionElement instanceof Signature) {
					Signature signature = (Signature) conditionElement;
					if (signature.getId().equals(policySignatureId)) {
						for (Pair<FeatureExpression, BooleanOperator> featurePair : signature.getFeatureSet()) {
							String featureId = featurePair.getFirstElement().getFeatureId().toLowerCase();
							foundFeatures.add(featureId);
						}
						LOGGER.trace("found " + foundFeatures.size() + " features for signature-id "
								+ signature.getId());
						return foundFeatures;
					}
				}
			}
		}
		LOGGER.debug("trace " + foundFeatures.size() + " features for signature-id " + policySignatureId);
		return foundFeatures;
	}

	private List<String> findHintFeatures(String policyHintId) {
		List<String> foundFeatures = new ArrayList<String>();

		for (Rule r : mPolicy.getRuleSet()) {
			for (Pair<ConditionElement, BooleanOperator> p : r.getCondition().getConditionSet()) {
				ConditionElement conditionElement = p.getFirstElement();
				if (conditionElement instanceof Anomaly) {
					Anomaly anomaly = (Anomaly) conditionElement;
					for (Pair<HintExpression, BooleanOperator> ex : anomaly.getHintSet()) {
						Hint hint = ex.getFirstElement().getHintValuePair().getFirstElement();
						if (hint.getId().equals(policyHintId)) {
							for (String featureId : hint.getFeatureIds()) {
								foundFeatures.add(featureId.toLowerCase());
							}
							LOGGER.trace("found " + foundFeatures.size() + " features for hint-id " + hint.getId());
							return foundFeatures;
						}
					}
				}
			}
		}
		LOGGER.trace("found " + foundFeatures.size() + " features for hint-id " + policyHintId);
		return foundFeatures;
	}

	@SuppressWarnings("unused")
	private List<Pair<String, String>> findActionFeatures(String policyRuleId) {
		List<Pair<String, String>> foundFeatures = new ArrayList<Pair<String, String>>();

		for (Rule r : mPolicy.getRuleSet()) {
			if (policyRuleId.equals(r.getId())) {
				for (Action action : r.getActions()) {
					foundFeatures.addAll(action.getKeyValuePairs());
				}
				break;
			}
		}
		LOGGER.trace("found " + foundFeatures.size() + " features for hint-id " + policyRuleId);
		return foundFeatures;
	}

	private boolean checkPollResultIsFinished(ResultUpdateEvent updateEvent){
		ResultObject result = updateEvent.getPayload();
		if (!checkResultType(result, POLICY)) {
			return false;
		}
		return result.getValue();
	}

	private boolean checkResultType(ResultObject result, ResultObjectType... type) {
		if (Arrays.asList(type).contains(result.getType())) {
			return true;
		}
		return false;
	}

	@Override
	public void submitNewEvent(Event event) {
		LOGGER.debug("new Event...");
		if (event.getType() == EventType.RESULT_UPDATE) {
			ResultObject result = ((ResultUpdateEvent) event).getPayload();

			if (checkResultType(result, POLICY, RULE, SIGNATURE, ANOMALY, HINT)) {
				try {
					mNewResultUpdateEvent.put((ResultUpdateEvent) event);
				} catch (InterruptedException e) {
					LOGGER.error("InterruptedException when submit new result-update: " + e.getMessage());
				}
			}
		}
	}

	public void submitChangedPolicy(Policy newPolicy) {
		mPolicy = newPolicy;
		mPolicyActionSearcher.submitChangedPolicy(newPolicy);
	}
}
