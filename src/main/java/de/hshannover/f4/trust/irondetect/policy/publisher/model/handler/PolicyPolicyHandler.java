package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import static de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Policy;

/**
 * An {@link PolicyPolicyHandler} is an {@link PolicyHandler}. It transforms an irondetect policy
 * {@link de.hshannover.f4.trust.irondetect.model.Policy} to an {@link ExtendedIdentifier}-{@link Policy}.
 *
 * @author Marcel Reichenbach
 */
public class PolicyPolicyHandler implements PolicyHandler<de.hshannover.f4.trust.irondetect.model.Policy> {

	@Override
	public Policy toIdentifier(de.hshannover.f4.trust.irondetect.model.Policy data) {
		String policyId = data.getId();

		Policy identifier = new Policy(policyId, DEFAULT_ADMINISTRATIVE_DOMAIN);

		return identifier;
	}

	@Override
	public de.hshannover.f4.trust.irondetect.model.Policy fromIdentifier(ExtendedIdentifier eIdentifier)
			throws UnmarshalException {

		if (eIdentifier instanceof Policy) {
			de.hshannover.f4.trust.irondetect.model.Policy policyData =
					new de.hshannover.f4.trust.irondetect.model.Policy();
			policyData.setId(((Policy) eIdentifier).getID());

			return policyData;

		} else {
			throw new UnmarshalException("False argument this handler is only for Policy ExtendedIdentifier");
		}
	}

	@Override
	public Class<de.hshannover.f4.trust.irondetect.model.Policy> handle() {
		return de.hshannover.f4.trust.irondetect.model.Policy.class;
	}

}
