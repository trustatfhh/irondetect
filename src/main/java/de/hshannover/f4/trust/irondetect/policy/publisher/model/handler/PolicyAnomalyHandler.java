/*
 * #%L
 * =====================================================
 *   _____                _     ____  _   _       _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Hochschule Hannover
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de/
 * 
 * This file is part of irondetect, version 0.0.10, 
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2016 Trust@HsH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import static de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN;

import java.util.List;
import java.util.Map;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
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
	public de.hshannover.f4.trust.irondetect.model.Anomaly fromIdentifier(ExtendedIdentifier eIdentifier)
			throws UnmarshalException {

		if (eIdentifier instanceof Anomaly) {
			de.hshannover.f4.trust.irondetect.model.Anomaly policyData =
					new de.hshannover.f4.trust.irondetect.model.Anomaly();
			policyData.setId(((Anomaly) eIdentifier).getID());
			policyData.setHintSet(HandlerHelper.retransformHintExpression(((Anomaly) eIdentifier).getExpressions()));
			policyData.setContextSet(HandlerHelper.reTransformContext(((Anomaly) eIdentifier).getContext()));

			return policyData;

		} else {
			throw new UnmarshalException("False argument this handler is only for Anomaly ExtendedIdentifier");
		}
	}

	@Override
	public Class<de.hshannover.f4.trust.irondetect.model.Anomaly> handle() {
		return de.hshannover.f4.trust.irondetect.model.Anomaly.class;
	}

}
