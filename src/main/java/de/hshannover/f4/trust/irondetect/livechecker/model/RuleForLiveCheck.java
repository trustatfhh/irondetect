package de.hshannover.f4.trust.irondetect.livechecker.model;

import static de.hshannover.f4.trust.irondetect.gui.ResultObjectType.RULE;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.engine.Processor;
import de.hshannover.f4.trust.irondetect.gui.ResultLogger;
import de.hshannover.f4.trust.irondetect.livechecker.gui.ResultLoggerForLiveCheck;
import de.hshannover.f4.trust.irondetect.model.Action;
import de.hshannover.f4.trust.irondetect.model.Rule;

public class RuleForLiveCheck extends Rule {

	private Logger mLogger = Logger.getLogger(RuleForLiveCheck.class);

	private ResultLogger mRlogger = ResultLoggerForLiveCheck.getInstance();

	public RuleForLiveCheck(Rule rule) {

	}

	/**
	 * checks the condition, if it is true all actions will be performed
	 */
	@Override
	public void evaluate(String device) {

		mLogger.info("checking rule " + getId());
		super.condition.setParent(this);
		boolean result = super.condition.evaluate(device);
		mLogger.info("rule " + getId() + " result was " + result);
		mRlogger.reportResultsToLogger(device, super.id, RULE, result);
		// we only perform actions when in testing mode
		if (result && Processor.getInstance().isTesting()) {
			for (Action a : super.actions) {
				a.perform(device);
			}
		}
	}

}
