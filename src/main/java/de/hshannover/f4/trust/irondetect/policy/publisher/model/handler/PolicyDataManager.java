package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import de.hshannover.f4.trust.irondetect.model.PolicyData;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;

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

}