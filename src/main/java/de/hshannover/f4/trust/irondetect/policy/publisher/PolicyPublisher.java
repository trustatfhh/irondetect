package de.hshannover.f4.trust.irondetect.policy.publisher;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.channel.ThreadSafeSsrc;
import de.hshannover.f4.trust.ifmapj.config.BasicAuthConfig;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.exception.InitializationException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.messages.MetadataLifetime;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.hshannover.f4.trust.ifmapj.messages.PublishUpdate;
import de.hshannover.f4.trust.ifmapj.messages.Requests;
import de.hshannover.f4.trust.irondetect.gui.ResultLoggerImpl;
import de.hshannover.f4.trust.irondetect.ifmap.EndpointPoller;
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
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * An {@link PolicyPublisher} publishes all irondetect policies.
 * 
 * @author Marcel Reichenbach
 */
public class PolicyPublisher {

	private static final Logger LOGGER = Logger.getLogger(PolicyPublisher.class);

	protected Policy mPolicy;

	private SSRC mSsrc;

	private PolicyFeatureUpdater mPolicyFeatureUpdater;

	private PolicyActionUpdater mPolicyActionUpdater;

	protected List<PublishUpdate> mPublishUpdates;
	
	private PolicyMetadataFactory mMetadataFactory;

	private String policyPublisherIdentifier = "irondetect-policy";

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
		policyPublisherIdentifier = Configuration.getPolicyPublisherIdentifier();

		connect();
		buildPublishUpdate();
		sendPublishUpdate();

		mPolicyFeatureUpdater = new PolicyFeatureUpdater(mPolicy, mSsrc);
		mPolicyActionUpdater = new PolicyActionUpdater(mPolicy, mSsrc);

		ResultLoggerImpl.getInstance().addEventReceiver(mPolicyActionUpdater);
		EndpointPoller.getInstance().addPollResultReceiver(mPolicyFeatureUpdater);
		EndpointPoller.getInstance().addPollResultReceiver(mPolicyActionUpdater);

		Thread featureThread = new Thread(mPolicyFeatureUpdater, PolicyFeatureUpdater.class.getSimpleName() + "-Thread");
		Thread actionThread = new Thread(mPolicyActionUpdater, PolicyActionUpdater.class.getSimpleName() + "-Thread");

		featureThread.start();
		actionThread.start();
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

	private void connect() throws IfmapErrorResult, IfmapException {
		String url = Configuration.ifmapUrlBasic();
		String userName = Configuration.irondetectPolicyPublisherUser();
		String userPassword = Configuration.irondetectPolicyPublisherPassword();
		String truststorePath = Configuration.keyStorePath();
		String truststorePassword = Configuration.keyStorePassword();
		int maxPollResultSize = Configuration.ifmapMaxResultSize();

		initSsrc(url, userName, userPassword, truststorePath, truststorePassword);
		initSession(maxPollResultSize);

	}

	private void initSsrc(String url, String user, String userPass, String truststore, String truststorePassword)
			throws InitializationException {
		LOGGER.trace("init SSRC ...");

		BasicAuthConfig config = new BasicAuthConfig(url, user, userPass, truststore, truststorePassword, true,
				120 * 1000);
		mSsrc = new ThreadSafeSsrc(IfmapJ.createSsrc(config));

		LOGGER.debug("init SSRC OK");
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

}
