/**
 * 
 */
package de.fhhannover.inform.trust.irondetect.model;

/*
 * #%L
 * ====================================================
 *   _____                _     ____  _____ _   _ _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \|  ___| | | | | | |
 *    | | | '__| | | / __| __|/ / _` | |_  | |_| | |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _| |  _  |  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_|   |_| |_|_| |_|
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
 * Website: http://trust.inform.fh-hannover.de/
 * 
 * This file is part of irongui, version 0.0.3, implemented by the Trust@FHH 
 * research group at the Hochschule Hannover, a program to visualize the content
 * of a MAP Server (MAPS), a crucial component within the TNC architecture.
 * 
 * The development was started within the bachelor
 * thesis of Tobias Ruhe at Hochschule Hannover (University of
 * Applied Sciences and Arts Hannover). irongui is now maintained
 * and extended within the ESUKOM research project. More information
 * can be found at the Trust@FHH website.
 * %%
 * Copyright (C) 2010 - 2013 Trust@FHH
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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.fhhannover.inform.trust.Category;
import de.fhhannover.inform.trust.ContextParamType;
import de.fhhannover.inform.trust.ContextParameter;
import de.fhhannover.inform.trust.Feature;
import de.fhhannover.inform.trust.FeatureType;
import de.fhhannover.inform.trust.irondetect.model.Context;
import de.fhhannover.inform.trust.irondetect.model.ContextParameterPol;
import de.fhhannover.inform.trust.irondetect.util.BooleanOperator;
import de.fhhannover.inform.trust.irondetect.util.ComparisonOperator;
import de.fhhannover.inform.trust.irondetect.util.Pair;

/**
 * @author bahellma
 *
 */
public class ContextTest {

	private Feature feature;
	private Context contextEQ;
	private Context contextNE;
	private Context contextGE;
	private Context contextGT;
	private Context contextSE;
	private Context contextST;
	
	private String featureDateTime;
	private String policyDateTimeEQ;
	private String policyDateTimeNE;
	private String policyDateTimeGE;
	private String policyDateTimeGT;
	private String policyDateTimeSE;
	private String policyDateTimeST;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.featureDateTime = "2012-05-01T12:00:00-00:00";
		this.policyDateTimeEQ = "12:00";	// same as feature time
		this.policyDateTimeNE = "0:00";
		this.policyDateTimeGE = "8:00";
		this.policyDateTimeGT = "8:00";
		this.policyDateTimeSE = "16:00";
		this.policyDateTimeST = "16:00";
		
		Set<ContextParameter> ctxParams = new HashSet<ContextParameter>();
		ctxParams.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME), this.featureDateTime));
		this.feature = new Feature("", "", new FeatureType(FeatureType.QUANTITIVE), new Category(""), ctxParams);
		
		ArrayList<Pair<ContextParameterPol, BooleanOperator>> ctxParamSetEQ = new ArrayList<Pair<ContextParameterPol,BooleanOperator>>();
		ctxParamSetEQ.add(new Pair<ContextParameterPol, BooleanOperator>(new ContextParameterPol("", new ContextParamType(ContextParamType.DATETIME), policyDateTimeEQ, ComparisonOperator.EQ), null));
		this.contextEQ = new Context("");
		this.contextEQ.setCtxParamSet(ctxParamSetEQ);
		
		ArrayList<Pair<ContextParameterPol, BooleanOperator>> ctxParamSetNE = new ArrayList<Pair<ContextParameterPol,BooleanOperator>>();
		ctxParamSetNE.add(new Pair<ContextParameterPol, BooleanOperator>(new ContextParameterPol("", new ContextParamType(ContextParamType.DATETIME), policyDateTimeNE, ComparisonOperator.NE), null));
		this.contextNE = new Context("");
		this.contextNE.setCtxParamSet(ctxParamSetNE);
		
		ArrayList<Pair<ContextParameterPol, BooleanOperator>> ctxParamSetGE = new ArrayList<Pair<ContextParameterPol,BooleanOperator>>();
		ctxParamSetGE.add(new Pair<ContextParameterPol, BooleanOperator>(new ContextParameterPol("", new ContextParamType(ContextParamType.DATETIME), policyDateTimeGE, ComparisonOperator.GE), null));
		this.contextGE = new Context("");
		this.contextGE.setCtxParamSet(ctxParamSetGE);
		
		ArrayList<Pair<ContextParameterPol, BooleanOperator>> ctxParamSetGT = new ArrayList<Pair<ContextParameterPol,BooleanOperator>>();
		ctxParamSetGT.add(new Pair<ContextParameterPol, BooleanOperator>(new ContextParameterPol("", new ContextParamType(ContextParamType.DATETIME), policyDateTimeGT, ComparisonOperator.GT), null));
		this.contextGT = new Context("");
		this.contextGT.setCtxParamSet(ctxParamSetGT);
		
		ArrayList<Pair<ContextParameterPol, BooleanOperator>> ctxParamSetSE = new ArrayList<Pair<ContextParameterPol,BooleanOperator>>();
		ctxParamSetSE.add(new Pair<ContextParameterPol, BooleanOperator>(new ContextParameterPol("", new ContextParamType(ContextParamType.DATETIME), policyDateTimeSE, ComparisonOperator.SE), null));
		this.contextSE = new Context("");
		this.contextSE.setCtxParamSet(ctxParamSetSE);
		
		ArrayList<Pair<ContextParameterPol, BooleanOperator>> ctxParamSetST = new ArrayList<Pair<ContextParameterPol,BooleanOperator>>();
		ctxParamSetST.add(new Pair<ContextParameterPol, BooleanOperator>(new ContextParameterPol("", new ContextParamType(ContextParamType.DATETIME), policyDateTimeST, ComparisonOperator.ST), null));
		this.contextST = new Context("");
		this.contextST.setCtxParamSet(ctxParamSetST);
	}

	@Test
	public void checkTest() {
		assertTrue(this.contextEQ.match(this.feature));
		assertTrue(this.contextNE.match(this.feature));
		assertTrue(this.contextGE.match(this.feature));
		assertTrue(this.contextGT.match(this.feature));
		assertTrue(this.contextSE.match(this.feature));
		assertTrue(this.contextST.match(this.feature));
	}

}
