package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import org.w3c.dom.Element;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.irondetect.model.PolicyData;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.ExtendedIdentifierHandler;

/**
 * A {@link PolicyDataManager} transforms an irondetect {@link PolicyData} to an {@link ExtendedIdentifier}.
 *
 * @author Marcel Reichenbach
 */
public class PolicyDataManager {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ExtendedIdentifier transformPolicyData(PolicyData data) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException {

		PolicyHandler handler = PolicyHandlerManager.getHandlerFor(data);

		return handler.toIdentifier(data);
	}

	@SuppressWarnings("rawtypes")
	public static PolicyData transformIdentifier(ExtendedIdentifier eIdentifier) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException, UnmarshalException {

		PolicyHandler handler = PolicyHandlerManager.getHandlerFor(eIdentifier);

		return handler.fromIdentifier(eIdentifier);
	}

	@SuppressWarnings("unchecked")
	public static PolicyData transformElement(Element policyElemen)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnmarshalException {

		ExtendedIdentifierHandler<? extends ExtendedIdentifier> eIh =
				(ExtendedIdentifierHandler<? extends ExtendedIdentifier>) Identifiers.getHandlerFor(policyElemen);

		ExtendedIdentifier identifier = eIh.fromElement(policyElemen);

		return transformIdentifier(identifier);
	}

}