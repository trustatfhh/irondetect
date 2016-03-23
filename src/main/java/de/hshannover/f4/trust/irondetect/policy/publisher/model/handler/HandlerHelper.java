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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.irondetect.model.ConditionElement;
import de.hshannover.f4.trust.irondetect.model.Context;
import de.hshannover.f4.trust.irondetect.model.ContextParamType;
import de.hshannover.f4.trust.irondetect.model.ContextParameterPol;
import de.hshannover.f4.trust.irondetect.model.FeatureExpression;
import de.hshannover.f4.trust.irondetect.model.Hint;
import de.hshannover.f4.trust.irondetect.model.HintExpression;
import de.hshannover.f4.trust.irondetect.util.BooleanOperator;
import de.hshannover.f4.trust.irondetect.util.ComparisonOperator;
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * A simple helper Class
 *
 * @author Marcel Reichenbach
 */
public class HandlerHelper {

	static Map<String, List<String>> transformContext(List<Context> contextSet) {
		Map<String, List<String>> context = new HashMap<String, List<String>>();

		for (Context c : contextSet) {
			for (Pair<ContextParameterPol, BooleanOperator> contexPair : c.getCtxParamSet()) {
				StringBuilder sb = new StringBuilder();

				String contextId = c.getId();
				String parameterType = contexPair.getFirstElement().getType().getName();
				String comparisonOperator = contexPair.getFirstElement().getComparisonOperator().valueOf();
				String parameterValue = contexPair.getFirstElement().getValue();

				if (contexPair.getSecondElement() != null) {
					String booleanOperator = contexPair.getSecondElement().toString();
					sb.append(booleanOperator);
					sb.append(' ');
				}

				sb.append(parameterType);
				sb.append(' ');
				sb.append(comparisonOperator);
				sb.append(' ');
				sb.append(parameterValue);

				if (!context.containsKey(contextId)) {
					context.put(contextId, new ArrayList<String>());
				}

				context.get(contextId).add(sb.toString());
			}
		}

		return context;
	}

	static List<Context> reTransformContext(Map<String, List<String>> contextSet) throws UnmarshalException {
		List<Context> contextList = new ArrayList<Context>();

		for(Entry<String, List<String>> entry: contextSet.entrySet()){

			Context context = new Context();
			context.setId(entry.getKey());

			ArrayList<Pair<ContextParameterPol, BooleanOperator>> ctxParamSet =
					new ArrayList<Pair<ContextParameterPol, BooleanOperator>>();

			for (String expression : entry.getValue()) {

				String[] expressionArray = expression.split(" ");

				if (expressionArray.length == 3) {

					ContextParamType parameterType = ContextParamType.valueOf(expressionArray[0]);
					ComparisonOperator comparisonOperator = ComparisonOperator.valueOf2(expressionArray[1]);
					String parameterValue = expressionArray[2];

					Pair<ContextParameterPol, BooleanOperator> ctxParam = buildContextParameterPair(parameterType,
							comparisonOperator, parameterValue, null);

					ctxParamSet.add(ctxParam);

				} else if (expressionArray.length == 4) {

					BooleanOperator booleanOperator = BooleanOperator.valueOf(expressionArray[0]);
					ContextParamType parameterType = ContextParamType.valueOf(expressionArray[1]);
					ComparisonOperator comparisonOperator = ComparisonOperator.valueOf2(expressionArray[2]);
					String parameterValue = expressionArray[3];

					Pair<ContextParameterPol, BooleanOperator> ctxParam = buildContextParameterPair(parameterType,
							comparisonOperator, parameterValue, booleanOperator);

					ctxParamSet.add(ctxParam);

				} else {
					throw new UnmarshalException("False expression syntax.("
							+ expression + ") Example: [BOOLEAN_OPERATOR] CONTEXT_PARAM_TYPE COMPARISON_OPERATOR "
							+ "PARAMETER_VALUE");
				}
			}

			context.setCtxParamSet(ctxParamSet);
		}

		return contextList;
	}

	private static Pair<ContextParameterPol, BooleanOperator> buildContextParameterPair(ContextParamType parameterType,
			ComparisonOperator comparisonOperator, String parameterValue, BooleanOperator booleanOperator) {

		ContextParameterPol contextParameter = new ContextParameterPol("", parameterType, parameterValue,
				comparisonOperator);

		Pair<ContextParameterPol, BooleanOperator> contextParameterPair =
				new Pair<ContextParameterPol, BooleanOperator>(contextParameter, booleanOperator);

		return contextParameterPair;
	}

	static List<String> transformFeatureExpression(List<Pair<FeatureExpression, BooleanOperator>> expressions) {
		List<String> stringExpressions = new ArrayList<String>();

		for (Pair<FeatureExpression, BooleanOperator> p : expressions) {
			StringBuilder sb = new StringBuilder();

			if (p.getSecondElement() != null) {
				String booleanOperator = p.getSecondElement().toString();
				sb.append(booleanOperator);
				sb.append(' ');
			}
			String feature = p.getFirstElement().getFeatureValuePair().getFirstElement();
			String comparisonOperator = p.getFirstElement().getFeatureValuePair().getSecondElement().getFirstElement()
					.valueOf();
			String featureValue = p.getFirstElement().getFeatureValuePair().getSecondElement().getSecondElement();


			sb.append(feature);
			sb.append(' ');
			sb.append(comparisonOperator);
			sb.append(' ');
			sb.append(escapeSpaces(featureValue));

			stringExpressions.add(sb.toString());
		}

		return stringExpressions;
	}

	protected static String escapeSpaces(String input) {

		String ret = input;

		String[] unwanted = {" "};
		String[] replaceBy = {"&space;"};

		for (int i = 0; i < unwanted.length; i++) {
			ret = ret.replace(unwanted[i], replaceBy[i]);
		}

		return ret;
	}

	protected static String deEscapeSpaces(String input) {

		String ret = input;

		String[] unwanted = {"&space;"};
		String[] replaceBy = {" "};

		for (int i = 0; i < unwanted.length; i++) {
			ret = ret.replace(unwanted[i], replaceBy[i]);
		}

		return ret;
	}

	static List<Pair<FeatureExpression, BooleanOperator>> retransformFeatureExpression(List<String> expressions)
			throws UnmarshalException {

		List<Pair<FeatureExpression, BooleanOperator>> featureExpressionList =
				new ArrayList<Pair<FeatureExpression, BooleanOperator>>();

		for (String expression : expressions) {

			String[] expressionArray = expression.split(" ");

			if (expressionArray.length == 3) {

				String feature = expressionArray[0];
				ComparisonOperator comparisonOperator = ComparisonOperator.valueOf2(expressionArray[1]);
				String featureValue = deEscapeSpaces(expressionArray[2]);

				Pair<FeatureExpression, BooleanOperator> featureExpression =
						buildFeatureExpression(feature, comparisonOperator, featureValue, null);

				featureExpressionList.add(featureExpression);

			} else if (expressionArray.length == 4) {

				BooleanOperator booleanOperator = BooleanOperator.valueOf(expressionArray[0]);
				String feature = expressionArray[1];
				ComparisonOperator comparisonOperator = ComparisonOperator.valueOf2(expressionArray[2]);
				String featureValue = deEscapeSpaces(expressionArray[3]);

				Pair<FeatureExpression, BooleanOperator> featureExpression =
						buildFeatureExpression(feature, comparisonOperator, featureValue, booleanOperator);

				featureExpressionList.add(featureExpression);

			} else {
				throw new UnmarshalException("False expression syntax.("
						+ expression + ") Example: [BOOLEAN_OPERATOR] FEATURE COMPARISON_OPERATOR FEATURE_VALUE");
			}
		}

		return featureExpressionList;
	}

	private static Pair<FeatureExpression, BooleanOperator> buildFeatureExpression(String feature,
			ComparisonOperator comparisonOperator, String featureValue, BooleanOperator booleanOperator) {

		Pair<ComparisonOperator, String> secondElement =
				new Pair<ComparisonOperator, String>(comparisonOperator, featureValue);
		Pair<String, Pair<ComparisonOperator, String>> featureValuePair =
				new Pair<String, Pair<ComparisonOperator, String>>(feature, secondElement);

		FeatureExpression featureExpression = new FeatureExpression();
		featureExpression.setFeatureValuePair(featureValuePair);

		return new Pair<FeatureExpression, BooleanOperator>(featureExpression, booleanOperator);

	}

	static List<String> transformHintExpression(List<Pair<HintExpression, BooleanOperator>> expressions) {
		List<String> stringExpressions = new ArrayList<String>();

		for (Pair<HintExpression, BooleanOperator> p : expressions) {
			StringBuilder sb = new StringBuilder();

			if (p.getSecondElement() != null) {
				String booleanOperator = p.getSecondElement().toString();
				sb.append(booleanOperator);
				sb.append(' ');
			}

			String hintId = p.getFirstElement().getHintValuePair().getFirstElement().getId();
			String comparisonOperator = p.getFirstElement().getHintValuePair().getSecondElement().getFirstElement()
					.valueOf();
			String hintValue = p.getFirstElement().getHintValuePair().getSecondElement().getSecondElement();

			sb.append(hintId);
			sb.append(' ');
			sb.append(comparisonOperator);
			sb.append(' ');
			sb.append(hintValue);

			stringExpressions.add(sb.toString());
		}

		return stringExpressions;
	}

	static List<Pair<HintExpression, BooleanOperator>> retransformHintExpression(List<String> expressions)
			throws UnmarshalException {
		List<Pair<HintExpression, BooleanOperator>> hintExpressions =
				new ArrayList<Pair<HintExpression, BooleanOperator>>();

		for (String expression : expressions) {
			String[] expressionArray = expression.split(" ");

			if (expressionArray.length == 3) {

				String hintId = expressionArray[0];
				ComparisonOperator comparisonOperator = ComparisonOperator.valueOf2(expressionArray[1]);
				String hintValue = expressionArray[2];

				Pair<HintExpression, BooleanOperator> hintExpression =
						buildHintExpression(hintId, comparisonOperator, hintValue, null);

				hintExpressions.add(hintExpression);

			} else if (expressionArray.length == 4) {

				BooleanOperator booleanOperator = BooleanOperator.valueOf(expressionArray[0]);
				String hintId = expressionArray[1];
				ComparisonOperator comparisonOperator = ComparisonOperator.valueOf2(expressionArray[2]);
				String hintValue = expressionArray[3];

				Pair<HintExpression, BooleanOperator> hintExpression =
						buildHintExpression(hintId, comparisonOperator, hintValue, booleanOperator);

				hintExpressions.add(hintExpression);

			} else {
				throw new UnmarshalException("False expression syntax.("
						+ expression + ") Example: [BOOLEAN_OPERATOR] HINT_ID COMPARISON_OPERATOR HINT_VALUE");
			}
		}

		return hintExpressions;
	}

	private static Pair<HintExpression, BooleanOperator> buildHintExpression(String hintId,
			ComparisonOperator comparisonOperator, String hintValue, BooleanOperator booleanOperator) {

		Pair<ComparisonOperator, String> comparisonPair =
				new Pair<ComparisonOperator, String>(comparisonOperator, hintValue);

		Hint hint = new Hint();
		hint.setId(hintId);

		Pair<Hint, Pair<ComparisonOperator, String>> hintValuePair =
				new Pair<Hint, Pair<ComparisonOperator, String>>(hint, comparisonPair);

		HintExpression expression = new HintExpression();
		expression.setHintValuePair(hintValuePair);

		Pair<HintExpression, BooleanOperator> hintExpression =
				new Pair<HintExpression, BooleanOperator>(expression, booleanOperator);

		return hintExpression;
	}

	static List<String> transformConditionExpression(List<Pair<ConditionElement, BooleanOperator>> conditionSet) {
		List<String> stringExpressions = new ArrayList<String>();

		for (Pair<ConditionElement, BooleanOperator> p : conditionSet) {
			StringBuilder sb = new StringBuilder();

			if (p.getSecondElement() != null) {
				String booleanOperator = p.getSecondElement().toString();
				sb.append(booleanOperator);
				sb.append(' ');
			}

			String conditionElementId = p.getFirstElement().getId();

			sb.append(conditionElementId);

			stringExpressions.add(sb.toString());
		}

		return stringExpressions;
	}

	static List<Pair<ConditionElement, BooleanOperator>> retransformConditionExpression(List<String> expressions)
			throws UnmarshalException {

		List<Pair<ConditionElement, BooleanOperator>> conditionSet =
				new ArrayList<Pair<ConditionElement, BooleanOperator>>();

		for (String expression : expressions) {

			String[] expressionArray = expression.split(" ");
			Pair<ConditionElement, BooleanOperator> condition;

			if (expressionArray.length == 1) {
				condition = new Pair<ConditionElement, BooleanOperator>(new ConditionElement(expressionArray[0]), null);
			} else if (expressionArray.length == 2) {
				condition = new Pair<ConditionElement, BooleanOperator>(new ConditionElement(expressionArray[1]),
						BooleanOperator.valueOf(expressionArray[0]));
			} else {
				throw new UnmarshalException("False expression syntax.("
						+ expression + ") Example: [BOOLEAN_OPERATOR] CONDITION_ELEMENT");
			}

			conditionSet.add(condition);
		}

		return conditionSet;
	}

}
