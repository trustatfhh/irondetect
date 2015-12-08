package de.hshannover.f4.trust.irondetect.policy.publisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.irondetect.model.Policy;
import de.hshannover.f4.trust.irondetect.policy.parser.PolicyFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Identifiers.class)
public class PolicyPublisherTest {

	private static final Logger mLog = Logger.getLogger(PolicyPublisherTest.class);

	private Policy mPolicy;

	private PolicyPublisher mPolicyPublisher;

	@Before
	public void setUp() throws Exception {

		PowerMockito.mockStatic(Identifiers.class);
		PowerMockito.doNothing().when(Identifiers.class, "registerIdentifierHandler", any());

		mPolicy = PolicyFactory.readPolicy("src/test/resources/MobileDeviceSzenario.pol");

		mPolicyPublisher = mock(PolicyPublisher.class);
		mPolicyPublisher.mPolicy = mPolicy;
		doCallRealMethod().when(mPolicyPublisher).buildPublishUpdate();
		doCallRealMethod().when(mPolicyPublisher).addPublishUpdate(any(Identifier.class), any(Document.class),
				any(Identifier.class));
	}

	@After
	public void tearDown() {

	}

	@Test
	public void expected_rightPublishUpdatesSize() {

		try {
			mPolicyPublisher.buildPublishUpdate();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			mLog.error(e.getClass().getSimpleName(), e);
		}

		assertNotNull(mPolicyPublisher.mPublishUpdates);
		assertEquals(12, mPolicyPublisher.mPublishUpdates.size());
	}
}
