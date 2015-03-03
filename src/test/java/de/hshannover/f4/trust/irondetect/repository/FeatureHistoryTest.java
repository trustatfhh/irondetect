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
 * This file is part of irondetect, version 0.0.6, 
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



import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.hshannover.f4.trust.Category;
import de.hshannover.f4.trust.ContextParamType;
import de.hshannover.f4.trust.ContextParameter;
import de.hshannover.f4.trust.Feature;
import de.hshannover.f4.trust.FeatureType;
import de.hshannover.f4.trust.irondetect.util.Helper;
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * @author bahellma
 *
 */
public class FeatureHistoryTest {

	private FeatureHistory fh1Equalsf1;
	private Feature f1Equalsfh1;
	private Feature f2;
	
	private FeatureHistory fh;
	private Feature featureTime1;
	private Feature featureTime2;
	private Feature featureTime3;
	private HashSet<ContextParameter> contextTime1;
	private HashSet<ContextParameter> contextTime2;
	private HashSet<ContextParameter> contextTime3;
	
	private Feature featureCategory1;
	private Feature featureCategory2;
	private FeatureHistory fhCategory1;
	
	private FeatureHistory fhDeleted;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		f1Equalsfh1 = new Feature("feature1", "", new FeatureType(FeatureType.QUANTITIVE), new Category("category1"), new HashSet<ContextParameter>());
		f2 = new Feature("feature2", "", new FeatureType(FeatureType.QUANTITIVE), new Category("category1"), new HashSet<ContextParameter>());
		fh1Equalsf1 = new FeatureHistory();
		fh1Equalsf1.addFeature(f1Equalsfh1, false);
		
		fh = new FeatureHistory();
		contextTime1 = new HashSet<ContextParameter>();
		contextTime1.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME), "1970-01-01T00:00:00-00:00"));
		
		contextTime2 = new HashSet<ContextParameter>();
		contextTime2.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME), "1970-01-01T01:00:00-00:00"));
		
		contextTime3 = new HashSet<ContextParameter>();
		contextTime3.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME), "1970-01-01T02:00:00-00:00"));
		
		featureTime1 = new Feature("feature2", "", new FeatureType(FeatureType.QUANTITIVE), new Category(""), contextTime1);
		featureTime2 = new Feature("feature2", "", new FeatureType(FeatureType.QUANTITIVE), new Category(""), contextTime2);
		featureTime3 = new Feature("feature2", "", new FeatureType(FeatureType.QUANTITIVE), new Category(""), contextTime3);
		
		featureCategory1 = new Feature("feature1", "", new FeatureType(FeatureType.QUANTITIVE), new Category("category1"), new HashSet<ContextParameter>());
		featureCategory2 = new Feature("feature1", "", new FeatureType(FeatureType.QUANTITIVE), new Category("category2"), new HashSet<ContextParameter>());
		fhCategory1 = new FeatureHistory();
		fhCategory1.addFeature(featureCategory1, false);
		
		fhDeleted = new FeatureHistory();
	}

	@Test
	public void featureBaseEqualsFeature() {
		assertTrue(fh1Equalsf1.hasQualifiedFeatureId(f1Equalsfh1));
		assertFalse(fh1Equalsf1.hasQualifiedFeatureId(f2));
	}
	
	@Test
	public void featureHistorySortOrder() {
		fh.addFeature(featureTime1, false);
		fh.addFeature(featureTime3, false);
		List<Pair<Feature, Boolean>> allFeatures = fh.getAllFeatures();
		assertTrue(allFeatures.size() == 2);
		
		Feature allFeatures1Object1 = allFeatures.get(0).getFirstElement();
		Feature allFeatures1Object2 = allFeatures.get(1).getFirstElement();
		ContextParameter allFeatureObject1DateTime = allFeatures1Object1.getContextParameterByType(new ContextParamType(ContextParamType.DATETIME));
		ContextParameter allFeatureObject2DateTime = allFeatures1Object2.getContextParameterByType(new ContextParamType(ContextParamType.DATETIME));
		
		assertTrue(Helper.getXsdStringAsCalendar(allFeatureObject1DateTime.getValue()).after(Helper.getXsdStringAsCalendar(allFeatureObject2DateTime.getValue())));
		assertFalse(Helper.getXsdStringAsCalendar(allFeatureObject1DateTime.getValue()).before(Helper.getXsdStringAsCalendar(allFeatureObject1DateTime.getValue())));
		
		fh.addFeature(featureTime2, false);
		allFeatures = fh.getAllFeatures();
		assertTrue(allFeatures.size() == 3);
		
		Feature allFeaturesObject1 = allFeatures.get(0).getFirstElement();
		Feature allFeaturesObject2 = allFeatures.get(1).getFirstElement();
		Feature allFeaturesObject3 = allFeatures.get(2).getFirstElement();
		allFeatureObject1DateTime = allFeaturesObject1.getContextParameterByType(new ContextParamType(ContextParamType.DATETIME));
		allFeatureObject2DateTime = allFeaturesObject2.getContextParameterByType(new ContextParamType(ContextParamType.DATETIME));
		ContextParameter allFeatureObject3DateTime = allFeaturesObject3.getContextParameterByType(new ContextParamType(ContextParamType.DATETIME));
		
		ContextParameter featureTime1DateTime = featureTime1.getContextParameterByType(new ContextParamType(ContextParamType.DATETIME));
		ContextParameter featureTime2DateTime = featureTime2.getContextParameterByType(new ContextParamType(ContextParamType.DATETIME));
		ContextParameter featureTime3DateTime = featureTime3.getContextParameterByType(new ContextParamType(ContextParamType.DATETIME));
		
		assertTrue(Helper.getXsdStringAsCalendar(allFeatureObject1DateTime.getValue()).after(Helper.getXsdStringAsCalendar(allFeatureObject2DateTime.getValue())));
		assertTrue(Helper.getXsdStringAsCalendar(allFeatureObject2DateTime.getValue()).after(Helper.getXsdStringAsCalendar(allFeatureObject3DateTime.getValue())));
		assertTrue(Helper.getXsdStringAsCalendar(allFeatureObject1DateTime.getValue()).after(Helper.getXsdStringAsCalendar(allFeatureObject3DateTime.getValue())));
		
		assertFalse(Helper.getXsdStringAsCalendar(allFeatureObject1DateTime.getValue()).before(Helper.getXsdStringAsCalendar(allFeatureObject2DateTime.getValue())));
		assertFalse(Helper.getXsdStringAsCalendar(allFeatureObject2DateTime.getValue()).before(Helper.getXsdStringAsCalendar(allFeatureObject3DateTime.getValue())));
		assertFalse(Helper.getXsdStringAsCalendar(allFeatureObject1DateTime.getValue()).before(Helper.getXsdStringAsCalendar(allFeatureObject3DateTime.getValue())));
		
		assertTrue(Helper.getXsdStringAsCalendar(featureTime3DateTime.getValue()).compareTo(Helper.getXsdStringAsCalendar(allFeatureObject1DateTime.getValue())) == 0);
		assertTrue(Helper.getXsdStringAsCalendar(featureTime2DateTime.getValue()).compareTo(Helper.getXsdStringAsCalendar(allFeatureObject2DateTime.getValue())) == 0);
		assertTrue(Helper.getXsdStringAsCalendar(featureTime1DateTime.getValue()).compareTo(Helper.getXsdStringAsCalendar(allFeatureObject3DateTime.getValue())) == 0);
	}
	
	@Test
	public void insertFeaturesWithDifferentCategories() {
		assertTrue(fhCategory1.hasQualifiedFeatureId(featureCategory1));
		assertFalse(fhCategory1.hasQualifiedFeatureId(featureCategory2));
	}
	
	@Test
	public void numberOfDeletedFeatures() {
		fhDeleted.addFeature(featureTime1, false);
		fhDeleted.addFeature(featureTime2, true);
		fhDeleted.addFeature(featureTime3, false);
		assertTrue(fhDeleted.getNumberOfDeletedFeatures() == 1);
		assertTrue(fhDeleted.getNumberOfNotDeletedFeatures() == 2);
		assertTrue(fhDeleted.getSize() == 3);
	}
}
