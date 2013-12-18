/*
 * #%L
 * =====================================================
 * 
 *   |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *     | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *     | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *     |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                              \____/
 * 
 *  =====================================================
 * 
 * Hochschule Hannover
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de/
 * 
 * This file is part of irondetect, version 0.0.5, implemented by the Trust@HsH
 * research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2013 Trust@HsH
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
package de.hshannover.f4.trust.procedure;



import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.hshannover.f4.trust.irondetect.model.ProcedureResult;
import de.hshannover.f4.trust.irondetect.procedure.ProcedureResultMapper;
import de.hshannover.f4.trust.irondetect.procedure.ProcedureResultMapper.Boundary;
import de.hshannover.f4.trust.irondetect.procedure.ProcedureResultMapper.DistanceType;

public class ProcedureResultMapperTest {
	
	private double precision = 0.001;
	
	@Test
	public void testPercentLow() {
		ProcedureResult pr;
		
		pr = ProcedureResultMapper.map(91, 100, DistanceType.percent, Boundary.low, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(89, 100, DistanceType.percent, Boundary.low, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(50, 100, DistanceType.percent, Boundary.low, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(49, 100, DistanceType.percent, Boundary.low, 10, 50);
		assertEquals(1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(110, 100, DistanceType.percent, Boundary.low, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(150, 100, DistanceType.percent, Boundary.low, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(200, 100, DistanceType.percent, Boundary.low, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
	}
	
	@Test
	public void testPercentHigh() {
		ProcedureResult pr;
		
		pr = ProcedureResultMapper.map(110, 100, DistanceType.percent, Boundary.high, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(111, 100, DistanceType.percent, Boundary.high, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(150, 100, DistanceType.percent, Boundary.high, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(151, 100, DistanceType.percent, Boundary.high, 10, 50);
		assertEquals(1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(90, 100, DistanceType.percent, Boundary.high, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(50, 100, DistanceType.percent, Boundary.high, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(0, 100, DistanceType.percent, Boundary.high, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
	}
	
	@Test
	public void testPercentBoth() {
		ProcedureResult pr;
		
		pr = ProcedureResultMapper.map(110, 100, DistanceType.percent, Boundary.both, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(111, 100, DistanceType.percent, Boundary.both, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(150, 100, DistanceType.percent, Boundary.both, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(151, 100, DistanceType.percent, Boundary.both, 10, 50);
		assertEquals(1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(90, 100, DistanceType.percent, Boundary.both, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(50, 100, DistanceType.percent, Boundary.both, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(0, 100, DistanceType.percent, Boundary.both, 10, 50);
		assertEquals(1.0, pr.getValue(), precision);
	}
	
	@Test
	public void testAbsoluteLow() {
		ProcedureResult pr;
		
		pr = ProcedureResultMapper.map(90, 100, DistanceType.absolute, Boundary.low, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(89, 100, DistanceType.absolute, Boundary.low, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(50, 100, DistanceType.absolute, Boundary.low, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(49, 100, DistanceType.absolute, Boundary.low, 10, 50);
		assertEquals(1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(110, 100, DistanceType.absolute, Boundary.low, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(150, 100, DistanceType.absolute, Boundary.low, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(200, 100, DistanceType.absolute, Boundary.low, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
	}
	
	@Test
	public void testAbsoluteHigh() {
		ProcedureResult pr;
		
		pr = ProcedureResultMapper.map(110, 100, DistanceType.absolute, Boundary.high, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(111, 100, DistanceType.absolute, Boundary.high, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(150, 100, DistanceType.absolute, Boundary.high, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(151, 100, DistanceType.absolute, Boundary.high, 10, 50);
		assertEquals(1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(90, 100, DistanceType.absolute, Boundary.high, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(50, 100, DistanceType.absolute, Boundary.high, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(0, 100, DistanceType.absolute, Boundary.high, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
	}
	
	@Test
	public void testAbsoluteBoth() {
		ProcedureResult pr;
		
		pr = ProcedureResultMapper.map(110, 100, DistanceType.absolute, Boundary.both, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(111, 100, DistanceType.absolute, Boundary.both, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(150, 100, DistanceType.absolute, Boundary.both, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(151, 100, DistanceType.absolute, Boundary.both, 10, 50);
		assertEquals(1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(90, 100, DistanceType.absolute, Boundary.both, 10, 50);
		assertEquals(-1.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(50, 100, DistanceType.absolute, Boundary.both, 10, 50);
		assertEquals(0.0, pr.getValue(), precision);
		
		pr = ProcedureResultMapper.map(0, 100, DistanceType.absolute, Boundary.both, 10, 50);
		assertEquals(1.0, pr.getValue(), precision);
	}
	
}
