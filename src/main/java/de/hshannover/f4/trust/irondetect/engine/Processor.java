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
 * This file is part of irondetect, version 0.0.8,
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2015 Trust@HsH
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
/**
 *
 */
package de.hshannover.f4.trust.irondetect.engine;



import static de.hshannover.f4.trust.irondetect.gui.ResultObjectType.POLICY;

import java.io.FileNotFoundException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.messages.PollResult;
import de.hshannover.f4.trust.ifmapj.messages.ResultItem;
import de.hshannover.f4.trust.ifmapj.messages.SearchResult;
import de.hshannover.f4.trust.ironcommon.properties.Properties;
import de.hshannover.f4.trust.irondetect.Main;
import de.hshannover.f4.trust.irondetect.gui.ResultLogger;
import de.hshannover.f4.trust.irondetect.gui.ResultLoggerImpl;
import de.hshannover.f4.trust.irondetect.model.Action;
import de.hshannover.f4.trust.irondetect.model.Anomaly;
import de.hshannover.f4.trust.irondetect.model.Condition;
import de.hshannover.f4.trust.irondetect.model.ConditionElement;
import de.hshannover.f4.trust.irondetect.model.FeatureExpression;
import de.hshannover.f4.trust.irondetect.model.Hint;
import de.hshannover.f4.trust.irondetect.model.HintExpression;
import de.hshannover.f4.trust.irondetect.model.Policy;
import de.hshannover.f4.trust.irondetect.model.PolicyData;
import de.hshannover.f4.trust.irondetect.model.Rule;
import de.hshannover.f4.trust.irondetect.model.Signature;
import de.hshannover.f4.trust.irondetect.policy.parser.ParseException;
import de.hshannover.f4.trust.irondetect.policy.parser.PolicyFactory;
import de.hshannover.f4.trust.irondetect.policy.parser.TokenMgrError;
import de.hshannover.f4.trust.irondetect.policy.publisher.PolicyPoller;
import de.hshannover.f4.trust.irondetect.policy.publisher.PolicyPublisher;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.handler.PolicyDataManager;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.util.BooleanOperator;
import de.hshannover.f4.trust.irondetect.util.Configuration;
import de.hshannover.f4.trust.irondetect.util.Pair;
import de.hshannover.f4.trust.irondetect.util.PollResultReceiver;
import de.hshannover.f4.trust.irondetect.util.event.Event;
import de.hshannover.f4.trust.irondetect.util.event.EventReceiver;
import de.hshannover.f4.trust.irondetect.util.event.EventType;
import de.hshannover.f4.trust.irondetect.util.event.FeatureBaseUpdateEvent;
import de.hshannover.f4.trust.irondetect.util.event.TrainingData;
import de.hshannover.f4.trust.irondetect.util.event.TrainingDataLoadedEvent;
import de.hshannover.f4.trust.irondetect.util.event.TriggerUpdateEvent;

/**
 *
 * Implements the state machine of the detection engine. Holds the policy.
 * Generates profiles from policy. Testing / Training dispatching
 *
 * @author ibente
 *
 */
public class Processor implements EventReceiver, Runnable, PollResultReceiver {

	private Logger logger = Logger.getLogger(Processor.class);

	private Properties mConfig = Main.getConfig();

	private LinkedBlockingQueue<Event> incomingEvents;
	private Policy mPolicy;

	private boolean isTraining; // TODO state machine
	private Map<String, TrainingData> trainingDataMap;

	private ResultLogger rlogger = ResultLoggerImpl.getInstance();

	private Thread mProcessorThread;

	private boolean mReadNewPolicy;

	private String mCurrentPolicyPath;

	private PolicyPublisher mPolicyPublisher;

	private boolean mAutomaticPolicyReload;

	public Policy getPolicy() {
		return mPolicy;
	}

	/**
	 * Thread-safe and performant Singleton
	 */
	private static Processor instance = new Processor();

	private Processor() {

		this.incomingEvents = new LinkedBlockingQueue<Event>();
		this.trainingDataMap = null;
		mReadNewPolicy = false;
		mCurrentPolicyPath = mConfig.getString(Configuration.KEY_POLICY_FILENAME, Configuration.DEFAULT_VALUE_POLICY_FILENAME);

		// default state is testing
		// setToTesting();
		setTotraining();

		// Parse policy
		readPolicyFromFile(mCurrentPolicyPath);
	}

	public Map<String, TrainingData> getTrainingDataMap(){
		return this.trainingDataMap;
	}

	/**
	 * Singleton
	 *
	 * @return
	 */
	public static Processor getInstance() {
		return Processor.instance;
	}

	/**
	 * @return true if irondetect is training
	 */
	public boolean isTraining() {
		return this.isTraining;
	}

	/**
	 * @return true if irondetect is testing
	 */
	public boolean isTesting() {
		return !this.isTraining;
	}

	/**
	 * set to training mode
	 */
	public synchronized void setTotraining() {
		logger.info("Set to training mode.");
		this.isTraining = true;
	}

	/**
	 * set to testing mode
	 */
	public synchronized void setToTesting() {
		logger.info("Set to testing mode.");
		this.isTraining = false;
	}

	private void readPolicyFromFile(String policyFile) {
		try {

			Policy newPolicy = PolicyFactory.readPolicy(policyFile);

			mPolicy = newPolicy;

			if (mPolicyPublisher != null) {
				mPolicyPublisher.submitChangedPolicy(mPolicy);
			}

		} catch (FileNotFoundException e) {
			logger.error("Policy file could not be loaded: " + e.getMessage() + ", " + e.getCause());
			logger.info("Old policy is still active!");
		} catch (ParseException e) {
			logger.error("Policy could not be parsed: " + e.getMessage() + ", " + e.getCause());
			logger.info("Old policy is still active!");
		} catch (TokenMgrError e) {
			logger.error("Policy could not be parsed: " + e.getMessage() + ", " + e.getCause());
			logger.info("Old policy is still active!");
		}
	}

	public void reloadPolicy() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
	UnmarshalException {
		Map<String, Boolean> firstRun = mPolicy.getFirstRunMap();

		if (mCurrentPolicyPath != null) {
			readNewPolicy(mCurrentPolicyPath);
		} else {
			readNewPolicy(getGraphPolicy());

		}
		// Don't check all rules again for a device when reload the same policy
		mPolicy.setFirstRunMap(firstRun);

		logger.info("Reload policy finished");
	}

	public void readNewPolicy(String policyPath) throws ClassNotFoundException, InstantiationException,
	IllegalAccessException, UnmarshalException {
		readPolicy(policyPath);
	}

	public void readNewPolicy(SearchResult searchResult) throws ClassNotFoundException, InstantiationException,
	IllegalAccessException, UnmarshalException {
		readPolicy(searchResult);
	}

	private void readPolicy(Object newPolicy) throws ClassNotFoundException, InstantiationException,
	IllegalAccessException, UnmarshalException {
		mReadNewPolicy = true;
		boolean newPolicyFinished = false;

		while (!newPolicyFinished) {
			if (mProcessorThread.getState() != State.RUNNABLE) {

				if (newPolicy instanceof String) {
					readPolicyFromFile((String) newPolicy);
					mCurrentPolicyPath = (String) newPolicy;
				} else if (newPolicy instanceof SearchResult) {
					readPolicyFromGraph((SearchResult) newPolicy);
					mCurrentPolicyPath = null;
				}

				newPolicyFinished = true;
			}

			if (!newPolicyFinished) {
				try {
					synchronized (Thread.currentThread()) {
						Thread.currentThread().wait(500);
					}
				} catch (InterruptedException e) {
					logger.info("Got interrupt signal while waiting for new work ...");
					break;
				}
			}
		}

		logPolicyStatus();

		mReadNewPolicy = false;

		synchronized (mProcessorThread) {
			mProcessorThread.notify();
		}
	}

	private void logPolicyStatus() {
		int signatureCount = 0;
		int anomalyCount = 0;
		int actionCount = 0;
		int contextCount = 0;
		int hintCount = 0;

		for (Rule rule : mPolicy.getRuleSet()) {
			actionCount = actionCount + rule.getActions().size();
			for (Pair<ConditionElement, BooleanOperator> conditionPair : rule.getCondition().getConditionSet()) {
				contextCount = contextCount + conditionPair.getFirstElement().getContextSet().size();
				if (conditionPair.getFirstElement() instanceof Signature) {
					signatureCount++;
				} else if (conditionPair.getFirstElement() instanceof Anomaly) {
					Anomaly anomaly = (Anomaly) conditionPair.getFirstElement();
					anomalyCount++;
					hintCount = hintCount + anomaly.getHintSet().size();
				}
			}
		}

		logger.debug("New Policy was set. Rules[" + mPolicy.getRuleSet().size() + "] Signature[" + signatureCount
				+ "] Anomaly[" + anomalyCount + "] Hint[" + hintCount + "] Action[" + actionCount + "] Context["
				+ contextCount + "]");
	}

	public void setPolicyPublisher(PolicyPublisher policyPublisher) {
		mPolicyPublisher = policyPublisher;
	}

	public SearchResult getGraphPolicy() {
		if (mPolicyPublisher == null) {
			logger.warn("No PolicyPublisher was set.");
			return null;
		}

		return mPolicyPublisher.searchPolicyGraph();
	}

	private void readPolicyFromGraph(SearchResult newPolicy) throws ClassNotFoundException, InstantiationException,
	IllegalAccessException, UnmarshalException {

		// transform and filter SearchResult
		List<PolicyData> policyDataList = transformToPolicyData(newPolicy);

		// ############################################
		// ### NOW RECONSTRUCT THE POLICY STRUCTURE ###
		// ############################################

		Policy policy = getPolicyFrom(policyDataList);
		List<Rule> ruleSet = getRuleSetFrom(policyDataList);

		// set RuleSet to the Policy
		policy.setRuleSet(ruleSet);

		// set Signature and Anomaly to Condition
		setConditionElementsToCondition(policyDataList);

		// set Hints to Anomalys
		setHintsToAnomalies(policyDataList);

		// add FeatureIds to rules
		addFeatureIdsToRules(policyDataList);

		// change to the new Policy
		mPolicy = policy;

		mPolicyPublisher.submitChangedPolicy(mPolicy);
	}

	private void addFeatureIdsToRules(List<PolicyData> policyDataList) {
		for (PolicyData policyData : policyDataList) {
			if (policyData instanceof Rule) {
				Rule rule = (Rule) policyData;
				for (Pair<ConditionElement, BooleanOperator> conditionPair : rule.getCondition().getConditionSet()) {
					if (conditionPair.getFirstElement() instanceof Signature) {
						Signature signature = (Signature) conditionPair.getFirstElement();
						for (Pair<FeatureExpression, BooleanOperator> featurePair : signature.getFeatureSet()) {
							String featureID = featurePair.getFirstElement().getFeatureValuePair().getFirstElement();

							rule.addFeatureId(featureID);
						}
					} else if (conditionPair.getFirstElement() instanceof Anomaly) {
						Anomaly anomaly = (Anomaly) conditionPair.getFirstElement();
						for (Pair<HintExpression, BooleanOperator> featurePair : anomaly.getHintSet()) {
							List<String> featureIDs =
									featurePair.getFirstElement().getHintValuePair().getFirstElement().getFeatureIds();

							for (String featureID : featureIDs) {
								rule.addFeatureId(featureID);
							}
						}
					}
				}
			}
		}
	}

	private void setHintsToAnomalies(List<PolicyData> policyDataList) {
		for (PolicyData policyData : policyDataList) {
			if (policyData instanceof Anomaly) {
				Anomaly anomaly = (Anomaly) policyData;

				for (Pair<HintExpression, BooleanOperator> hintPair : anomaly.getHintSet()) {
					for (PolicyData policyData2 : policyDataList) {
						if (policyData2 instanceof Hint) {
							if (hintPair.getFirstElement().getHintValuePair().getFirstElement().getId().equals(
									((Hint) policyData2).getId())) {
								hintPair.getFirstElement().getHintValuePair().setFirstElement((Hint) policyData2);
							}
						}
					}
				}
			}
		}
	}

	private void setConditionElementsToCondition(List<PolicyData> policyDataList) {
		for (PolicyData policyData : policyDataList) {
			if (policyData instanceof Condition) {
				Condition condition = (Condition) policyData;

				for (Pair<ConditionElement, BooleanOperator> conditionPair : condition.getConditionSet()) {
					for (PolicyData policyData2 : policyDataList) {
						if (policyData2 instanceof ConditionElement) {
							if (conditionPair.getFirstElement().getId().equals(((ConditionElement) policyData2)
									.getId())) {
								conditionPair.setFirstElement((ConditionElement) policyData2);
							}
						}
					}
				}
			}
		}
	}

	private List<PolicyData> transformToPolicyData(SearchResult newPolicy)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnmarshalException {
		List<PolicyData> policyDataList = new ArrayList<PolicyData>();

		for(ResultItem ri: newPolicy.getResultItems()){
			Identifier i1 = ri.getIdentifier1();
			Identifier i2 = ri.getIdentifier2();

			PolicyData policyData1 = null;
			PolicyData policyData2 = null;
			if (i1 instanceof ExtendedIdentifier) {
				policyData1 = PolicyDataManager.transformIdentifier((ExtendedIdentifier) i1);
			}

			if (i2 instanceof ExtendedIdentifier) {
				policyData2 = PolicyDataManager.transformIdentifier((ExtendedIdentifier) i2);
			}

			if (policyData1 instanceof Policy) {
				if (!policyDataList.contains(policyData1)) {
					policyDataList.add(policyData1);
				}

			} else if (policyData1 instanceof Rule && policyData2 instanceof Condition) {
				if (policyDataList.contains(policyData1)) {
					policyData1 = policyDataList.get(policyDataList.indexOf(policyData1));
					((Rule) policyData1).setCondition((Condition) policyData2);
					policyDataList.add(policyData2);
				} else {
					policyDataList.add(policyData1);
					((Rule) policyData1).setCondition((Condition) policyData2);
					policyDataList.add(policyData2);
				}

			} else if (policyData1 instanceof Rule && policyData2 instanceof Action) {
				if (policyDataList.contains(policyData1)) {
					policyData1 = policyDataList.get(policyDataList.indexOf(policyData1));
					((Rule) policyData1).addAction((Action) policyData2);
					policyDataList.add(policyData2);
				} else {
					policyDataList.add(policyData1);
					((Rule) policyData1).addAction((Action) policyData2);
					policyDataList.add(policyData2);
				}

			} else if (policyData1 instanceof Signature) {
				if (!policyDataList.contains(policyData1)) {
					policyDataList.add(policyData1);
				}

			} else if (policyData1 instanceof Anomaly) {
				if (!policyDataList.contains(policyData1)) {
					policyDataList.add(policyData1);
				}

			} else if (policyData1 instanceof Hint) {
				if (!policyDataList.contains(policyData1)) {
					policyDataList.add(policyData1);
				}

			}
		}

		return policyDataList;
	}

	private Policy getPolicyFrom(List<PolicyData> policyDataList) throws UnmarshalException {
		for (PolicyData policyData : policyDataList) {
			if (policyData instanceof Policy) {
				return (Policy) policyData;
			}
		}

		throw new UnmarshalException("No Policy found!");
	}

	private List<Rule> getRuleSetFrom(List<PolicyData> policyDataList) {
		List<Rule> ruleSet = new ArrayList<Rule>();
		for (PolicyData policyData : policyDataList) {
			if (policyData instanceof Rule) {
				ruleSet.add((Rule) policyData);
			}
		}
		return ruleSet;
	}

	@Override
	public void run() {
		mProcessorThread = Thread.currentThread();
		logger.info(Processor.class.getSimpleName() + " has started.");

		try {
			while (!mProcessorThread.isInterrupted()) {
				Event e = incomingEvents.take();

				if (mReadNewPolicy) {
					synchronized (mProcessorThread) {
						mProcessorThread.wait();
					}
				}

				onNewPollResult(e);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.info("Got interrupt signal while waiting for new work, exiting ...");
		} finally {
			logger.info("Shutdown complete.");
		}
	}

	private void onNewPollResult(Event e) {
		logger.info("Got new event: " + e.toString());

		if (isTraining) {
			logger.info("training triggered by new event: " + e);
			if (e.getType() == EventType.TRAINING_DATA_LOADED) {
				TrainingDataLoadedEvent event = (TrainingDataLoadedEvent) e;
				Map<String, TrainingData> payload = event.getPayload();
				logger.info("Features have changed on " + payload.size()
				+ " devices.");
				this.trainingDataMap = payload; // FIXME ugly

				for (String singleDevice : this.trainingDataMap.keySet()) {
					logger.trace("Payload size for device '" + singleDevice + "' is: " + trainingDataMap.get(singleDevice).getFeatureIDs().size());
					mPolicy.train(singleDevice);
				}
			} // trigger updates are ignored

			// all training data was loaded and "trained", so set to testing mode
			this.setToTesting();
			logger.info("Training finished -> switching to testing phase.");
		} else {
			logger.info("policy check triggered by new event: " + e);
			if (e.getType() == EventType.FEATURE_BASE_UPDATE) {
				// a Map containing Lists of FeatureIds that changed (new,
				// updated, deleted) during the FeatureBaseUpdate for each
				// device.
				Map<String, Set<String>> payload = ((FeatureBaseUpdateEvent) e)
						.getPayload();
				logger.info("Features have changed on " + payload.size()
				+ " devices.");

				for (String s : payload.keySet()) {
					logger.trace("Payload size for device '" + s + "' is: " + payload.get(s).size());
					mPolicy.check(s, payload.get(s));
				}
				rlogger.reportResultsToLogger("BLANK", mPolicy.getId(), POLICY, true);

			} else if (e.getType() == EventType.TRIGGER) {
				TriggerUpdateEvent triggerEvent = (TriggerUpdateEvent) e;
				Trigger t = triggerEvent.getTrigger();
				logger.info("Trigger update was received: " + t);
				logger.trace("Evaluating rule " + t.getRootRule() + "'");
				t.getRootRule().evaluate(t.getDevice());
			}
			logger.info("Finished testing of incoming poll result.");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.hshannover.f4.trust.irondetect.util.EventReceiver#submitNewEvent
	 * (de.hshannover.f4.trust.irondetect.util.EventType)
	 */
	@Override
	public synchronized void submitNewEvent(Event e) {
		try {
			this.incomingEvents.put(e);
			logger.trace("Event was inserted");
		} catch (InterruptedException e1) {
			logger.error("Could not add Event to Processor:" + e1.getMessage());
		}
	}

	public void startPolicyAutomaticReload() throws IfmapErrorResult, IfmapException {
		mAutomaticPolicyReload = true;
		PolicyPoller.getInstance().addPollResultReceiver(this);

		mPolicyPublisher.startPolicyAutomaticReload();
		mCurrentPolicyPath = null;
	}

	public void stopPolicyAutomaticReload() {
		mAutomaticPolicyReload = false;
	}

	@Override
	public void submitNewPollResult(PollResult pr) {
		if (mAutomaticPolicyReload) {
			for (SearchResult result : pr.getResults()) {
				if (result.getName().equals(PolicyPublisher.SUBSCRIPTION_NAME_POLICY_RELOAD)) {
					try {
						reloadPolicy();
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnmarshalException e) {
						logger.error("Error while automatic read new policy from graph.");
					}
				}
			}
		}
	}
}
