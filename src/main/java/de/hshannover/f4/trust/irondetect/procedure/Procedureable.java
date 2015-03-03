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
package de.hshannover.f4.trust.irondetect.procedure;



import java.util.List;

import de.hshannover.f4.trust.Feature;
import de.hshannover.f4.trust.irondetect.model.Context;
import de.hshannover.f4.trust.irondetect.model.ProcedureResult;
import java.util.Calendar;

/**
 * This must be implemented by any procedure that is loaded and used by
 * irondetect.
 *  
 * @author ib
 *
 */
public interface Procedureable {
	
	/**
	 * All configuration and initialization stuff that might be needed.
	 */
	public void setUp(String config);
	
	/**
	 * Calculate a {@link ProcedureResult}. Depending on the implementation, this
	 * might be stateful or stateless. Computation may take some time ...
	 * 
	 * @param featureSet
	 * @param contextSet
	 * @return the calculated value mapped to the range of [-1,1]
	 */
	public ProcedureResult calculate(List<Feature> featureSet, List<Context> contextSet);
	
	/**
	 * Train the given featureSet. Depending on the implementation, this
	 * might be stateful or stateless. Computation may take some time ...
	 * This method does not return anythin. The expected value is trained for
	 * the procedure. This expected value will be used later to do the mapping
	 * to an {@link ProcedureResult} when calculate is called during testing mode.
	 * 
	 * @param featureSet
	 * @param contextSet
	 * 
	 */
	public void train(List<Feature> featureSet, List<Context> contextSet, Calendar startOfTraining, Calendar endOfTraining);

	/**
	 * Clean up the procedure.
	 */
	public void tearDown(String config);
	
}
