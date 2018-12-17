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
package de.hshannover.f4.trust.irondetect.livechecker.rest;

import java.util.Set;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * A class that constructs a {@link HttpServer} for REST resources of the
 * dataservice.
 *
 * @author Bastian Hellman
 * @author Marcel Reichenbach
 * @author Ralf Steuerwald
 *
 */
public class RestService implements Runnable {

	private static final Logger log = Logger.getLogger(RestService.class);

	private String mUrl;

	private Set<Class<?>> mClasses;

	/**
	 * Creates a new {@link RestService} with a given URL and a set of classes
	 * to load as REST resources. Adds the following classes to this set before
	 * handling it over to a {@link GrizzlyServerFactory}:
	 * <ul>
	 * <li> {@link LiveCheckerResource}
	 * <li> {@link GraphResource}
	 * <li> {@link SubscribeResource}
	 * </ul>
	 *
	 * @param url
	 *            the URL the REST service shall be running at
	 * @param classes
	 *            a {@link Set} of {@link Class} that contain REST resources
	 */
	public RestService(String url, Set<Class<?>> classes) {
		this.mUrl = url;
		this.mClasses = classes;
		this.mClasses.add(LiveCheckerResource.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		log.trace("run() ...");

		ResourceConfig resourceConfig = new DefaultResourceConfig(mClasses);
		resourceConfig.getContainerResponseFilters().add(
				CrossOriginResourceSharingFilter.class);
		log.info("Starting REST service on '" + mUrl + "'.");

		log.debug("Using REST resources:");
		for (Class<?> clazz : mClasses) {
			log.debug(clazz.getPackage() + "." + clazz.getSimpleName());
		}

		try {
			HttpServer server = GrizzlyServerFactory.createHttpServer(mUrl,
					resourceConfig);
			if (server.isStarted()) {
				log.debug("REST service running.");
			} else {
				log.warn("REST service NOT running.");
			}
			// TODO shutdown server properly
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			synchronized (this) {
				wait(); // FIXME
			}
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}
}
