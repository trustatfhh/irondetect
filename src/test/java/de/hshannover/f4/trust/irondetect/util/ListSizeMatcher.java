package de.hshannover.f4.trust.irondetect.util;

import java.util.List;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

/**
 * An {@link ListSizeMatcher}.
 * 
 * @author Marcel Reichenbach
 */
public class ListSizeMatcher<T> extends ArgumentMatcher<List<T>> {

	private String mErrorDescription;

	private int mListSize;

	public ListSizeMatcher(int listSize) {
		mListSize = listSize;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Object o) {
		if (o == null) {
			setErrorDescription("It was not a List. It is 'null'.\n");
			return false;
		}

		if (o instanceof List) {
			List<T> list = (List<T>) o;

			if (list.size() != mListSize) {
				setErrorDescription("A List with size = '" + mListSize + "'. But was '" + list.size() + "'");
				return false;
			}
		}
		return true;
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
