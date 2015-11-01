package de.hshannover.f4.trust.irondetect.util;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

/**
 * An {@link PairMatcher}.
 * 
 * @author Marcel Reichenbach
 */
public class PairMatcher<A, B> extends ArgumentMatcher<Pair<A, B>> {

	private String mErrorDescription;

	public PairMatcher() {
	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	@Override
	public boolean matches(Object o) {
		if (o == null) {
			setErrorDescription("It was not a Pair-Object. It is 'null'.\n");
			return false;
		}

		if (o instanceof Pair) {
			Pair pair = (Pair) o;

			try {
				
				Pair<A, B> pairWithType = (Pair<A, B>) o;
			
			} catch (ClassCastException e){
				setErrorDescription("But the Pair-Object types was: firstElement Class = '"
						+ pair.getFirstElement().getClass().getSimpleName() + "' and secondElement Class = '"
						+ pair.getSecondElement().getClass().getSimpleName() + "'.\n");
				return false;
			}
			return true;
		} else {

			setErrorDescription("But it was not a Pair-Object. It is a '" + o.getClass().getSimpleName()
					+ "'-Object.\n");
			return false;
		}
	}

	private void setErrorDescription(String description) {
		mErrorDescription = description;
	}

	@Override
	public void describeTo(Description description) {
		super.describeTo(description);
		if (mErrorDescription != null) {
			description.appendText(" -> ERROR = " + mErrorDescription);
		} else {
			description.appendText(" -> result = OK\n");
		}
	}
}
