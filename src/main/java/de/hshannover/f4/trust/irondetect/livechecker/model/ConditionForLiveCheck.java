package de.hshannover.f4.trust.irondetect.livechecker.model;

import static de.hshannover.f4.trust.irondetect.gui.ResultObjectType.CONDITION;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.gui.ResultLogger;
import de.hshannover.f4.trust.irondetect.livechecker.gui.ResultLoggerForLiveCheck;
import de.hshannover.f4.trust.irondetect.model.Condition;
import de.hshannover.f4.trust.irondetect.model.ConditionElement;
import de.hshannover.f4.trust.irondetect.util.BooleanOperator;
import de.hshannover.f4.trust.irondetect.util.Pair;

public class ConditionForLiveCheck extends Condition {

	private Logger mLogger = Logger.getLogger(ConditionForLiveCheck.class);

	private ResultLogger mRlogger = ResultLoggerForLiveCheck.getInstance();

	public ConditionForLiveCheck(Condition condition) {

		List<Pair<ConditionElement, BooleanOperator>> newconditionSet =
				new ArrayList<Pair<ConditionElement, BooleanOperator>>();
		for (Pair<ConditionElement, BooleanOperator> pair : condition.getConditionSet()) {
			newconditionSet.add(new Pair<ConditionElement, BooleanOperator>(new ConditionElementForLiveCheck(pair
					.getFirstElement()), pair.getSecondElement()));
		}

		super.setId(condition.getId());
		super.setParent(condition.getParent());
		super.setConditionSet(newconditionSet);
	}

	@Override
	public boolean evaluate(String device) {
		mLogger.debug("evaluating condition " + this.getId());
		for (Pair<ConditionElement, BooleanOperator> p : super.conditionSet) {
			p.getFirstElement().setParent(super.parent);
		}
		boolean result = super.evaluateConditionSet(device);
		mLogger.debug("condition " + super.id + " evaluation returned " + result);
		mRlogger.reportResultsToLogger(device, super.id, CONDITION, result);
		return result;

	}

}
