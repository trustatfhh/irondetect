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
 * Copyright (C) 2010 - 2016 Trust@HsH
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
package de.hshannover.f4.trust.irondetect.policy.publisher.identifier.handler;

import java.io.FileNotFoundException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.hshannover.f4.trust.ifmapj.log.IfmapJLog;
import de.hshannover.f4.trust.irondetect.model.Policy;
import de.hshannover.f4.trust.irondetect.policy.parser.ParseException;
import de.hshannover.f4.trust.irondetect.policy.parser.PolicyFactory;

public abstract class AbstractHandlerTest {

	protected Policy mPolicy;

	protected DocumentBuilder mDocumentBuilder;

	public AbstractHandlerTest() throws FileNotFoundException, ParseException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			mDocumentBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IfmapJLog.error("Could not get DocumentBuilder instance [" + e.getMessage() + "]");
			throw new RuntimeException(e);
		}

		mPolicy = PolicyFactory.readPolicy("src/test/resources/PolicyHandlerTest.pol");
	}

}
