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
 * This file is part of irondetect, version 0.0.9, 
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

			List<Pair<String, String>> keyValuePairs = new ArrayList<Pair<String, String>>();
			List<String> expressions = ((Action) eIdentifier).getExpressions();

			for (String expression : expressions) {
				String firstElement = expression.substring(0, expression.indexOf(" "));
				String secodElement = expression.substring(expression.indexOf(" ")
						+ 1, expression.length());

				keyValuePairs.add(new Pair<String, String>(firstElement, secodElement));
			}

			policyData.setKeyValuePairs(keyValuePairs);

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
