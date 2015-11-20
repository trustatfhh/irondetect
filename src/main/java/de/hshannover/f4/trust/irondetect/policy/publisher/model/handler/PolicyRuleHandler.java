package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import static de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Rule;

/**
 * An {@link PolicyRuleHandler} is an {@link PolicyHandler}. It transforms an irondetect policy
 * {@link de.hshannover.f4.trust.irondetect.model.Rule} to an {@link ExtendedIdentifier}-{@link Rule}.
 *
 * @author Marcel Reichenbach
 */
public class PolicyRuleHandler implements PolicyHandler<de.hshannover.f4.trust.irondetect.model.Rule> {

	@Override
	public Rule toIdentifier(de.hshannover.f4.trust.irondetect.model.Rule data) {
		String ruleId = data.getId();

		Rule identifier = new Rule(ruleId, DEFAULT_ADMINISTRATIVE_DOMAIN);

		return identifier;
	}

	@Override
	public de.hshannover.f4.trust.irondetect.model.Rule fromIdentifier(ExtendedIdentifier eIdentifier)
			throws UnmarshalException {

		if (eIdentifier instanceof Rule) {
			de.hshannover.f4.trust.irondetect.model.Rule policyData =
					new de.hshannover.f4.trust.irondetect.model.Rule();
			policyData.setId(((Rule) eIdentifier).getID());

			return policyData;

		} else {
			throw new UnmarshalException("False argument this handler is only for Rule ExtendedIdentifier");
		}
	}

	@Override
	public Class<de.hshannover.f4.trust.irondetect.model.Rule> handle() {
		return de.hshannover.f4.trust.irondetect.model.Rule.class;
	}

}
