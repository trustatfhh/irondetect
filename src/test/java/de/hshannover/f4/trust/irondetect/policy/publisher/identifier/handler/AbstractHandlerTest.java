package de.hshannover.f4.trust.irondetect.policy.publisher.identifier.handler;

import java.io.FileNotFoundException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.hshannover.f4.trust.ifmapj.log.IfmapJLog;
import de.hshannover.f4.trust.irondetect.model.Policy;
import de.hshannover.f4.trust.irondetect.policy.parser.ParseException;
import de.hshannover.f4.trust.irondetect.policy.parser.PolicyFactory;

public abstract class AbstractHandlerTest {

	protected Policy mPolicy;

	protected DocumentBuilder mDocumentBuilder;

	public AbstractHandlerTest() throws FileNotFoundException, ParseException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			mDocumentBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IfmapJLog.error("Could not get DocumentBuilder instance [" + e.getMessage() + "]");
			throw new RuntimeException(e);
		}

		mPolicy = PolicyFactory.readPolicy("src/test/resources/PolicyHandlerTest.pol");
	}

}
