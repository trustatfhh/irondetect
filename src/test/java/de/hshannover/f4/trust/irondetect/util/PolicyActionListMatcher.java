package de.hshannover.f4.trust.irondetect.util;

import java.util.List;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.ifmapj.messages.PublishUpdate;

//TODO noch nin schöneren anderen Namen?
public class PolicyActionListMatcher extends ListSizeMatcher<PublishUpdate> {

	private String mErrorDescription;

	private ArgumentMatcher<Document> policyActionMatcher;

	private int mMetadataCount;

	public PolicyActionListMatcher(int listSize, int metadataCount, int revFeatureCount) {
		super(listSize);

		mMetadataCount = metadataCount;
		policyActionMatcher = new PolicyActionMatcher(revFeatureCount);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Object o) {
		if (!super.matches(o)) {
			return false;
		}

		int metadataCount = 0;
		List<PublishUpdate> list = (List<PublishUpdate>) o;
		for (PublishUpdate update : list) {
			List<Document> documentList = update.getMetadata();
			metadataCount = metadataCount + documentList.size();

			for (Document document : documentList) {
				if (!policyActionMatcher.matches(document)) {
					return false;
				}
			}
		}

		if (metadataCount != mMetadataCount) {
			setErrorDescription("Expected Metadata count = '" + mMetadataCount + "'. But was '" + metadataCount + "'");
			return false;
		}

		return true;
	}

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
