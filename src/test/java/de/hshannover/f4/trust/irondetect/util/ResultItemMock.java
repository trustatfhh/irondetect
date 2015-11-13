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
 * This file is part of visitmeta-dataservice, version 0.5.1,
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2012 - 2015 Trust@HsH
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.messages.ResultItem;

/**
 * 
 * @author Marcel Reichenbach
 *
 */
public class ResultItemMock implements IfmapMock<ResultItem> {

	private ResultItem mResultItem_mock;

	private List<Document> mMetadata;

	private ResultItemMock() {
		mResultItem_mock = mock(ResultItem.class);
		mMetadata = new ArrayList<Document>();
		when(mResultItem_mock.getMetadata()).thenReturn(mMetadata);
	}

	public ResultItemMock(Identifier id1, Identifier id2) {
		this();

		when(mResultItem_mock.getIdentifier1()).thenReturn(id1);
		when(mResultItem_mock.getIdentifier2()).thenReturn(id2);
		when(mResultItem_mock.holdsLink()).thenReturn(true);
	}

	public ResultItemMock(Identifier id1) {
		this();

		when(mResultItem_mock.getIdentifier1()).thenReturn(id1);
		when(mResultItem_mock.holdsLink()).thenReturn(false);
	}

	@Override
	public ResultItem getMock() {
		return mResultItem_mock;
	}

	public void addMetadata(Document... doc) {
		for (Document d : doc) {
			mMetadata.add(d);
		}

		when(mResultItem_mock.getMetadata()).thenReturn(mMetadata);
	}
	
	public void setMetadata(List<Document> docList) {
		mMetadata = docList;
		when(mResultItem_mock.getMetadata()).thenReturn(mMetadata);
	}

}