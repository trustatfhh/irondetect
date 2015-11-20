package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import static de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN;

import java.util.ArrayList;
import java.util.List;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Action;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * An {@link PolicyActionHandler} is an {@link PolicyHandler}. It transforms an irondetect policy
 * {@link de.hshannover.f4.trust.irondetect.model.Action} to an {@link ExtendedIdentifier}-{@link Action}.
 *
 * @author Marcel Reichenbach
 */
public class PolicyActionHandler implements PolicyHandler<de.hshannover.f4.trust.irondetect.model.Action> {

	@Override
	public Action toIdentifier(de.hshannover.f4.trust.irondetect.model.Action data) {
		String actionId = data.getId();

		List<String> expressions = new ArrayList<String>();

		for (Pair<String, String> p : data.getKeyValuePairs()) {
			StringBuilder sb = new StringBuilder();
			sb.append(p.getFirstElement());
			sb.append(' ');
			sb.append(p.getSecondElement());
			expressions.add(sb.toString());
		}

		Action identifier = new Action(actionId, expressions, DEFAULT_ADMINISTRATIVE_DOMAIN);

		return identifier;
	}

	@Override
	public de.hshannover.f4.trust.irondetect.model.Action fromIdentifier(ExtendedIdentifier eIdentifier)
			throws UnmarshalException {

		if (eIdentifier instanceof Action) {
			de.hshannover.f4.trust.irondetect.model.Action policyData =
					new de.hshannover.f4.trust.irondetect.model.Action();
			policyData.setId(((Action) eIdentifier).getID());
			policyData.setKeyValuePairs(null); // TODO wie im oben

			return policyData;

		} else {
			throw new UnmarshalException("False argument this handler is only for Action ExtendedIdentifier");
		}
	}

	@Override
	public Class<de.hshannover.f4.trust.irondetect.model.Action> handle() {
		return de.hshannover.f4.trust.irondetect.model.Action.class;
	}
}
