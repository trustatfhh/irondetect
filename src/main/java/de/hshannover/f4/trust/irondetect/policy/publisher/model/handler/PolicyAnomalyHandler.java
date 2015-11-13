package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import static de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN;

import java.util.List;
import java.util.Map;

import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Anomaly;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;

/**
 * An {@link PolicyAnomalyHandler} is an {@link PolicyHandler}. It transforms an irondetect policy
 * {@link de.hshannover.f4.trust.irondetect.model.Anomaly} to an {@link ExtendedIdentifier}-{@link Anomaly}.
 * 
 * @author Marcel Reichenbach
 */
public class PolicyAnomalyHandler implements PolicyHandler<de.hshannover.f4.trust.irondetect.model.Anomaly> {

	@Override
	public Anomaly toIdentifier(de.hshannover.f4.trust.irondetect.model.Anomaly data) {

		List<String> expressions = HandlerHelper.transformHintExpression(data.getHintSet());
		Map<String, List<String>> context = HandlerHelper.transformContext(data.getContextSet());

		Anomaly identifier = new Anomaly(data.getId(), expressions, DEFAULT_ADMINISTRATIVE_DOMAIN, context);

		return identifier;
	}

	@Override
	public Class<de.hshannover.f4.trust.irondetect.model.Anomaly> handle() {
		return de.hshannover.f4.trust.irondetect.model.Anomaly.class;
	}

}