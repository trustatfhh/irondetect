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
 * This file is part of irondetect, version 0.0.9,
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2016 Trust@HsH
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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.exception.InitializationException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.messages.MetadataLifetime;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.hshannover.f4.trust.ifmapj.messages.PublishUpdate;
import de.hshannover.f4.trust.ifmapj.messages.Requests;
import de.hshannover.f4.trust.ifmapj.messages.SearchRequest;
import de.hshannover.f4.trust.ifmapj.messages.SearchResult;
import de.hshannover.f4.trust.ifmapj.messages.SubscribeUpdate;
import de.hshannover.f4.trust.ironcommon.properties.Properties;
import de.hshannover.f4.trust.irondetect.Main;
import de.hshannover.f4.trust.irondetect.gui.ResultLoggerImpl;
import de.hshannover.f4.trust.irondetect.ifmap.EndpointPoller;
import de.hshannover.f4.trust.irondetect.ifmap.IfmapUtil;
import de.hshannover.f4.trust.irondetect.livechecker.policy.publisher.LiveCheckerPolicyEvaluationUpdater;
import de.hshannover.f4.trust.irondetect.model.Action;
import de.hshannover.f4.trust.irondetect.model.Anomaly;
import de.hshannover.f4.trust.irondetect.model.ConditionElement;
import de.hshannover.f4.trust.irondetect.model.Hint;
import de.hshannover.f4.trust.irondetect.model.HintExpression;
import de.hshannover.f4.trust.irondetect.model.Policy;
import de.hshannover.f4.trust.irondetect.model.Rule;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.handler.PolicyDataManager;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.ActionHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.AnomalyHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.ConditionHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.HintHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.PolicyHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.RuleHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.SignatureHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.metadata.PolicyMetadataFactory;
import de.hshannover.f4.trust.irondetect.util.BooleanOperator;
import de.hshannover.f4.trust.irondetect.util.Configuration;
import de.hshannover.f4.trust.irondetect.util.Constants;
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * An {@link PolicyPublisher} publishes all irondetect policies.
 *
 * @author Marcel Reichenbach
 */
public class PolicyPublisher {

	private static final Logger LOGGER = Logger.getLogger(PolicyPublisher.class);

	private Properties mConfig = Main.getConfig();

	public static final String SUBSCRIPTION_NAME_POLICY_RELOAD = "AutoPolicyReload";

	protected Policy mPolicy;

	private SSRC mSsrc;

	private PolicyFeatureUpdater mPolicyFeatureUpdater;

	private PolicyActionUpdater mPolicyActionUpdater;

	private LiveCheckerPolicyEvaluationUpdater mLiveCheckerPolicyEvaluationUpdater;

	protected List<PublishUpdate> mPublishUpdates;

	private PolicyMetadataFactory mMetadataFactory;

	private String policyPublisherIdentifier;

	private Thread mPolicyPollerThread;

	private Integer ifmapMaxResultSize;

	// register all extended identifier handler to ifmapJ
	static {
		Identifiers.registerIdentifierHandler(new PolicyHandler());
		Identifiers.registerIdentifierHandler(new RuleHandler());
		Identifiers.registerIdentifierHandler(new ConditionHandler());
		Identifiers.registerIdentifierHandler(new ActionHandler());
		Identifiers.registerIdentifierHandler(new SignatureHandler());
		Identifiers.registerIdentifierHandler(new AnomalyHandler());
		Identifiers.registerIdentifierHandler(new HintHandler());
	}

	public PolicyPublisher(Policy policy) throws IfmapErrorResult, IfmapException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		mPolicy = policy;

		if (mConfig.getBoolean(Configuration.KEY_SELF_PUBLISH_ENABLED,
				Configuration.DEFAULT_VALUE_SELF_PUBLISH_ENABLED)) {
			policyPublisherIdentifier = mConfig.getString(Configuration.KEY_SELF_PUBLISH_DEVICE,
					Configuration.DEFAULT_VALUE_SELF_PUBLISH_DEVICE);
		} else {
			policyPublisherIdentifier = mConfig.getString(Configuration.KEY_PUBLISHER_POLICY_DEVICENAME,
					Configuration.DEFAULT_VALUE_PUBLISHER_POLICY_DEVICENAME);
		}

		ifmapMaxResultSize =
				mConfig.getInt(Configuration.KEY_IFMAP_MAXRESULTSIZE, Configuration.DEFAULT_VALUE_IFMAP_MAXRESULTSIZE);

		String username = mConfig.getString(Configuration.KEY_IFMAP_BASIC_POLICYPUBLISHER_USERNAME,
				Configuration.DEFAULT_VALUE_IFMAP_BASIC_POLICYPUBLISHER_USERNAME);
		String password = mConfig.getString(Configuration.KEY_IFMAP_BASIC_POLICYPUBLISHER_PASSWORD,
				Configuration.DEFAULT_VALUE_IFMAP_BASIC_POLICYPUBLISHER_PASSWORD);

		try {
			mSsrc = IfmapUtil.initSsrc(username, password);
		} catch (FileNotFoundException e) {
			LOGGER.error("Could not initialize truststore: "
					+ e.getMessage());
			System.exit(Constants.RETURN_CODE_ERROR_TRUSTSTORE_LOADING_FAILED);
		} catch (InitializationException e) {
			LOGGER.error("Could not initialize ifmapj: "
					+ e.getMessage() + ", " + e.getCause());
			System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_INITIALIZATION_FAILED);
		}

		initSession(ifmapMaxResultSize);

		buildPublishUpdate();
		sendPublishUpdate();

		mPolicyFeatureUpdater = new PolicyFeatureUpdater(mPolicy, mSsrc);
		mPolicyActionUpdater = new PolicyActionUpdater(mPolicy, mSsrc);
		mLiveCheckerPolicyEvaluationUpdater = LiveCheckerPolicyEvaluationUpdater.getInstance(mPolicy, mSsrc);

		ResultLoggerImpl.getInstance().addEventReceiver(mPolicyActionUpdater);
		ResultLoggerImpl.getInstance().addEventReceiver(mLiveCheckerPolicyEvaluationUpdater);
		EndpointPoller.getInstance().addPollResultReceiver(mPolicyFeatureUpdater);
		EndpointPoller.getInstance().addPollResultReceiver(mPolicyActionUpdater);

		Thread featureThread = new Thread(mPolicyFeatureUpdater, PolicyFeatureUpdater.class.getSimpleName()
				+ "-Thread");
		Thread actionThread = new Thread(mPolicyActionUpdater, PolicyActionUpdater.class.getSimpleName()
				+ "-Thread");
		Thread liveCheckeractionThread =
				new Thread(mLiveCheckerPolicyEvaluationUpdater, LiveCheckerPolicyEvaluationUpdater.class.getSimpleName()
						+ "-Thread");

		featureThread.start();
		actionThread.start();
		liveCheckeractionThread.start();
	}

	private void sendPublishUpdate() throws IfmapErrorResult, IfmapException {
		PublishRequest req = Requests.createPublishReq();

		for (PublishUpdate updateElement : mPublishUpdates) {
			req.addPublishElement(updateElement);
		}

		mSsrc.publish(req);
	}

	protected void addPublishUpdate(Identifier identifier1, Document metadata, Identifier identifier2) {
		PublishUpdate update = Requests.createPublishUpdate();

		update.setIdentifier1(identifier1);
		update.setIdentifier2(identifier2);
		update.addMetadata(metadata);
		update.setLifeTime(MetadataLifetime.session);

		mPublishUpdates.add(update);
	}

	protected void buildPublishUpdate() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		mPublishUpdates = new ArrayList<PublishUpdate>();
		mMetadataFactory = new PolicyMetadataFactory();

		ExtendedIdentifier policyIdentifier = PolicyDataManager.transformPolicyData(mPolicy);
		Document hasElementMetadata = mMetadataFactory.createHasElement();

		Identifier policies = Identifiers.createDev(policyPublisherIdentifier);
		addPublishUpdate(policies, hasElementMetadata, policyIdentifier);

		for (Rule r : mPolicy.getRuleSet()) {
			ExtendedIdentifier identfierRule = PolicyDataManager.transformPolicyData(r);
			addPublishUpdate(policyIdentifier, hasElementMetadata, identfierRule);

			ExtendedIdentifier identfierCondition = PolicyDataManager.transformPolicyData(r.getCondition());
			addPublishUpdate(identfierRule, hasElementMetadata, identfierCondition);

			for (Action a : r.getActions()) {
				ExtendedIdentifier identfierAction = PolicyDataManager.transformPolicyData(a);
				addPublishUpdate(identfierRule, hasElementMetadata, identfierAction);
			}

			for (Pair<ConditionElement, BooleanOperator> p : r.getCondition().getConditionSet()) {
				ConditionElement conditionElement = p.getFirstElement();
				ExtendedIdentifier identfierConditionElement = PolicyDataManager.transformPolicyData(conditionElement);
				addPublishUpdate(identfierCondition, hasElementMetadata, identfierConditionElement);

				if (conditionElement instanceof Anomaly) {
					Anomaly anomaly = (Anomaly) conditionElement;
					for (Pair<HintExpression, BooleanOperator> ex : anomaly.getHintSet()) {
						Hint hint = ex.getFirstElement().getHintValuePair().getFirstElement();
						ExtendedIdentifier identfierHint = PolicyDataManager.transformPolicyData(hint);
						addPublishUpdate(identfierConditionElement, hasElementMetadata, identfierHint);
					}
				}
			}
		}
	}

	private void initSession(int maxPollResultSize) throws IfmapErrorResult, IfmapException {
		LOGGER.trace("creating new SSRC session ...");

		mSsrc.newSession(maxPollResultSize);

		LOGGER.debug("new SSRC session OK");
	}

	public void disconnect() throws IfmapErrorResult, IfmapException {
		try {

			LOGGER.trace("endSession() ...");
			mSsrc.endSession();
			LOGGER.debug("endSession() OK");

			LOGGER.trace("closeTcpConnection() ...");
			mSsrc.closeTcpConnection();
			LOGGER.debug("closeTcpConnection() OK");

		} finally {
			resetConnection();
		}

	}

	private void resetConnection() {
		LOGGER.trace("resetConnection() ...");

		mSsrc = null;

		LOGGER.trace("... resetConnection() OK");
	}

	public SearchResult searchPolicyGraph() {
		Identifier startIdentifier = Identifiers.createDev(policyPublisherIdentifier);

		SearchRequest searchRequest =
				Requests.createSearchReq(null, 10, null, this.ifmapMaxResultSize, null, startIdentifier);

		try {
			SearchResult search;
			synchronized (mSsrc) {
				search = mSsrc.search(searchRequest);
			}

			return search;

		} catch (IfmapErrorResult e) {
			LOGGER.error("Got IfmapErrorResult: "
					+ e.getMessage() + ", " + e.getCause());
		} catch (IfmapException e) {
			LOGGER.error("Got IfmapExecption: "
					+ e.getMessage() + ", " + e.getCause());
		}

		return null;
	}

	public void startPolicyAutomaticReload() throws IfmapErrorResult, IfmapException {
		if (mPolicyPollerThread == null) {
			startPolicyPoller();
			subscribeForAutoPolicyReload();
		}
	}

	private void startPolicyPoller() throws InitializationException {
		PolicyPoller.getInstance().setArc(mSsrc.getArc());

		mPolicyPollerThread = new Thread(PolicyPoller.getInstance(), PolicyPoller.class.getSimpleName());
		mPolicyPollerThread.start();
	}

	private void subscribeForAutoPolicyReload() throws IfmapErrorResult, IfmapException {
		LOGGER.debug("Subscribe for automatic policy reload.");

		Identifier startIdentifier = Identifiers.createDev(policyPublisherIdentifier);

		SubscribeUpdate subscribeUpdate = Requests.createSubscribeUpdate();

		subscribeUpdate.setName(SUBSCRIPTION_NAME_POLICY_RELOAD);
		subscribeUpdate.setStartIdentifier(startIdentifier);
		subscribeUpdate.setMaxDepth(1000);

		synchronized (mSsrc) {
			mSsrc.subscribe(Requests.createSubscribeReq(subscribeUpdate));
		}

		LOGGER.debug("Subscription done!");
	}

	public void submitChangedPolicy(Policy newPolicy) {
		mPolicy = newPolicy;
		mPolicyFeatureUpdater.submitChangedPolicy(newPolicy);
		mPolicyActionUpdater.submitChangedPolicy(newPolicy);
		mLiveCheckerPolicyEvaluationUpdater.submitChangedPolicy(newPolicy);
	}

}
