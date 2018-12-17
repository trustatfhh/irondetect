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
 * Copyright (C) 2010 - 2018 Trust@HsH
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

import de.hshannover.f4.trust.irondetect.model.PolicyData;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;

/**
 * A {@link PolicyHandlerManager} search the right {@link PolicyHandler} by the {@link PolicyData} instance class name.
 *
 * @author Marcel Reichenbach
 */
public class PolicyHandlerManager {

	private static final String POST_CLASS_PATH = "de.hshannover.f4.trust.irondetect.policy.publisher.model.handler.";

	/**
	 * Return a {@link PolicyHandler} by the {@link PolicyData} instance class name.
	 */
	public static PolicyHandler<?> getHandlerFor(PolicyData data) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException {

		Class<?> handlerClazz = Class.forName(POST_CLASS_PATH + "Policy" + data.getClass().getSimpleName() + "Handler");

		PolicyHandler<?> eventHandler = (PolicyHandler<?>) handlerClazz.newInstance();

		return eventHandler;
	}

	/**
	 * Return a {@link PolicyHandler} by the {@link ExtendedIdentifier} instance class name.
	 */
	public static PolicyHandler<?> getHandlerFor(ExtendedIdentifier eIdentifier) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		Class<?> handlerClazz = Class.forName(POST_CLASS_PATH
				+ "Policy" + eIdentifier.getClass().getSimpleName() + "Handler");

		PolicyHandler<?> eventHandler = (PolicyHandler<?>) handlerClazz.newInstance();

		return eventHandler;
	}

}
