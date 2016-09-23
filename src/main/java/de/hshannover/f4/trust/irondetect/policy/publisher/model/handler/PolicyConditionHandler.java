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

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Condition;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;

/**
 * An {@link PolicyConditionHandler} is an {@link PolicyHandler}. It transforms an irondetect policy
 * {@link de.hshannover.f4.trust.irondetect.model.Condition} to an {@link ExtendedIdentifier}-{@link Condition}.
 *
 * @author Marcel Reichenbach
 */
public class PolicyConditionHandler implements PolicyHandler<de.hshannover.f4.trust.irondetect.model.Condition> {

	@Override
	public Condition toIdentifier(de.hshannover.f4.trust.irondetect.model.Condition data) {
		String conditionId = data.getId();
		List<String> expressions = HandlerHelper.transformConditionExpression(data.getConditionSet());

		Condition identifier = new Condition(conditionId, expressions, DEFAULT_ADMINISTRATIVE_DOMAIN);

		return identifier;
	}

	@Override
	public de.hshannover.f4.trust.irondetect.model.Condition fromIdentifier(ExtendedIdentifier eIdentifier)
			throws UnmarshalException {

		if (eIdentifier instanceof Condition) {
			de.hshannover.f4.trust.irondetect.model.Condition policyData =
					new de.hshannover.f4.trust.irondetect.model.Condition();
			policyData.setId(((Condition) eIdentifier).getID());
			policyData.setConditionSet(
					HandlerHelper.retransformConditionExpression(((Condition) eIdentifier).getExpressions()));

			return policyData;

		} else {
			throw new UnmarshalException("False argument this handler is only for Condition ExtendedIdentifier");
		}
	}

	@Override
	public Class<de.hshannover.f4.trust.irondetect.model.Condition> handle() {
		return de.hshannover.f4.trust.irondetect.model.Condition.class;
	}

}
