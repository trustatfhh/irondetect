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
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Condition;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.ConditionHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.ExtendedIdentifierHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;
import util.DomHelpers;

public class ConditionHandlerTest extends AbstractHandlerTest {

	private Element mConditionElement;

	private Condition mConditionIdentfier;

	private ExtendedIdentifierHandler<?> mHandler;

	static {
		// register extended identifier PolicyHandler handler to ifmapJ
		Identifiers.registerIdentifierHandler(new ConditionHandler());
	}

	public ConditionHandlerTest() throws FileNotFoundException, ParseException {
		super();

		mHandler = new ConditionHandler();
	}

	@Before
	public void setUp() throws Exception {
		ExtendedIdentifier identfier = PolicyDataManager.transformPolicyData(
				super.mPolicy.getRuleSet().get(0).getCondition());

		if (identfier instanceof Condition) {
			mConditionIdentfier = (Condition) identfier;

			mConditionElement = Identifiers.toElement(mConditionIdentfier, super.mDocumentBuilder.newDocument());
		}else{
			throw new RuntimeException("The transformed PolicyData is not the right ExtendedIdentifier type.");
		}
	}

	@Test
	public void TO_ELEMENT_expected_rightElement() throws MarshalException {
		Element policyElement = mHandler.toExtendedElement(mConditionIdentfier, super.mDocumentBuilder.newDocument());

		String administrativeDomain = policyElement.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		String identityAdministrativeDomain = mConditionElement.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		String identityOtherTypeDefinition = mConditionElement.getAttribute(IfmapStrings.IDENTITY_ATTR_OTHER_TYPE_DEF);
		String identityType = mConditionElement.getAttribute(IfmapStrings.IDENTITY_ATTR_TYPE);
		String identityName = mConditionElement.getAttribute(IfmapStrings.IDENTITY_ATTR_NAME);

		List<Element> children = DomHelpers.getChildElements(policyElement);
		List<Element> identityChildren = DomHelpers.getChildElements(mConditionElement);

		// assert extended Identity Element
		assertEquals(IfmapStrings.IDENTITY_EL_NAME, mConditionElement.getLocalName());
		assertEquals(IfmapStrings.OTHER_TYPE_EXTENDED_IDENTIFIER, identityOtherTypeDefinition);
		assertEquals(IdentityType.other.toString(), identityType);
		assertEquals(true, identityName.length() > 0);
		assertEquals(0, identityChildren.size());
		assertEquals("", identityAdministrativeDomain);

		// assert extended Element
		assertEquals(PolicyStrings.CONDITION_EL_NAME, policyElement.getLocalName());
		assertEquals(3, children.size());
		assertEquals(PolicyStrings.ID_EL_NAME, children.get(0).getLocalName());
		assertEquals("TestCondition1", children.get(0).getTextContent());
		assertEquals(PolicyStrings.CONDITION_EXPRESSION_EL_NAME, children.get(1).getLocalName());
		assertEquals("TestSignature1", children.get(1).getTextContent());
		assertEquals(PolicyStrings.CONDITION_EXPRESSION_EL_NAME, children.get(2).getLocalName());
		assertEquals("AND TestAnomaly1", children.get(2).getTextContent());
		assertEquals(PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN, administrativeDomain);
		assertEquals(PolicyStrings.POLICY_IDENTIFIER_NS_URI, policyElement.getNamespaceURI());
	}

	@Test
	public void FROM_ELEMENT_expected_rightExtendedIdentifier() throws UnmarshalException {
		IdentifierHandler<?> ih = Identifiers.getHandlerFor(mConditionElement);

		Identifier identifier = ih.fromElement(mConditionElement);

		assertEquals(Condition.class, identifier.getClass());
		assertEquals("TestCondition1", ((Condition) identifier).getID());
		assertEquals(2, ((Condition) identifier).getExpressions().size());
		assertEquals("TestSignature1", ((Condition) identifier).getExpressions().get(0));
		assertEquals("AND TestAnomaly1", ((Condition) identifier).getExpressions().get(1));
		assertEquals(PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN, ((Condition) identifier).getAdministrativeDomain());
	}
}
