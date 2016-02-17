package de.hshannover.f4.trust.irondetect.policy.publisher;

import java.util.ArrayList;
import java.util.List;
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
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;
import de.hshannover.f4.trust.irondetect.util.BooleanOperator;
import de.hshannover.f4.trust.irondetect.util.Pair;
import de.hshannover.f4.trust.irondetect.util.PollResultReceiver;

/**
 * An {@link PolicyFeatureUpdater} publishes for a new ESUKOM-feature metadata a new policy-feature metadata with
 * reference to the ESUKOM-feature.
 *
 * @author Marcel Reichenbach
 */
public class PolicyFeatureUpdater implements Runnable, PollResultReceiver {

	private static final Logger LOGGER = Logger.getLogger(PolicyFeatureUpdater.class);

	private static final String ESUKOM_CATEGORY_IDENTIFIER = "32939:category";

	private static final String FEATURE_TYPE_NAME = "feature";

	private static final String XMLNS_FEATURE_URL_PREFIX = "xmlns:esukom";

	private static final String ESUKOM_URL = "http://www.esukom.de/2012/ifmap-metadata/1";

	private LinkedBlockingQueue<PollResult> mNewPollResults;

	private Policy mPolicy;

	private SSRC mSsrc;

	protected List<PublishUpdate> mPublishUpdates;

	private PolicyMetadataFactory mMetadataFactory;

	public PolicyFeatureUpdater(Policy policy, SSRC ssrc) throws IfmapErrorResult, IfmapException {
		init(policy, ssrc);
	}

	protected void init(Policy policy, SSRC ssrc) {
		mPolicy = policy;
		mSsrc = ssrc;
		mNewPollResults = new LinkedBlockingQueue<PollResult>();
		mMetadataFactory = new PolicyMetadataFactory();
		mPublishUpdates = new ArrayList<PublishUpdate>();
	}

	protected void sendPublishUpdate() throws IfmapErrorResult, IfmapException {
		PublishRequest req = Requests.createPublishReq();

		if (mPublishUpdates.size() > 0) {
			for (PublishUpdate updateElement : mPublishUpdates) {
				req.addPublishElement(updateElement);
			}
			mSsrc.publish(req);
			LOGGER.debug("sended publish request for " + mPublishUpdates.size() + " policy-rev-metadata");
		} else {
			LOGGER.trace("Nothig was sended. Wait for new poll results...");
		}
	}

	private void addPublishUpdate(Identifier identifier, Document metadata) {
		PublishUpdate update = Requests.createPublishUpdate();

		update.setIdentifier1(identifier);
		update.addMetadata(metadata);
		update.setLifeTime(MetadataLifetime.session);

		mPublishUpdates.add(update);

	}

	private String findElement(String element, Element root) {
		String value = "";
		NodeList childs = root.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node idNode = childs.item(i);
			if (idNode != null && element.equals(idNode.getLocalName())) {
				Node idValueNode = idNode.getFirstChild();
				if (idValueNode != null && idValueNode.getNodeType() == Node.TEXT_NODE) {
					value = idValueNode.getTextContent();
					break;
				}
			}
		}
		return value;
	}

	private void addFeatureRevIfExistInPolicy(Identity categoryIdentity, Document featureMetadata) throws DOMException,
	MarshalException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		String metadataFeatureId = categoryIdentity.getName() + "." + findElement("id", featureMetadata.getDocumentElement());
		String metadataFeatureValue = findElement("value", featureMetadata.getDocumentElement());

		for (Rule r : mPolicy.getRuleSet()) {
			for (Pair<ConditionElement, BooleanOperator> p : r.getCondition().getConditionSet()) {
				ConditionElement conditionElement = p.getFirstElement();

				if (conditionElement instanceof Anomaly) {
					Anomaly anomaly = (Anomaly) conditionElement;
					for (Pair<HintExpression, BooleanOperator> ex : anomaly.getHintSet()) {
						Hint hint = ex.getFirstElement().getHintValuePair().getFirstElement();
						String featureValue = ex.getFirstElement().getHintValuePair().getSecondElement()
								.getSecondElement();

						for (String featureId : hint.getFeatureIds()) {
							LOGGER.trace("check feature-id: " + featureId);

							if (featureId.equalsIgnoreCase(metadataFeatureId)
									&& featureValue.equalsIgnoreCase(metadataFeatureValue)) {
								ExtendedIdentifier identfierHint = PolicyDataManager.transformPolicyData(hint);
								Document revFeatureMetadata = mMetadataFactory.createRevMetadata(
										PolicyStrings.POLICY_FEATURE_EL_NAME, featureMetadata, categoryIdentity);
								addPublishUpdate(identfierHint, revFeatureMetadata);
							} else {
								LOGGER.trace("not equals (ID from Policy: '" + featureId + "' |ID from PollResult: '"
										+ metadataFeatureId + "' )");
							}
						}
					}
				} else if (conditionElement instanceof Signature) {
					Signature signature = (Signature) conditionElement;

					for (Pair<FeatureExpression, BooleanOperator> featurePair : signature.getFeatureSet()) {
						String featureId = featurePair.getFirstElement().getFeatureId();
						String featureValue = featurePair.getFirstElement().getFeatureValuePair().getSecondElement()
								.getSecondElement();

						LOGGER.trace("check feature-id: " + featureId);

						if (featureId.equalsIgnoreCase(metadataFeatureId)
								&& featureValue.equalsIgnoreCase(metadataFeatureValue)) {
							ExtendedIdentifier identfierSignature = PolicyDataManager.transformPolicyData(signature);
							Document revFeatureMetadata = mMetadataFactory.createRevMetadata(
									PolicyStrings.POLICY_FEATURE_EL_NAME, featureMetadata,
									categoryIdentity);
							addPublishUpdate(identfierSignature, revFeatureMetadata);
						} else {
							LOGGER.trace("not equals (ID from Policy: '" + featureId + "' |ID from PollResult: '"
									+ metadataFeatureId + "' )");
						}
					}
				}
			}
		}
	}

	/**
	 * Submit a new {@link PollResult} to this {@link PolicyFeatureUpdater}.
	 *
	 * @param pollResult A new {@link PollResult} to submit
	 */
	@Override
	public void submitNewPollResult(PollResult pollResult) {
		LOGGER.trace("new Poll-Result...");
		try {
			mNewPollResults.put(pollResult);
		} catch (InterruptedException e) {
			LOGGER.error("InterruptedException when submit new Poll-Result: " + e.getMessage());
		}
		LOGGER.trace("... new Poll-Result submitted");
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

		for (SearchResult searchResult : pollResult.getResults()) {
			LOGGER.trace("New Search-Result: " + searchResult.getName());
			for (ResultItem resultItem : searchResult.getResultItems()) {
				LOGGER.trace("new result item(" + resultItem + ")");
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
					for (Document metadata : resultItem.getMetadata()) {
						if (checkOfEsukomFeatureMetadata(metadata)) {
							try {
								addFeatureRevIfExistInPolicy(identity, metadata);
							} catch (DOMException | MarshalException | ClassNotFoundException
									| InstantiationException | IllegalAccessException e) {
								LOGGER.error(e.getClass().getSimpleName() + " when add policy-feature metadata");
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void run() {
		LOGGER.info("run() ...");

		while (!Thread.currentThread().isInterrupted()) {
			try {

				newPollResult();

				try {
					sendPublishUpdate();
					mPublishUpdates.clear();
				} catch (IfmapErrorResult e) {
					LOGGER.error(e.getClass().getSimpleName() + " when send policy-feature (" + e.toString() + ")");
				} catch (IfmapException e) {
					LOGGER.error(e.getClass().getSimpleName() + " when send policy-feature (Message= " + e.getMessage()
					+ " |Description= " + e.getDescription() + ")");
				}

			} catch (InterruptedException e) {
				LOGGER.error(e.getClass().getSimpleName() + " when take a new Poll-Result");
				break;
			}
		}

		LOGGER.info("... run()");
	}

	public void submitChangedPolicy(Policy newPolicy) {
		mPolicy = newPolicy;
	}

}
