/*
 * #%L
 * =====================================================
 *    _____                _     ____  _   _       _   _
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
package de.hshannover.f4.trust.irondetect.procedure;



import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.util.Configuration;

/**
 * Repository to handle classes that implement the {@link Procedureable} interface.
 * 
 * @author ib
 * @author Ralf Steuerwald
 *
 */
public class ProcedureRepository {
	
	private static final Logger logger = Logger.getLogger(ProcedureRepository.class);
	
	private String mProcedureDirectory;
	private Map<String, Procedureable> mLoadedProcedures;
	
	/**
	 * Singleton
	 */
	private static ProcedureRepository mInstance;
	
	public static synchronized ProcedureRepository getInstance(){
		if(mInstance == null){
			mInstance = new ProcedureRepository();
		}
		return mInstance;
	}
	
	private ProcedureRepository() {
		mProcedureDirectory = Configuration.procedureDirectory();
		mLoadedProcedures = new HashMap<String, Procedureable>();

		logger.debug("searching in '" + mProcedureDirectory + "' for jars");
		
	}
	
	/**
	 * Returns always a new {@link Procedureable} instance for the given class name.
	 * 
	 * @param className the class name of the procedure
	 * @return a new procedure instance or null if loading fails
	 */
	public Procedureable newProcedureById(String className) {
		Procedureable p = loadProcedure(className);
		mLoadedProcedures.put(className, p);
		return p;
	}
	
	/**
	 * Returns a {@link Procedureable} instance for the given class name. If the
	 * procedure is already loaded the old instance is returned, otherwise a
	 * fresh object will be created.
	 * 
	 * @param device name of the device
	 * @param anomaly id of anomaly
	 * @param hint id of hint
	 * @param className name of class that implements {@link Procedureable}}
	 * @return a (maybe old) procedure object or null if loading fails
	 */
	public Procedureable getProcedureById(String device, String anomaly, String hint, String className) {
		String key = makeKey(device, anomaly, hint, className);
		if (mLoadedProcedures.containsKey(key)) {
			return mLoadedProcedures.get(key);
		}
		else {
			Procedureable p = loadProcedure(className);
			mLoadedProcedures.put(key, p);
			return p;
		}
	}
	
	/**
	 * Make a key from to identify the instance of any given procedure. The
	 * key is composed as follows: device:anomaly_id:hint_id:class_of_procedure
	 * @param device
	 * @param anomaly
	 * @param hint
	 * @param className
	 * @return "device:anomaly:hint:className"
	 */
	private String makeKey(String device, String anomaly, String hint, String className) {
		return device + ":" + anomaly + ":" + hint + ":" + className;
	}

	/**
	 * Returns a list of {@link URL} instances, containing all jar URL from
	 * the procedure directory.
	 */
	private List<URL> listJarFiles() {
		List<URL> jarFiles = new ArrayList<URL>();
		
		System.out.println(new File(mProcedureDirectory).list());
		
		String[] files = new File(mProcedureDirectory).list();

		for (String f : files) {
			if (f.endsWith(".jar")) {
				try {
					URL u = new File(mProcedureDirectory + File.separator + f).toURI().toURL();
					jarFiles.add(u);
					logger.debug("found jar: " + u);
					
				} catch (MalformedURLException e) {
					logger.warn("could not load " + f + ": " + e.getMessage());
				}
			}
		}
		logger.debug("found " + jarFiles.size() + " jars");
		return jarFiles;
	}
	
	/**
	 * Try to load a class with the given name and returns a fresh instance
	 * of that class. If loading fails, <code>null</code> is returned.
	 */
	private Procedureable loadProcedure(String className) {
		logger.debug("try to load " + className);
		
		List<URL> jars = listJarFiles();
		
		ClassLoader classLoader = URLClassLoader.newInstance(
				jars.toArray(new URL[] {}));
		
		try {
			Class<?> procedure = Class.forName(className, true, classLoader);
			Class<?>[] interfaces = procedure.getInterfaces();
			
			boolean implementsProcedure = false;
			for (Class<?> i : interfaces) {
				if (i.getClass() == Procedureable.class.getClass()) {
					implementsProcedure = true;
				}
			}
			
			if (implementsProcedure) {
				Procedureable p = (Procedureable)procedure.newInstance();
				return p;
			}
			else {
				logger.warn(className + " does not implement " + Procedureable.class.getName());
			}
			
		} catch (ClassNotFoundException e) {
			logger.warn("could not load " + className + ": " + e);
		} catch (InstantiationException e) {
			logger.warn("could not initialize " + className + ": " + e);
		} catch (IllegalAccessException e) {
			logger.warn("could not access " + className + ": " + e);
		}
		
		// loading failed return null 
		return null;
	}
}
