package de.hshannover.f4.trust.irondetect.policy.publisher;

import static de.hshannover.f4.trust.irondetect.util.MetadataMock.TYPE_ARBITRARY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.Identity;
import de.hshannover.f4.trust.ifmapj.identifier.IdentityType;
import de.hshannover.f4.trust.ifmapj.messages.PollResult;
import de.hshannover.f4.trust.ifmapj.messages.SearchResult.Type;
import de.hshannover.f4.trust.irondetect.model.Policy;
import de.hshannover.f4.trust.irondetect.policy.parser.PolicyFactory;
import de.hshannover.f4.trust.irondetect.util.IfmapjMock;

public class PolicyFeatureUpdaterTest extends IfmapjMock {

	private static final Logger mLog = Logger.getLogger(PolicyFeatureUpdaterTest.class);

	private static final String OTHER_TYPE_DEFINITION = "32939:category";

	private static final String DEVICE = "WindowsPhone";

	private static final String SUB1 = "SubscriptionName1";

	public static final String CATEGORY_APP = "smartphone.android.app";

	public static final String CATEGORY_APP_0_PERMISSION = "smartphone.android.app:0.permission";

	public static final String APP_FACEBOOK = "Facebook";

	public static final String APP_IS_RUNNING = "smartphone.android.app.IsRunning";

	public static final String APP_NAME = "smartphone.android.app.Name";

	public static final String PERMISSION_NAME = "smartphone.android.app.permission.Name";

	public static final String APP_PERMISSION_INTERNET = "INTERNET";

	public static final String APP_PERMISSION_SEND_SMS = "SEND_SMS";

	public static final String APP_PERMISSION_ACCESS_NETWORK_STATE = "ACCESS_NETWORK_STATE";

	public static final String APP_PERMISSION_ACCESS_COARSE_LOCATION = "ACCESS_COARSE_LOCATION";

	private static final Date FIRST_TIMESTAMP = new Date(3333);

	private static final Date SECOND_TIMESTAMP = new Date(5555);

	private static final Date THIRD_TIMESTAMP = new Date(8888);

	private PolicyFeatureUpdater mPolicyFeatureUpdater;

	private Policy mPolicy;

	@Before
	public void setUp() throws Exception {
		mPolicy = PolicyFactory.readPolicy("src/test/resources/MobileDeviceSzenario.pol");

		mPolicyFeatureUpdater = mock(PolicyFeatureUpdater.class, Mockito.CALLS_REAL_METHODS);
		doNothing().when(mPolicyFeatureUpdater).sendPublishUpdate();

		mPolicyFeatureUpdater.init(mPolicy, null);
	}

	@After
	public void tearDown() {
		// nothing
	}

	@Test
	public void expected_TwoPolicyFeature() {
		mPolicyFeatureUpdater.submitNewPollResult(buildFirstPollResult());
		
		try {
			mPolicyFeatureUpdater.newPollResult();
		} catch (InterruptedException e) {
			mLog.error(e.getClass().getSimpleName());
		}

		assertNotNull(mPolicyFeatureUpdater.mPublishUpdates);
		assertEquals(2, mPolicyFeatureUpdater.mPublishUpdates.size());
	}

	private PollResult buildFirstPollResult() {
		return PollResultMock(SearchResultMock(SUB1, Type.updateResult,
				ResultItemMock(
						CreateCategory(CATEGORY_APP + ":0", DEVICE),
						CreateEsukomFeature(APP_NAME, TYPE_ARBITRARY, APP_FACEBOOK, FIRST_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP + ":0", DEVICE),
						CreateEsukomFeature(APP_IS_RUNNING, TYPE_ARBITRARY, "true", FIRST_TIMESTAMP))));
	}

	@Test
	public void expected_OnePolicyFeature() {
		mPolicyFeatureUpdater.submitNewPollResult(buildSecondPollResult());

		try {
			mPolicyFeatureUpdater.newPollResult();
		} catch (InterruptedException e) {
			mLog.error(e.getClass().getSimpleName());
		}

		assertNotNull(mPolicyFeatureUpdater.mPublishUpdates);
		assertEquals(1, mPolicyFeatureUpdater.mPublishUpdates.size());
	}

	private PollResult buildSecondPollResult() {
		return PollResultMock(SearchResultMock(SUB1, Type.updateResult,
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":0", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY, APP_PERMISSION_INTERNET,
								SECOND_TIMESTAMP))));
	}

	@Test
	public void expected_FourPolicyFeature() {
		mPolicyFeatureUpdater.submitNewPollResult(buildThirdPollResult());

		try {
			mPolicyFeatureUpdater.newPollResult();
		} catch (InterruptedException e) {
			mLog.error(e.getClass().getSimpleName());
		}

		assertNotNull(mPolicyFeatureUpdater.mPublishUpdates);
		assertEquals(4, mPolicyFeatureUpdater.mPublishUpdates.size());
	}

	private PollResult buildThirdPollResult() {
		return PollResultMock(SearchResultMock(SUB1, Type.updateResult,
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":0", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY,
								APP_PERMISSION_ACCESS_COARSE_LOCATION, SECOND_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":1", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY,
								APP_PERMISSION_ACCESS_NETWORK_STATE, SECOND_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":2", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY, APP_PERMISSION_INTERNET,
								SECOND_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":3", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY, APP_PERMISSION_SEND_SMS,
								SECOND_TIMESTAMP))));
	}

	@Test
	public void expected_NoPolicyFeature() {
		mPolicyFeatureUpdater.submitNewPollResult(buildFourthPollResult());

		try {
			mPolicyFeatureUpdater.newPollResult();
		} catch (InterruptedException e) {
			mLog.error(e.getClass().getSimpleName());
		}

		assertNotNull(mPolicyFeatureUpdater.mPublishUpdates);
		assertEquals(0, mPolicyFeatureUpdater.mPublishUpdates.size());
	}

	private PollResult buildFourthPollResult() {
		return PollResultMock(
				SearchResultMock(SUB1 + "_2", Type.updateResult,
						ResultItemMock(
								Identifiers.createAr("ACCESS_REQUEST"),
								CreateCapability("CAP1", SECOND_TIMESTAMP)),
						ResultItemMock(
								Identifiers.createAr("ACCESS_REQUEST"),
								Identifiers.createMac("MAC_TEST3"),
								CreateArMac(SECOND_TIMESTAMP)),
						ResultItemMock(
								Identifiers.createAr("ACCESS_REQUEST"),
								Identifiers.createMac("MAC_TEST2"),
								CreateArMac(THIRD_TIMESTAMP))),
				SearchResultMock(SUB1, Type.updateResult,
						ResultItemMock(
								Identifiers.createAr("ACCESS_REQUEST"),
								Identifiers.createMac("MAC_TEST"),
								CreateArMac(THIRD_TIMESTAMP))));
	}

	@Test
	public void expected_OnePolicyFeature2() {
		mPolicyFeatureUpdater.submitNewPollResult(buildFifthPollResult());

		try {
			mPolicyFeatureUpdater.newPollResult();
		} catch (InterruptedException e) {
			mLog.error(e.getClass().getSimpleName());
		}

		assertNotNull(mPolicyFeatureUpdater.mPublishUpdates);
		assertEquals(1, mPolicyFeatureUpdater.mPublishUpdates.size());
	}

	private PollResult buildFifthPollResult() {
		return PollResultMock(SearchResultMock(SUB1, Type.updateResult,
				ResultItemMock(
						CreateCategory(CATEGORY_APP + ":0", DEVICE),
						CreateEsukomFeature(APP_NAME, TYPE_ARBITRARY, "Not in Policy", FIRST_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP + ":0", DEVICE),
						CreateEsukomFeature(APP_IS_RUNNING, TYPE_ARBITRARY, "true", FIRST_TIMESTAMP))));
	}

	public static Document CreateEsukomFeature(String id, String type, String value, Date timestamp) {
		return CreateEsukomFeature("TODO", "TODO", timestamp, id, type, value, timestamp);
	}

	private static Identity CreateCategory(String name, String admDomain) {
		return Identifiers.createIdentity(
				IdentityType.other,
				name,
				admDomain,
				OTHER_TYPE_DEFINITION);
	}
}
