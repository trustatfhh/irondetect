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
package de.hshannover.f4.trust.irondetect.policy.publisher.identifier.handler;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import de.hshannover.f4.trust.ifmapj.binding.IfmapStrings;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.IdentifierHandler;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.IdentityType;
import de.hshannover.f4.trust.irondetect.model.Anomaly;
import de.hshannover.f4.trust.irondetect.model.ConditionElement;
import de.hshannover.f4.trust.irondetect.policy.parser.ParseException;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.handler.PolicyDataManager;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Hint;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.ExtendedIdentifierHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.HintHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;
import util.DomHelpers;

public class HintHandlerTest extends AbstractHandlerTest {

	private Element mHintElement;

	private Hint mHintIdentfier;

	private ExtendedIdentifierHandler<?> mHandler;

	static {
		// register extended identifier PolicyHandler handler to ifmapJ
		Identifiers.registerIdentifierHandler(new HintHandler());
	}

	public HintHandlerTest() throws FileNotFoundException, ParseException {
		super();

		mHandler = new HintHandler();
	}

	@Before
	public void setUp() throws Exception {
		ConditionElement conditionElement = super.mPolicy.getRuleSet().get(0).getCondition().getConditionSet().get(1).getFirstElement();

		ExtendedIdentifier identfier = PolicyDataManager.transformPolicyData(
				((Anomaly) conditionElement).getHintSet().get(0).getFirstElement().getHintValuePair()
				.getFirstElement());

		if (identfier instanceof Hint) {
			mHintIdentfier = (Hint) identfier;

			mHintElement = Identifiers.toElement(mHintIdentfier, super.mDocumentBuilder.newDocument());
		}else{
			throw new RuntimeException("The transformed PolicyData is not the right ExtendedIdentifier type.");
		}
	}

	@Test
	public void TO_ELEMENT_expected_rightElement() throws MarshalException {
		Element policyElement = mHandler.toExtendedElement(mHintIdentfier, super.mDocumentBuilder.newDocument());

		String administrativeDomain = policyElement.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		String identityAdministrativeDomain = mHintElement.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		String identityOtherTypeDefinition = mHintElement.getAttribute(IfmapStrings.IDENTITY_ATTR_OTHER_TYPE_DEF);
		String identityType = mHintElement.getAttribute(IfmapStrings.IDENTITY_ATTR_TYPE);
		String identityName = mHintElement.getAttribute(IfmapStrings.IDENTITY_ATTR_NAME);

		List<Element> children = DomHelpers.getChildElements(policyElement);
		List<Element> identityChildren = DomHelpers.getChildElements(mHintElement);

		// assert extended Identity Element
		assertEquals(IfmapStrings.IDENTITY_EL_NAME, mHintElement.getLocalName());
		assertEquals(IfmapStrings.OTHER_TYPE_EXTENDED_IDENTIFIER, identityOtherTypeDefinition);
		assertEquals(IdentityType.other.toString(), identityType);
		assertEquals(true, identityName.length() > 0);
		assertEquals(0, identityChildren.size());
		assertEquals("", identityAdministrativeDomain);

		// assert extended Element
		assertEquals(PolicyStrings.HINT_EL_NAME, policyElement.getLocalName());
		assertEquals(2, children.size());
		assertEquals(PolicyStrings.ID_EL_NAME, children.get(0).getLocalName());
		assertEquals("TestHint1", children.get(0).getTextContent());
		assertEquals(PolicyStrings.PROCEDURE_EXPRESSION_EL_NAME, children.get(1).getLocalName());
		assertEquals("smartphone.communication.sms.SentCount de.hshannover.f4.trust.irondetectprocedures.Mean 10",
				children.get(1).getTextContent());
		assertEquals(PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN, administrativeDomain);
		assertEquals(PolicyStrings.POLICY_IDENTIFIER_NS_URI, policyElement.getNamespaceURI());
	}

	@Test
	public void FROM_ELEMENT_expected_rightExtendedIdentifier() throws UnmarshalException {
		IdentifierHandler<?> ih = Identifiers.getHandlerFor(mHintElement);

		Identifier identifier = ih.fromElement(mHintElement);

		assertEquals(Hint.class, identifier.getClass());
		assertEquals("TestHint1", ((Hint) identifier).getID());
		assertEquals(1, ((Hint) identifier).getExpressions().size());
		assertEquals("smartphone.communication.sms.SentCount de.hshannover.f4.trust.irondetectprocedures.Mean 10",
				((Hint) identifier).getExpressions().get(0));
		assertEquals(PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN, ((Hint) identifier).getAdministrativeDomain());
	}
}
