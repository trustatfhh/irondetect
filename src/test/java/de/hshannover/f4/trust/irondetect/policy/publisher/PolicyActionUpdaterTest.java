package de.hshannover.f4.trust.irondetect.policy.publisher;

import static de.hshannover.f4.trust.irondetect.util.MetadataMock.TYPE_ARBITRARY;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.Identity;
import de.hshannover.f4.trust.ifmapj.identifier.IdentityType;
import de.hshannover.f4.trust.ifmapj.messages.PollResult;
import de.hshannover.f4.trust.ifmapj.messages.SearchResult.Type;
import de.hshannover.f4.trust.irondetect.gui.ResultObject;
import de.hshannover.f4.trust.irondetect.gui.ResultObjectType;
import de.hshannover.f4.trust.irondetect.model.Policy;
import de.hshannover.f4.trust.irondetect.policy.parser.PolicyFactory;
import de.hshannover.f4.trust.irondetect.util.FeatureCountPairMatcher;
import de.hshannover.f4.trust.irondetect.util.IfmapjMock;
import de.hshannover.f4.trust.irondetect.util.PolicyActionListMatcher;
import de.hshannover.f4.trust.irondetect.util.event.Event;
import de.hshannover.f4.trust.irondetect.util.event.ResultUpdateEvent;

public class PolicyActionUpdaterTest extends IfmapjMock {

	private static final Logger mLog = Logger.getLogger(PolicyActionUpdaterTest.class);

	private static final String POLICY_File_LOCATION = "src/test/resources/MobileDeviceSzenario.pol";

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

	private PolicyActionUpdater mPolicyActionUpdater;

	private PolicyActionSearcher mPolicyActionSearcher;

	private Policy mPolicy;

	@Before
	public void setUp() throws Exception {
		mPolicy = PolicyFactory.readPolicy(POLICY_File_LOCATION);

		mPolicyActionSearcher = mock(PolicyActionSearcher.class);
		
		mPolicyActionUpdater = mock(PolicyActionUpdater.class, Mockito.CALLS_REAL_METHODS);
		doNothing().when(mPolicyActionUpdater).sendPublishUpdate(anyList());

		mPolicyActionUpdater.init(mPolicy, null, mPolicyActionSearcher);
	}

	@After
	public void tearDown() {
		// nothing
	}

	private ResultObject mockResultObject(String device, String id, ResultObjectType type, boolean value) {
		ResultObject result = mock(ResultObject.class);
		when(result.getDevice()).thenReturn(device);
		when(result.getId()).thenReturn(id);
		when(result.getType()).thenReturn(type);
		when(result.getValue()).thenReturn(value);
		return result;
	}

	private Event builResult(String device, String id, ResultObjectType type, boolean value) {
		ResultObject result = mockResultObject(device, id, type, value);
		ResultUpdateEvent event = new ResultUpdateEvent(result);
		return event;
	}

	private Event builPolicyResult(String device, String id, boolean value) {
		return builResult(device, id, ResultObjectType.POLICY, value);
	}

	private Event builRuleResult(String device, String id, boolean value) {
		return builResult(device, id, ResultObjectType.RULE, value);
	}

	private Event builSignatureResult(String device, String id, boolean value) {
		return builResult(device, id, ResultObjectType.SIGNATURE, value);
	}

	private Event builAnomalyResult(String device, String id, boolean value) {
		return builResult(device, id, ResultObjectType.ANOMALY, value);
	}

	private Event builHintResult(String device, String id, boolean value) {
		return builResult(device, id, ResultObjectType.HINT, value);
	}

	@Test
	public void expected_OneSubmitNewPolicyActionCallWhithTwoFeatures() throws IfmapErrorResult, IfmapException {
		Event policyEvent = builPolicyResult(DEVICE, POLICY_File_LOCATION, true);
		Event ruleEvent = builRuleResult(DEVICE, "FacebookAPPAn", true);
		Event signatureEvent = builSignatureResult(DEVICE, "sigFacebookAPPAn", true);

		mPolicyActionUpdater.submitNewPollResult(buildPollResultWithTwoFeatures());

		mPolicyActionUpdater.submitNewEvent(signatureEvent);
		mPolicyActionUpdater.submitNewEvent(ruleEvent);
		mPolicyActionUpdater.submitNewEvent(policyEvent);

		try {
			mPolicyActionUpdater.newPollResult();
		} catch (InterruptedException e) {
			mLog.error(e.getClass().getSimpleName());
		}

		verify(mPolicyActionSearcher, times(1)).submitNewPolicyAction(argThat(new FeatureCountPairMatcher(2)));
		verify(mPolicyActionUpdater, never()).sendPublishUpdate(anyList());

	}

	private PollResult buildPollResultWithTwoFeatures() {
		return PollResultMock(SearchResultMock(SUB1, Type.updateResult,
				ResultItemMock(
						CreateCategory(CATEGORY_APP + ":0", DEVICE),
						CreateEsukomFeature(APP_NAME, TYPE_ARBITRARY, APP_FACEBOOK, FIRST_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP + ":0", DEVICE),
						CreateEsukomFeature(APP_IS_RUNNING, TYPE_ARBITRARY, "true", FIRST_TIMESTAMP))));
	}
	
	@Test
	public void expected_OneSendPublishUpdateCallWhithTwoFeatures() throws IfmapErrorResult,
			IfmapException {
		Event policyEvent = builPolicyResult(DEVICE, POLICY_File_LOCATION, true);
		Event ruleEvent = builRuleResult(DEVICE, "FacebookAPPAn", false);
		Event signatureEvent = builSignatureResult(DEVICE, "sigFacebookAPPAn", false);

		mPolicyActionUpdater.submitNewPollResult(buildPollResultWithTwoFeatures_IS_RUNNING_FALSE());

		mPolicyActionUpdater.submitNewEvent(signatureEvent);
		mPolicyActionUpdater.submitNewEvent(ruleEvent);
		mPolicyActionUpdater.submitNewEvent(policyEvent);

		try {
			mPolicyActionUpdater.newPollResult();
		} catch (InterruptedException e) {
			mLog.error(e.getClass().getSimpleName());
		}

		verify(mPolicyActionSearcher, never()).submitNewPolicyAction(argThat(new FeatureCountPairMatcher(0)));
		verify(mPolicyActionUpdater, times(1)).sendPublishUpdate(argThat(new PolicyActionListMatcher(1, 1, 2)));

	}

	private PollResult buildPollResultWithTwoFeatures_IS_RUNNING_FALSE() {
		return PollResultMock(SearchResultMock(SUB1, Type.updateResult,
				ResultItemMock(
						CreateCategory(CATEGORY_APP + ":0", DEVICE),
						CreateEsukomFeature(APP_NAME, TYPE_ARBITRARY, APP_FACEBOOK, FIRST_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP + ":0", DEVICE),
						CreateEsukomFeature(APP_IS_RUNNING, TYPE_ARBITRARY, "false", FIRST_TIMESTAMP))));
	}

	@Test
	public void expected_OneSubmitNewPolicyActionCallWhithOneFeature() throws IfmapErrorResult, IfmapException {
		Event policyEvent = builPolicyResult(DEVICE, POLICY_File_LOCATION, true);
		Event ruleEvent = builRuleResult(DEVICE, "unknownAPP", true);
		Event signatureEvent = builSignatureResult(DEVICE, "sigPermissionINTERNET", true);

		mPolicyActionUpdater.submitNewPollResult(buildPollResultWithOneFeature());
		
		mPolicyActionUpdater.submitNewEvent(signatureEvent);
		mPolicyActionUpdater.submitNewEvent(ruleEvent);
		mPolicyActionUpdater.submitNewEvent(policyEvent);

		try {
			mPolicyActionUpdater.newPollResult();
		} catch (InterruptedException e) {
			mLog.error(e.getClass().getSimpleName());
		}

		verify(mPolicyActionSearcher, times(1)).submitNewPolicyAction(argThat(new FeatureCountPairMatcher(1)));
		verify(mPolicyActionUpdater, never()).sendPublishUpdate(anyList());

	}
	
	private PollResult buildPollResultWithOneFeature() {
		return PollResultMock(SearchResultMock(SUB1, Type.updateResult,
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":0", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY, APP_PERMISSION_INTERNET,
								SECOND_TIMESTAMP))));
	}

	@Test
	public void expected_OneSendPublishUpdateCallWhithNoFeatures() throws IfmapErrorResult, IfmapException {
		Event policyEvent = builPolicyResult(DEVICE, POLICY_File_LOCATION, true);
		Event ruleEvent = builRuleResult(DEVICE, "unknownAPP", false);
		Event internetEvent = builSignatureResult(DEVICE, "sigPermissionINTERNET", false);
		Event sendSMSEvent = builSignatureResult(DEVICE, "sigPermissionSENDSMS", false);
		Event accessNetworkStateEvent = builSignatureResult(DEVICE, "sigPermissionACCESSNETWORKSTATE", false);
		Event accessCoarseLocationEvent = builSignatureResult(DEVICE, "sigPermissionACCESSCOARSELOCATION", false);

		mPolicyActionUpdater.submitNewPollResult(buildBlankPollResult());

		mPolicyActionUpdater.submitNewEvent(accessCoarseLocationEvent);
		mPolicyActionUpdater.submitNewEvent(accessNetworkStateEvent);
		mPolicyActionUpdater.submitNewEvent(sendSMSEvent);
		mPolicyActionUpdater.submitNewEvent(internetEvent);
		mPolicyActionUpdater.submitNewEvent(ruleEvent);
		mPolicyActionUpdater.submitNewEvent(policyEvent);

		try {
			mPolicyActionUpdater.newPollResult();
		} catch (InterruptedException e) {
			mLog.error(e.getClass().getSimpleName());
		}

		verify(mPolicyActionSearcher, times(0)).submitNewPolicyAction(argThat(new FeatureCountPairMatcher(0)));
		verify(mPolicyActionUpdater, times(1)).sendPublishUpdate(argThat(new PolicyActionListMatcher(1, 1, 0)));

	}

	private PollResult buildBlankPollResult() {
		return PollResultMock(SearchResultMock(SUB1, Type.updateResult,
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":0", DEVICE),
						CreateEsukomFeature("BLANK", TYPE_ARBITRARY, "BLANK", SECOND_TIMESTAMP))));
	}

	@Test
	public void expected_OneSubmitNewPolicyActionCallWhithFourFeatures() throws IfmapErrorResult, IfmapException {
		Event policyEvent = builPolicyResult(DEVICE, POLICY_File_LOCATION, true);
		Event ruleEvent = builRuleResult(DEVICE, "unknownAPP", true);
		Event internetEvent = builSignatureResult(DEVICE, "sigPermissionINTERNET", true);
		Event sendSMSEvent = builSignatureResult(DEVICE, "sigPermissionSENDSMS", true);
		Event accessNetworkStateEvent = builSignatureResult(DEVICE, "sigPermissionACCESSNETWORKSTATE", true);
		Event accessCoarseLocationEvent = builSignatureResult(DEVICE, "sigPermissionACCESSCOARSELOCATION", true);

		mPolicyActionUpdater.submitNewPollResult(buildPollResultWithFourFeatures());

		mPolicyActionUpdater.submitNewEvent(accessCoarseLocationEvent);
		mPolicyActionUpdater.submitNewEvent(accessNetworkStateEvent);
		mPolicyActionUpdater.submitNewEvent(sendSMSEvent);
		mPolicyActionUpdater.submitNewEvent(internetEvent);
		mPolicyActionUpdater.submitNewEvent(ruleEvent);
		mPolicyActionUpdater.submitNewEvent(policyEvent);

		try {
			mPolicyActionUpdater.newPollResult();
		} catch (InterruptedException e) {
			mLog.error(e.getClass().getSimpleName());
		}

		verify(mPolicyActionSearcher, times(1)).submitNewPolicyAction(argThat(new FeatureCountPairMatcher(4)));
		verify(mPolicyActionUpdater, never()).sendPublishUpdate(anyList());

	}

	private PollResult buildPollResultWithFourFeatures() {
		return PollResultMock(SearchResultMock(
				SUB1,
				Type.updateResult,
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":0", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY, APP_PERMISSION_ACCESS_COARSE_LOCATION,
								THIRD_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":0", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY, APP_PERMISSION_ACCESS_NETWORK_STATE,
								THIRD_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":0", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY, APP_PERMISSION_INTERNET, THIRD_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":0", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY, APP_PERMISSION_SEND_SMS, THIRD_TIMESTAMP))));
	}

	@Test
	public void expected_OneSubmitNewPolicyActionCallWhithThreeFeatures() throws IfmapErrorResult, IfmapException {
		Event policyEvent = builPolicyResult(DEVICE, POLICY_File_LOCATION, true);
		Event ruleEvent = builRuleResult(DEVICE, "unknownAPP", true);
		Event internetEvent = builSignatureResult(DEVICE, "sigPermissionINTERNET", true);
		Event sendSMSEvent = builSignatureResult(DEVICE, "sigPermissionSENDSMS", false);
		Event accessNetworkStateEvent = builSignatureResult(DEVICE, "sigPermissionACCESSNETWORKSTATE", true);
		Event accessCoarseLocationEvent = builSignatureResult(DEVICE, "sigPermissionACCESSCOARSELOCATION", true);

		mPolicyActionUpdater.submitNewPollResult(buildPollResultWithThreeFeatures());

		mPolicyActionUpdater.submitNewEvent(accessCoarseLocationEvent);
		mPolicyActionUpdater.submitNewEvent(accessNetworkStateEvent);
		mPolicyActionUpdater.submitNewEvent(sendSMSEvent);
		mPolicyActionUpdater.submitNewEvent(internetEvent);
		mPolicyActionUpdater.submitNewEvent(ruleEvent);
		mPolicyActionUpdater.submitNewEvent(policyEvent);

		try {
			mPolicyActionUpdater.newPollResult();
		} catch (InterruptedException e) {
			mLog.error(e.getClass().getSimpleName());
		}

		verify(mPolicyActionSearcher, times(1)).submitNewPolicyAction(argThat(new FeatureCountPairMatcher(3)));
		verify(mPolicyActionUpdater, never()).sendPublishUpdate(anyList());

	}

	private PollResult buildPollResultWithThreeFeatures() {
		return PollResultMock(SearchResultMock(SUB1, Type.updateResult,
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":0", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY, APP_PERMISSION_ACCESS_COARSE_LOCATION,
								THIRD_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":0", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY, APP_PERMISSION_ACCESS_NETWORK_STATE,
								THIRD_TIMESTAMP)),
				ResultItemMock(
						CreateCategory(CATEGORY_APP_0_PERMISSION + ":0", DEVICE),
						CreateEsukomFeature(PERMISSION_NAME, TYPE_ARBITRARY, APP_PERMISSION_INTERNET, THIRD_TIMESTAMP))));
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
