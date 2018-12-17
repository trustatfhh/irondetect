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
import de.hshannover.f4.trust.irondetect.policy.parser.ParseException;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.handler.PolicyDataManager;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Rule;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.ExtendedIdentifierHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.RuleHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;
import util.DomHelpers;

public class RuleHandlerTest extends AbstractHandlerTest {

	private Element mRuleElement;

	private Rule mRuleIdentfier;

	private ExtendedIdentifierHandler<?> mHandler;

	static {
		// register extended identifier PolicyHandler handler to ifmapJ
		Identifiers.registerIdentifierHandler(new RuleHandler());
	}

	public RuleHandlerTest() throws FileNotFoundException, ParseException {
		super();

		mHandler = new RuleHandler();
	}

	@Before
	public void setUp() throws Exception {
		if (super.mPolicy.getRuleSet().size() != 1) {
			throw new RuntimeException("Only for one Rule!");
		}

		ExtendedIdentifier identfier = PolicyDataManager.transformPolicyData(super.mPolicy.getRuleSet().get(0));

		if (identfier instanceof Rule) {
			mRuleIdentfier = (Rule) identfier;

			mRuleElement = Identifiers.toElement(mRuleIdentfier, super.mDocumentBuilder.newDocument());
		}else{
			throw new RuntimeException("The transformed PolicyData is not the right ExtendedIdentifier type.");
		}
	}

	@Test
	public void TO_ELEMENT_expected_rightElement() throws MarshalException {
		Element ruleElement = mHandler.toExtendedElement(mRuleIdentfier, super.mDocumentBuilder.newDocument());

		String administrativeDomain = ruleElement.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		String identityAdministrativeDomain = mRuleElement.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		String identityOtherTypeDefinition = mRuleElement.getAttribute(IfmapStrings.IDENTITY_ATTR_OTHER_TYPE_DEF);
		String identityType = mRuleElement.getAttribute(IfmapStrings.IDENTITY_ATTR_TYPE);
		String identityName = mRuleElement.getAttribute(IfmapStrings.IDENTITY_ATTR_NAME);

		List<Element> children = DomHelpers.getChildElements(ruleElement);
		List<Element> identityChildren = DomHelpers.getChildElements(mRuleElement);

		// assert extended Identity Element
		assertEquals(IfmapStrings.IDENTITY_EL_NAME, mRuleElement.getLocalName());
		assertEquals(IfmapStrings.OTHER_TYPE_EXTENDED_IDENTIFIER, identityOtherTypeDefinition);
		assertEquals(IdentityType.other.toString(), identityType);
		assertEquals(true, identityName.length() > 0);
		assertEquals(0, identityChildren.size());
		assertEquals("", identityAdministrativeDomain);

		// assert extended Element
		assertEquals(PolicyStrings.RULE_EL_NAME, ruleElement.getLocalName());
		assertEquals(1, children.size());
		assertEquals(PolicyStrings.ID_EL_NAME, children.get(0).getLocalName());
		assertEquals("TestRule1", children.get(0).getTextContent());
		assertEquals(PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN, administrativeDomain);
		assertEquals(PolicyStrings.POLICY_IDENTIFIER_NS_URI, ruleElement.getNamespaceURI());
	}

	@Test
	public void FROM_ELEMENT_expected_rightExtendedIdentifier() throws UnmarshalException {
		IdentifierHandler<?> ih = Identifiers.getHandlerFor(mRuleElement);

		Identifier identifier = ih.fromElement(mRuleElement);

		assertEquals(Rule.class, identifier.getClass());
		assertEquals("TestRule1", ((Rule) identifier).getID());
		assertEquals(PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN, ((Rule) identifier).getAdministrativeDomain());
	}
}
