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
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Action;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.ActionHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.ExtendedIdentifierHandler;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;
import util.DomHelpers;

public class ActionHandlerTest extends AbstractHandlerTest {

	private Element mActionElement;

	private Action mActionIdentfier;

	private ExtendedIdentifierHandler<?> mHandler;

	static {
		// register extended identifier PolicyHandler handler to ifmapJ
		Identifiers.registerIdentifierHandler(new ActionHandler());
	}

	public ActionHandlerTest() throws FileNotFoundException, ParseException {
		super();

		mHandler = new ActionHandler();
	}

	@Before
	public void setUp() throws Exception {
		if (super.mPolicy.getRuleSet().get(0).getActions().size() != 1) {
			throw new RuntimeException("Only for one Action!");
		}

		ExtendedIdentifier identfier = PolicyDataManager.transformPolicyData(
				super.mPolicy.getRuleSet().get(0).getActions().get(0));

		if (identfier instanceof Action) {
			mActionIdentfier = (Action) identfier;

			mActionElement = Identifiers.toElement(mActionIdentfier, super.mDocumentBuilder.newDocument());
		}else{
			throw new RuntimeException("The transformed PolicyData is not the right ExtendedIdentifier type.");
		}
	}

	@Test
	public void TO_ELEMENT_expected_rightElement() throws MarshalException {
		Element policyElement = mHandler.toExtendedElement(mActionIdentfier, super.mDocumentBuilder.newDocument());

		String administrativeDomain = policyElement.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		String identityAdministrativeDomain = mActionElement.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		String identityOtherTypeDefinition = mActionElement.getAttribute(IfmapStrings.IDENTITY_ATTR_OTHER_TYPE_DEF);
		String identityType = mActionElement.getAttribute(IfmapStrings.IDENTITY_ATTR_TYPE);
		String identityName = mActionElement.getAttribute(IfmapStrings.IDENTITY_ATTR_NAME);

		List<Element> children = DomHelpers.getChildElements(policyElement);
		List<Element> identityChildren = DomHelpers.getChildElements(mActionElement);

		// assert extended Identity Element
		assertEquals(IfmapStrings.IDENTITY_EL_NAME, mActionElement.getLocalName());
		assertEquals(IfmapStrings.OTHER_TYPE_EXTENDED_IDENTIFIER, identityOtherTypeDefinition);
		assertEquals(IdentityType.other.toString(), identityType);
		assertEquals(true, identityName.length() > 0);
		assertEquals(0, identityChildren.size());
		assertEquals("", identityAdministrativeDomain);

		// assert extended Element
		assertEquals(PolicyStrings.ACTION_EL_NAME, policyElement.getLocalName());
		assertEquals(2, children.size());
		assertEquals(PolicyStrings.ID_EL_NAME, children.get(0).getLocalName());
		assertEquals("TestAction1", children.get(0).getTextContent());
		assertEquals(PolicyStrings.OPERATION_EL_NAME, children.get(1).getLocalName());
		assertEquals("Test Action1", children.get(1).getTextContent());
		assertEquals(PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN, administrativeDomain);
		assertEquals(PolicyStrings.POLICY_IDENTIFIER_NS_URI, policyElement.getNamespaceURI());
	}

	@Test
	public void FROM_ELEMENT_expected_rightExtendedIdentifier() throws UnmarshalException {
		IdentifierHandler<?> ih = Identifiers.getHandlerFor(mActionElement);

		Identifier identifier = ih.fromElement(mActionElement);

		assertEquals(Action.class, identifier.getClass());
		assertEquals("TestAction1", ((Action) identifier).getID());
		assertEquals(1, ((Action) identifier).getExpressions().size());
		assertEquals("Test Action1", ((Action) identifier).getExpressions().get(0));
		assertEquals(PolicyStrings.DEFAULT_ADMINISTRATIVE_DOMAIN, ((Action) identifier).getAdministrativeDomain());
	}
}
