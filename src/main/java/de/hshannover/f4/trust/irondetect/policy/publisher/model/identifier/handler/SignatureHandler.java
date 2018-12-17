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
 * Copyright (C) 2010 - 2018 Trust@HsH
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
package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hshannover.f4.trust.ifmapj.binding.IfmapStrings;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers.Helpers;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Signature;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;
import util.DomHelpers;

/**
 * An {@link SignatureHandler} extends the ExtendedIdentifierHandler. It transforms an {@link Signature}-
 * {@link ExtendedIdentifier} to a XML {@link Element}.
 *
 * @author Marcel Reichenbach
 */
public class SignatureHandler extends ExtendedIdentifierHandler<Signature> {

	@Override
	public Element toExtendedElement(Identifier i, Document doc) throws MarshalException {
		Helpers.checkIdentifierType(i, this);

		Signature signature = (Signature) i;

		String id = signature.getID();
		List<String> expressions = signature.getExpressions();
		Map<String, List<String>> context = signature.getContext();

		if (id == null) {
			throw new MarshalException("No id set");
		}

		if (expressions == null) {
			throw new MarshalException("Signature with null expressions not allowed");
		}

		if (context == null) {
			throw new MarshalException("Signature with null context not allowed");
		}

		Element signatureElement = doc.createElementNS(PolicyStrings.POLICY_IDENTIFIER_NS_URI,
				PolicyStrings.SIGNATURE_EL_NAME);
		Element idElement = doc.createElementNS(null, PolicyStrings.ID_EL_NAME);

		List<Element> expressionElements = buildFeatureExpressionElements(expressions, doc);
		List<Element> contextElements = super.buildContextElements(context, doc);

		idElement.setTextContent(id);

		signatureElement.appendChild(idElement);
		super.appendListAsChild(signatureElement, expressionElements);
		super.appendListAsChild(signatureElement, contextElements);

		Helpers.addAdministrativeDomain(signatureElement, signature);

		return signatureElement;
	}

	private Element buildFeatureExpressionElement(String expression, Document doc) {
		return super.buildElement(PolicyStrings.FEATURE_EXPRESSION_EL_NAME, expression, doc);
	}

	private List<Element> buildFeatureExpressionElements(List<String> expressions, Document doc) {
		List<Element> expressionElements = new ArrayList<Element>();

		for (String s : expressions) {
			expressionElements.add(buildFeatureExpressionElement(s, doc));
		}

		return expressionElements;
	}

	@Override
	public Signature fromExtendedElement(Element element) throws UnmarshalException {
		if (!super.policyElementMatches(element, PolicyStrings.SIGNATURE_EL_NAME)) {
			return null;
		}

		String ruleId = null;
		List<String> expressionList = new ArrayList<String>();
		Map<String, List<String>> contextList = new HashMap<String, List<String>>();

		String administrativeDomain = element.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		List<Element> children = DomHelpers.getChildElements(element);

		for (Element childElement : children) {
			if (super.policyElementMatches(childElement, PolicyStrings.ID_EL_NAME)) {
				ruleId = childElement.getTextContent();
			} else if (super.policyElementMatches(childElement, PolicyStrings.FEATURE_EXPRESSION_EL_NAME)) {
				expressionList.add(super.deEscapeXml(childElement.getTextContent()));
			} else if (super.policyElementMatches(childElement, PolicyStrings.CONTEXT_EL_NAME)) {
				// context elements
				String contextId = null;
				List<String> parameterExpressionList = new ArrayList<String>();

				List<Element> contextChildren = DomHelpers.getChildElements(childElement);

				for (Element contextChildElement : contextChildren) {
					if (super.policyElementMatches(contextChildElement, PolicyStrings.ID_EL_NAME)) {
						contextId = contextChildElement.getTextContent();
					} else if (super.policyElementMatches(contextChildElement,
							PolicyStrings.PARAMETER_EXPRESSION_EL_NAME)) {
						parameterExpressionList.add(super.deEscapeXml(contextChildElement.getTextContent()));
					}
				}

				contextList.put(contextId, parameterExpressionList);
			}
		}

		if (ruleId == null || ruleId.length() == 0) {
			throw new UnmarshalException("No text content for ruleId found");
		}

		Signature signature = new Signature(ruleId, expressionList, administrativeDomain, contextList);

		return signature;
	}

	@Override
	public Class<Signature> handles() {
		return Signature.class;
	}

}
