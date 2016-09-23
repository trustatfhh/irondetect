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

import java.util.ArrayList;
import java.util.List;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.irondetect.model.Procedure;
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

			List<String> featureIds = getFeatureIDs(((Hint) eIdentifier).getExpressions());

			Procedure procedure = new Procedure();

			for (String expression : ((Hint) eIdentifier).getExpressions()) {
				String[] expressionArray = expression.split(" ");

				if (expressionArray.length == 3) {
					String procedureId = expressionArray[1];
					String value = expressionArray[2];

					procedure.setId(procedureId);
					procedure.setConfig(value);
					break;

				} else {
					throw new UnmarshalException("False expression syntax.("
							+ expression + ") Example: FEATURE_ID PROCEDURE PROCEDURE_VALUE");
				}
			}
			policyData.setFeatureIds(featureIds);
			policyData.setProcedure(procedure);

			return policyData;

		} else {
			throw new UnmarshalException("False argument this handler is only for Hint ExtendedIdentifier");
		}
	}

	private List<String> getFeatureIDs(List<String> expressions) throws UnmarshalException {
		List<String> featureIds = new ArrayList<String>();

		for (String expression : expressions) {
			String[] expressionArray = expression.split(" ");

			if (expressionArray.length == 3) {
				featureIds.add(expressionArray[0]);
			} else {
				throw new UnmarshalException("False expression syntax.("
						+ expression + ") Example: FEATURE_ID PROCEDURE PROCEDURE_VALUE");
			}
		}

		return featureIds;
	}

	@Override
	public Class<de.hshannover.f4.trust.irondetect.model.Hint> handle() {
		return de.hshannover.f4.trust.irondetect.model.Hint.class;
	}

}
