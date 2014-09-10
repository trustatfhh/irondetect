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



import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.hshannover.f4.trust.irondetect.procedure.ProcedureRepository;
import de.hshannover.f4.trust.irondetect.procedure.Procedureable;

public class ProcedureRepositoryTest {
	
	private ProcedureRepository repository;
	
	@Before
	public void setUp() {
		repository = ProcedureRepository.getInstance();
	}
	
	@Test
	public void testNewProcedureById() {
		Procedureable p = repository.newProcedureById("de.hshannover.f4.trust.irondetectprocedures.Mean");
		assertTrue(p instanceof Procedureable);
	}
	
	@Test
	public void testNewProcedureByIdLoadAlwaysNew() {
		Procedureable p1 = repository.newProcedureById("de.hshannover.f4.trust.irondetectprocedures.Mean");
		Procedureable p2 = repository.newProcedureById("de.hshannover.f4.trust.irondetectprocedures.Mean");
		
		assertFalse(p1 == p2);
	}
	
	@Test
	public void testNewProcedureByIdNotImplementingProcedure() {
		Procedureable p = repository.newProcedureById("de.somepackage.NoProcedure");
		assertTrue(p == null);
	}
	
	@Test
	public void testGetProcedureByIdLoadNew() {
		Procedureable p = repository.getProcedureById("device1", "anomaly", "hint1", "de.hshannover.f4.trust.irondetectprocedures.Mean");
		assertTrue(p instanceof Procedureable);
	}
	
	@Test
	public void testGetProcedureByIdGetOld() {
		Procedureable p1 = repository.getProcedureById("device1", "anomaly", "hint1", "de.hshannover.f4.trust.irondetectprocedures.Mean");
		Procedureable p2 = repository.getProcedureById("device1", "anomaly", "hint1", "de.hshannover.f4.trust.irondetectprocedures.Mean");

		assertTrue(p1 == p2);
	}
	
	@Test
	public void testGetProcedureByIdNotImplementingProcedure() {
		Procedureable p = repository.getProcedureById("device1", "anomaly", "hint1", "de.somepackage.NoProcedure");
		assertTrue(p == null);
	}
	
}
