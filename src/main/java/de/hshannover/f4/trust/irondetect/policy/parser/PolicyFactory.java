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
 * This file is part of irondetect, version 0.0.8, 
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2015 Trust@HsH
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
package de.hshannover.f4.trust.irondetect.policy.parser;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hshannover.f4.trust.irondetect.model.Policy;
import de.hshannover.f4.trust.irondetect.util.Helper;

/**
 * @author rosso
 *
 */
public class PolicyFactory {
	
	/**
	 * @param configPath
	 * @return
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static Policy readPolicy(String configPath) throws FileNotFoundException, ParseException {
		return readPolicy(configPath, configPath);
	}
	
	/**
	 * @param configPath
	 * @param policyId
	 * @return
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static Policy readPolicy(String configPath, String policyId) throws FileNotFoundException, ParseException {
//		InputStream resourceAsStream = PolicyFactory.class.getResourceAsStream(configPath);
		InputStream resourceAsStream = Helper.getInputStreamForFile(configPath);
		Policy p = readPolicy(resourceAsStream);
		p.setId(policyId);
		return p;
	}
	
	/**
	 * @param configFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static Policy readPolicy(File configFile) throws FileNotFoundException, ParseException {
		FileInputStream is = new FileInputStream(configFile);
		return readPolicy(is);
	}
	/**
	 * @param configFile
	 * @param policyId
	 * @return
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static Policy readPolicy(File configFile, String policyId) throws FileNotFoundException, ParseException {
		Policy p = readPolicy(configFile);
		p.setId(policyId);
		return p;
	}
	
	/**
	 * @param is
	 * @return
	 * @throws ParseException
	 */
	public static Policy readPolicy(InputStream is) throws ParseException {
		PolicyParser parser = new PolicyParser(is);
		Policy p = null;
		try {
			p = parser.parse();
		} catch (TokenMgrError e) {
			e.printStackTrace();
		}
		return p;
	}
	
	/**
	 * @param is
	 * @param policyId
	 * @return
	 * @throws ParseException
	 */
	public static Policy readPolicy(InputStream is, String policyId)
			throws ParseException  {
		Policy p = readPolicy(is);
		p.setId(policyId);	
		return p;
	}
	
	
}
