package de.hshannover.f4.trust.irondetect.policy.publisher.identifier.handler;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.IdentifierHandler;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.irondetect.policy.parser.ParseException;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.handler.PolicyDataManager;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Policy;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.PolicyHandler;

public class PolicyHandlerTest extends AbstractHandlerTest {

	private Element mPolicyElement;

	// register extended identifier PolicyHandler handler to ifmapJ
	static {
		Identifiers.registerIdentifierHandler(new PolicyHandler());
	}

	public PolicyHandlerTest() throws FileNotFoundException, ParseException {
		super();
	}

	@Before
	public void setUp() throws Exception {
		ExtendedIdentifier policyIdentifier = PolicyDataManager.transformPolicyData(mPolicy);

		if(policyIdentifier instanceof Policy){
			mPolicyElement = Identifiers.toElement(policyIdentifier, mDocumentBuilder.newDocument());
		}else{
			throw new RuntimeException("The transformed PolicyData is not the right ExtendedIdentifier type.");
		}
	}

	@After
	public void tearDown() {

	}

	@Test
	public void expected_rightExtendedIdentifier() throws UnmarshalException {
		IdentifierHandler<?> ih = Identifiers.getHandlerFor(Policy.class);

		Identifier identifier = ih.fromElement(mPolicyElement);

		assertEquals(Policy.class, identifier.getClass());
		assertEquals("src/test/resources/PolicyHandlerTest.pol", ((Policy)identifier).getID());

	}
}
