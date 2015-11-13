package de.hshannover.f4.trust.irondetect.policy.publisher.identifier.handler;

import static org.junit.Assert.assertEquals;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.IdentifierHandler;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.log.IfmapJLog;
import de.hshannover.f4.trust.irondetect.policy.parser.PolicyFactory;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.handler.PolicyDataManager;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Policy;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.PolicyHandler;

public class PolicyHandlerTest {

	private de.hshannover.f4.trust.irondetect.model.Policy mPolicy;

	private DocumentBuilder mDocumentBuilder;

	private Element mPolicyElement;

	// register extended identifier PolicyHandler handler to ifmapJ
	static {
		Identifiers.registerIdentifierHandler(new PolicyHandler());
	}

	public PolicyHandlerTest() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			mDocumentBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IfmapJLog.error("Could not get DocumentBuilder instance [" + e.getMessage() + "]");
			throw new RuntimeException(e);
		}
	}

	@Before
	public void setUp() throws Exception {
		mPolicy = PolicyFactory.readPolicy("src/test/resources/MobileDeviceSzenario.pol");

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
		assertEquals("src/test/resources/MobileDeviceSzenario.pol", ((Policy)identifier).getID());

	}
}
