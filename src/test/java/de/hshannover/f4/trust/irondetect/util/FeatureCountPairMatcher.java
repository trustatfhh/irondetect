package de.hshannover.f4.trust.irondetect.util;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.irondetect.gui.ResultObject;

/**
 * An {@link FeatureCountPairMatcher}.
 * 
 * @author Marcel Reichenbach
 */
public class FeatureCountPairMatcher extends PairMatcher<ResultObject, Document> {

	private String mErrorDescription;

	private ArgumentMatcher<Document> policyActionMatcher;

	public FeatureCountPairMatcher(int revFeatureCount) {
		policyActionMatcher = new PolicyActionMatcher(revFeatureCount);
	}

	/**
	 * The method matches() needs a one lastFeatureCount because this method calls multiple.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Object o) {
		if (!super.matches(o)) {
			return false;
		}

		Pair<ResultObject, Document> pair = (Pair<ResultObject, Document>) o;
		Document document = pair.getSecondElement();

		if (!policyActionMatcher.matches(document)) {
			return false;
		}
		
		return true;
	}


	@SuppressWarnings("unused")
	private void setErrorDescription(String description) {
		mErrorDescription = description;
	}

	@Override
	public void describeTo(Description description) {
		super.describeTo(description);

		policyActionMatcher.describeTo(description);

		if (mErrorDescription != null) {
			description.appendText(" -> ERROR = " + mErrorDescription);
		}
	}
}
