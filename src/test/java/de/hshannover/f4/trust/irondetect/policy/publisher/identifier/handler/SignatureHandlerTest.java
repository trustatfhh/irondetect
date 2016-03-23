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
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Signature;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.ExtendedIdentifierHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.SignatureHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;
import util.DomHelpers;

public class SignatureHandlerTest extends AbstractHandlerTest {

	private Element mSignatureElement;

	private Signature mSignatureIdentfier;

	private ExtendedIdentifierHandler<?> mHandler;

	static {
		// register extended identifier PolicyHandler handler to ifmapJ
		Identifiers.registerIdentifierHandler(new SignatureHandler());
	}

	public SignatureHandlerTest() throws FileNotFoundException, ParseException {
		super();

		mHandler = new SignatureHandler();
	}

	@Before
	public void setUp() throws Exception {
		ExtendedIdentifier identfier = PolicyDataManager.transformPolicyData(
				super.mPolicy.getRuleSet().get(0).getCondition().getConditionSet().get(0).getFirstElement());

		if (identfier instanceof Signature) {
			mSignatureIdentfier = (Signature) identfier;

			mSignatureElement = Identifiers.toElement(mSignatureIdentfier, super.mDocumentBuilder.newDocument());
		}else{
			throw new RuntimeException("The transformed PolicyData is not the right ExtendedIdentifier type.");
		}
	}

	@Test
	public void TO_ELEMENT_expected_rightElement() throws MarshalException {
		Element policyElement = mHandler.toExtendedElement(mSignatureIdentfier, super.mDocumentBuilder.newDocument());

		String administrativeDomain = policyElement.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		String identityAdministrativeDomain = mSignatureElement.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		String identityOtherTypeDefinition = mSignatureElement.getAttribute(IfmapStrings.IDENTITY_ATTR_OTHER_TYPE_DEF);
		String identityType = mSignatureElement.getAttribute(IfmapStrings.IDENTITY_ATTR_TYPE);
		String identityName = mSignatureElement.getAttribute(IfmapStrings.IDENTITY_ATTR_NAME);

		List<Element> children = DomHelpers.getChildElements(policyElement);
		List<Element> identityChildren = DomHelpers.getChildElements(mSignatureElement);

		// assert extended Identity Element
		assertEquals(IfmapStrings.IDENTITY_EL_NAME, mSignatureElement.getLocalName());
		assertEquals(IfmapStrings.OTHER_TYPE_EXTENDED_IDENTIFIER, identityOtherTypeDefinition);
		assertEquals(IdentityType.other.toString(), identityType);
		assertEquals(true, identityName.length() > 0);
		assertEquals(0, identityChildren.size());
		assertEquals("", identityAdministrativeDomain);

		// assert extended Element
		assertEquals(PolicyStrings.SIGNATURE_EL_NAME, policyElement.getLocalName());
		assertEquals(3, children.size());
		assertEquals(PolicyStrings.ID_EL_NAME, children.get(0).getLocalName());
		assertEquals("TestSignature1", children.get(0).getTextContent());
		assertEquals(PolicyStrings.FEATURE_EXPRESSION_EL_NAME, children.get(1).getLocalName());
		assertEquals("smartphone.android.app.Name = Test", children.get(1).getTextContent());
		assertEquals(PolicyStrings.FEATURE_EXPRESSION_EL_NAME, children.get(2).getLocalName());
		assertEquals("AND smartphone.android.app.IsRunning = true", children.get(2).getTextContent());
		assertEquals(PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN, administrativeDomain);
		assertEquals(PolicyStrings.POLICY_IDENTIFIER_NS_URI, policyElement.getNamespaceURI());
	}

	@Test
	public void FROM_ELEMENT_expected_rightExtendedIdentifier() throws UnmarshalException {
		IdentifierHandler<?> ih = Identifiers.getHandlerFor(mSignatureElement);

		Identifier identifier = ih.fromElement(mSignatureElement);

		assertEquals(Signature.class, identifier.getClass());
		assertEquals("TestSignature1", ((Signature) identifier).getID());
		assertEquals(2, ((Signature) identifier).getExpressions().size());
		assertEquals("smartphone.android.app.Name = Test", ((Signature) identifier).getExpressions().get(0));
		assertEquals("AND smartphone.android.app.IsRunning = true", ((Signature) identifier).getExpressions().get(1));
		assertEquals(PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN, ((Signature) identifier).getAdministrativeDomain());
	}
}
