/*
 * #%L
 * =====================================================
 *   _____                _     ____  _   _       _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Hochschule Hannover
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de/
 * 
 * This file is part of irondetect, version 0.0.10, 
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2018 Trust@HsH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.hshannover.f4.trust.irondetect.util;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PolicyActionMatcher extends ArgumentMatcher<Document> {

	private static final String POLICY_METADATA_PREFIX = "policy";

	private static final String POLICY_METADATA_URL = "http://www.trust.f4.hs-hannover.de/2015/POLICY/METADATA/1";

	private static final String POLICY_ACTION_TYPE_NAME = "policy-action";

	private static final String FEATURE_METADATA_PREFIX = "esukom";

	private static final String FEATURE_METADATA_URL = "http://www.esukom.de/2012/ifmap-metadata/1";

	private static final String FEATURE_TYPE_NAME = "feature";

	private String mErrorDescription;

	private int mRevFeatureCount;

	public PolicyActionMatcher(int revFeatureCount) {
		mRevFeatureCount = revFeatureCount;
	}

	@Override
	public boolean matches(Object o) {
		if (o == null) {
			setErrorDescription("It was not a Document-Object. It is 'null'.\n");
			return false;
		}

		int lastFeatureCount = 0;
		if (o instanceof Document) {
			Document document = (Document) o;
			Element docElement = document.getDocumentElement();

			// TODO die könnte man an sch auch über eine XML-Schemata validierung sicherstellen
			if (!checkDocumentElement(docElement)) {
				setErrorDescription("Document-Element is not a valid policy-action-Element!");
				return false;
			}

			NodeList childs = docElement.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				Node child = childs.item(i);
				String nodeName = child.getLocalName();

				if (FEATURE_TYPE_NAME.equals(nodeName)) {
					Node firstChild = child.getFirstChild();
					if (isFeatureMetadata(firstChild)) {
						lastFeatureCount++;
					}
				}
			}
		} else {
			setErrorDescription("Object is not a Document!");
			return false;
		}

		if (lastFeatureCount != mRevFeatureCount) {
			setErrorDescription("Expected ESUKOM-Feature-Metadata count = '" + mRevFeatureCount + "'. But was '" +
					lastFeatureCount + "'");
			return false;
		}

		return true;
	}

	private boolean checkDocumentElement(Element documentElement) {
		String nodePrefix = documentElement.getPrefix();
		String nodeUrl = documentElement.getNamespaceURI();
		String nodeLocalName = documentElement.getLocalName();
		String nodeName = documentElement.getNodeName();

		if (!POLICY_METADATA_PREFIX.equals(nodePrefix)) {
			return false;
		}

		if (!POLICY_METADATA_URL.equals(nodeUrl)) {
			return false;
		}

		if (!POLICY_ACTION_TYPE_NAME.equals(nodeLocalName)) {
			return false;
		}

		if (!nodeName.equals(POLICY_METADATA_PREFIX + ":" + POLICY_ACTION_TYPE_NAME)) {
			return false;
		}

		return true;
	}

	private boolean isFeatureMetadata(Node documentNode) {
		String nodePrefix = documentNode.getPrefix();
		String nodeUrl = documentNode.getNamespaceURI();
		String nodeTypeName = documentNode.getLocalName();

		if (!FEATURE_METADATA_PREFIX.equals(nodePrefix)) {
			return false;
		}

		if (!FEATURE_METADATA_URL.equals(nodeUrl)) {
			return false;
		}

		if (!FEATURE_TYPE_NAME.equals(nodeTypeName)) {
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
		if (mErrorDescription != null) {
			description.appendText(" -> ERROR = " + mErrorDescription);
		} else {
			description.appendText(" -> result = OK\n");
		}
	}

}
