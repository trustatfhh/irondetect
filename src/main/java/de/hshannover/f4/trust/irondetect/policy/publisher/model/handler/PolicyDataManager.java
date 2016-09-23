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
package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import org.w3c.dom.Element;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.irondetect.model.PolicyData;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler.ExtendedIdentifierHandler;

/**
 * A {@link PolicyDataManager} transforms an irondetect {@link PolicyData} to an {@link ExtendedIdentifier}.
 *
 * @author Marcel Reichenbach
 */
public class PolicyDataManager {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ExtendedIdentifier transformPolicyData(PolicyData data) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException {

		PolicyHandler handler = PolicyHandlerManager.getHandlerFor(data);

		return handler.toIdentifier(data);
	}

	@SuppressWarnings("rawtypes")
	public static PolicyData transformIdentifier(ExtendedIdentifier eIdentifier) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException, UnmarshalException {

		PolicyHandler handler = PolicyHandlerManager.getHandlerFor(eIdentifier);

		return handler.fromIdentifier(eIdentifier);
	}

	@SuppressWarnings("unchecked")
	public static PolicyData transformElement(Element policyElemen)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnmarshalException {

		ExtendedIdentifierHandler<? extends ExtendedIdentifier> eIh =
				(ExtendedIdentifierHandler<? extends ExtendedIdentifier>) Identifiers.getHandlerFor(policyElemen);

		ExtendedIdentifier identifier = eIh.fromElement(policyElemen);

		return transformIdentifier(identifier);
	}

}
