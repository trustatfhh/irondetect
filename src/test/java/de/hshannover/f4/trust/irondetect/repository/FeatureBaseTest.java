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
/**
 * 
 */
package de.hshannover.f4.trust.irondetect.repository;



import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.hshannover.f4.trust.irondetect.model.Category;
import de.hshannover.f4.trust.irondetect.model.ContextParamType;
import de.hshannover.f4.trust.irondetect.model.ContextParameter;
import de.hshannover.f4.trust.irondetect.model.Feature;
import de.hshannover.f4.trust.irondetect.model.FeatureType;
import de.hshannover.f4.trust.irondetect.util.Helper;
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * @author bahellma
 *
 */
public class FeatureBaseTest {

	private FeatureBase fb1;
	private FeatureBaseImpl fbImpl1;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.fbImpl1 = FeatureBaseImpl.getInstance();
		this.fb1 = this.fbImpl1;
	}

	@Test
	public void addNewFeatureTest1() {
		this.fb1.resetFeatureBase();
		String device1 = "device1";
		String device2 = "device2";
		String id1 = "feature1";
		String id2 = "feature2";
		FeatureType type = new FeatureType(FeatureType.QUANTITIVE);
		Category cat = new Category("category1");
		Set<ContextParameter> ctxParams = new HashSet<ContextParameter>();		
		
		Calendar c = new GregorianCalendar();
		
		Feature feature;
		List<Pair<String, Pair<Feature, Boolean>>> features = new ArrayList<Pair<String,Pair<Feature,Boolean>>>();
		Pair<String, Pair<Feature, Boolean>> pair;
		
		for (int i = 0; i < 3; i++) {
			c.set(2012, 05, 18, i, 0, 0);
			ctxParams = new HashSet<ContextParameter>();
			ctxParams.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME), Helper.getCalendarAsXsdDateTime(c)));
			
			feature = new Feature(id1, "0.0", type, cat, ctxParams);
			pair = new Pair<String, Pair<Feature,Boolean>>(device1, new Pair<Feature, Boolean>(feature, false));
			features.add(pair);
		}
		
		for (int i = 0; i < 2; i++) {
			c.set(2012, 05, 18, i, 0, 0);
			ctxParams = new HashSet<ContextParameter>();
			ctxParams.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME), Helper.getCalendarAsXsdDateTime(c)));
			
			feature = new Feature(id1, "0.0", type, cat, ctxParams);
			pair = new Pair<String, Pair<Feature,Boolean>>(device2, new Pair<Feature, Boolean>(feature, false));
			features.add(pair);
		}
		
		for (int i = 0; i < 2; i++) {
			c.set(2012, 05, 18, i, 0, 0);
			ctxParams = new HashSet<ContextParameter>();
			ctxParams.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME), Helper.getCalendarAsXsdDateTime(c)));
			
			feature = new Feature(id2, "0.0", type, cat, ctxParams);
			pair = new Pair<String, Pair<Feature,Boolean>>(device2, new Pair<Feature, Boolean>(feature, false));
			features.add(pair);
		}
		
		this.fb1.addNewFeatures(features, false);
		
		List<FeatureHistory> allFeatureHistoriesForDevice1 = this.fbImpl1.getAllFeatureHistoriesForDevice(device1);
		List<FeatureHistory> allFeatureHistoriesForDevice2 = this.fbImpl1.getAllFeatureHistoriesForDevice(device2);
		assertTrue(allFeatureHistoriesForDevice1.size() == 1);
		assertTrue(allFeatureHistoriesForDevice2.size() == 2);
		
		assertTrue(this.fbImpl1.getFeatureHistoryByQualifiedFeatureId(allFeatureHistoriesForDevice1, cat.getId() + "." + id1).getSize() == 3);
		assertTrue(this.fbImpl1.getFeatureHistoryByQualifiedFeatureId(allFeatureHistoriesForDevice2, cat.getId() + "." + id1).getSize() == 2);
		assertTrue(this.fbImpl1.getFeatureHistoryByQualifiedFeatureId(allFeatureHistoriesForDevice2, cat.getId() + "." + id2).getSize() == 2);
		
		/**
		 * New Features after first call of addNewFeatures.
		 */
		features = new ArrayList<Pair<String,Pair<Feature,Boolean>>>();
		
		/**
		 * Add TWO new Features with ID2 to DEVICE1.
		 */
		for (int i = 0; i < 2; i++) {
			c.set(2012, 05, 18, i, 0, 0);
			ctxParams = new HashSet<ContextParameter>();
			ctxParams.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME), Helper.getCalendarAsXsdDateTime(c)));
			
			feature = new Feature(id2, "0.0", type, cat, ctxParams);
			pair = new Pair<String, Pair<Feature,Boolean>>(device1, new Pair<Feature, Boolean>(feature, false));
			features.add(pair);
		}
		
		/**
		 * Delete ONE Feature with ID2 on DEVICE2.
		 */
		for (int i = 0; i < 1; i++) {
			c.set(2012, 05, 18, i + 3, 0, 0);
			ctxParams = new HashSet<ContextParameter>();
			ctxParams.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME), Helper.getCalendarAsXsdDateTime(c)));
			
			feature = new Feature(id2, "0.0", type, cat, ctxParams);
			pair = new Pair<String, Pair<Feature,Boolean>>(device2, new Pair<Feature, Boolean>(feature, true));
			features.add(pair);
		}
		
		/**
		 * Add TWO new Features with ID2 to DEVICE2. 
		 */
		for (int i = 0; i < 2; i++) {
			c.set(2012, 05, 18, i + 4, 0, 0);
			ctxParams = new HashSet<ContextParameter>();
			ctxParams.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME), Helper.getCalendarAsXsdDateTime(c)));
			
			feature = new Feature(id2, "0.0", type, cat, ctxParams);
			pair = new Pair<String, Pair<Feature,Boolean>>(device2, new Pair<Feature, Boolean>(feature, false));
			features.add(pair);
		}
		
		this.fb1.addNewFeatures(features, false);
		
		allFeatureHistoriesForDevice1 = this.fbImpl1.getAllFeatureHistoriesForDevice(device1);
		allFeatureHistoriesForDevice2 = this.fbImpl1.getAllFeatureHistoriesForDevice(device2);
		assertTrue(allFeatureHistoriesForDevice1.size() == 2);
		assertTrue(allFeatureHistoriesForDevice2.size() == 2);
		
		this.fb1.resetFeatureBase();
		
		allFeatureHistoriesForDevice1 = this.fbImpl1.getAllFeatureHistoriesForDevice(device1);
		assertTrue(allFeatureHistoriesForDevice1 == null);
	}
	
	@Test
	public void getFeaturesByContext1Test() {
//		this.fb1.resetFeatureBase();
//		
//		String device = "device1";
//		String id1 = "feature1";
//		String id2 = "feature2";
//		String category = "category1";
//		String qualId1 = category + "." + id1;
//		String qualId2 = category + "." + id2;
//		
//		List<String> ids = new ArrayList<String>();
//		ids.add(qualId1);
//		ids.add(qualId2);
//		
//		List<Context> contextSet = new ArrayList<Context>();
//		contextSet.add(new Context(id, description, ctxParamSet));

//		this.fb1.getFeaturesByContext(device, ids, contextSet);
	}
	
	@Test
	public void getFeaturesByContext2Test() {
//		String device = "";
//		List<String> ids = new ArrayList<String>();
//		List<Context> contextSet = new ArrayList<Context>();
//		this.fb1.getFeaturesByContext(device, ids, contextSet);
	}

}
