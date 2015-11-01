package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import de.hshannover.f4.trust.irondetect.model.PolicyData;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;

/**
 * A {@link PolicyHandler} interface. It transforms an irondetect {@link PolicyData} to an {@link ExtendedIdentifier}.
 * 
 * @author Marcel Reichenbach
 */
public interface PolicyHandler<T extends PolicyData> {

	public ExtendedIdentifier toIdentifier(T data);

	public Class<T> handle();
}
