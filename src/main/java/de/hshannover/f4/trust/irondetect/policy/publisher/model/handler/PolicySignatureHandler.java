package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import static de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN;

import java.util.List;
import java.util.Map;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Signature;

/**
 * An {@link PolicySignatureHandler} is an {@link PolicyHandler}. It transforms an irondetect policy
 * {@link de.hshannover.f4.trust.irondetect.model.Signature} to an {@link ExtendedIdentifier}-{@link Signature}.
 *
 * @author Marcel Reichenbach
 */
public class PolicySignatureHandler implements PolicyHandler<de.hshannover.f4.trust.irondetect.model.Signature> {

	@Override
	public Signature toIdentifier(de.hshannover.f4.trust.irondetect.model.Signature data) {

		List<String> expressions = HandlerHelper.transformFeatureExpression(data.getFeatureSet());
		Map<String, List<String>> context = HandlerHelper.transformContext(data.getContextSet());

		Signature identifier = new Signature(data.getId(), expressions, DEFAULT_ADMINISTRATIVE_DOMAIN, context);

		return identifier;
	}

	@Override
	public de.hshannover.f4.trust.irondetect.model.Signature fromIdentifier(ExtendedIdentifier eIdentifier)
			throws UnmarshalException {

		if (eIdentifier instanceof Signature) {
			de.hshannover.f4.trust.irondetect.model.Signature policyData =
					new de.hshannover.f4.trust.irondetect.model.Signature();
			policyData.setId(((Signature) eIdentifier).getID());
			policyData.setFeatureSet(null); // TODO
			policyData.setContextSet(null); // TODO

			return policyData;

		} else {
			throw new UnmarshalException("False argument this handler is only for Signature ExtendedIdentifier");
		}
	}

	@Override
	public Class<de.hshannover.f4.trust.irondetect.model.Signature> handle() {
		return de.hshannover.f4.trust.irondetect.model.Signature.class;
	}

}
