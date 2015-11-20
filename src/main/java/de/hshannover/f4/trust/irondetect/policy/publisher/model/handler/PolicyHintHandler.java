package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import static de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN;

import java.util.ArrayList;
import java.util.List;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Hint;

/**
 * An {@link PolicyHintHandler} is an {@link PolicyHandler}. It transforms an irondetect policy
 * {@link de.hshannover.f4.trust.irondetect.model.Hint} to an {@link ExtendedIdentifier}-{@link Hint}.
 *
 * @author Marcel Reichenbach
 */
public class PolicyHintHandler implements PolicyHandler<de.hshannover.f4.trust.irondetect.model.Hint> {

	@Override
	public Hint toIdentifier(de.hshannover.f4.trust.irondetect.model.Hint data) {
		List<String> expressions = new ArrayList<String>();

		String hintId = data.getId();
		String procedure = data.getProcedure().getId();
		String value = data.getProcedure().getConfig();

		for (String featureId : data.getFeatureIds()) {
			StringBuilder sb = new StringBuilder();
			sb.append(featureId);
			sb.append(' ');
			sb.append(procedure);
			sb.append(' ');
			sb.append(value);
			expressions.add(sb.toString());
		}

		Hint identifier = new Hint(hintId, expressions, DEFAULT_ADMINISTRATIVE_DOMAIN);

		return identifier;
	}

	@Override
	public de.hshannover.f4.trust.irondetect.model.Hint fromIdentifier(ExtendedIdentifier eIdentifier)
			throws UnmarshalException {

		if (eIdentifier instanceof Hint) {
			de.hshannover.f4.trust.irondetect.model.Hint policyData =
					new de.hshannover.f4.trust.irondetect.model.Hint();
			policyData.setId(((Hint) eIdentifier).getID());
			policyData.setProcedure(null); // TODO

			return policyData;

		} else {
			throw new UnmarshalException("False argument this handler is only for Hint ExtendedIdentifier");
		}
	}

	@Override
	public Class<de.hshannover.f4.trust.irondetect.model.Hint> handle() {
		return de.hshannover.f4.trust.irondetect.model.Hint.class;
	}

}
