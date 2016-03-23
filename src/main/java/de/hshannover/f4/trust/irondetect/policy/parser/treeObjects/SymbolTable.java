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
 * This file is part of irondetect, version 0.0.9, 
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
package de.hshannover.f4.trust.irondetect.policy.parser.treeObjects;



import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hshannover.f4.trust.irondetect.model.Action;
import de.hshannover.f4.trust.irondetect.model.Anomaly;
import de.hshannover.f4.trust.irondetect.model.Condition;
import de.hshannover.f4.trust.irondetect.model.Context;
import de.hshannover.f4.trust.irondetect.model.Feature;
import de.hshannover.f4.trust.irondetect.model.Hint;
import de.hshannover.f4.trust.irondetect.model.Rule;
import de.hshannover.f4.trust.irondetect.model.Signature;

public class SymbolTable {
	private Set<String> _symbols = new HashSet<String>();
	private Map<String, Action> _actions = new HashMap<String, Action>();
	private Map<String, Anomaly> _anomalies = new HashMap<String, Anomaly>();
	private Map<String, Condition> _conditions = new HashMap<String, Condition>();
	private Map<String, Context> _contexts = new HashMap<String, Context>();
	private Map<String, Hint> _hints = new HashMap<String, Hint>();
	private Map<String, Signature> _signatures = new HashMap<String, Signature>();
	private Map<String, Feature> _features = new HashMap<String, Feature>();
	private Map<String, Rule> _rules = new HashMap<String, Rule>();
	private Map<String, Set<String>> _con_feature = new HashMap<String, Set<String>>();

	public Action getAction(String identifier) {
		if (_actions.containsKey(identifier)) {
			return _actions.get(identifier);
		}
		return null;
		// TODO throw error
	}

	public void addAction(String identifier, Action a) {
		if (!_symbols.contains(identifier)) {
			_symbols.add(identifier);
			_actions.put(identifier, a);
		}
		// TODO throw error
	}

	public boolean containsAction(String identifier) {
		return _actions.containsKey(identifier);
	}

	public Anomaly getAnomaly(String identifier) {
		if (containsAnomaly(identifier)) {
			return _anomalies.get(identifier);
		}
		return null;
		// TODO throw error
	}

	public void addAnomaly(String identifier, Anomaly a) {
		if (!_symbols.contains(identifier)) {
			_symbols.add(identifier);
			_anomalies.put(identifier, a);
		}
		// TODO throw error
	}

	public boolean containsAnomaly(String identifier) {
		return _anomalies.containsKey(identifier);
	}

	public Condition getCondition(String identifier) {
		if (containsCondition(identifier)) {
			return _conditions.get(identifier);
		}
		return null;
		// TODO throw error
	}

	public void addCondition(String identifier, Condition c) {
		if (!_symbols.contains(identifier)) {
			_symbols.add(identifier);
			_conditions.put(identifier, c);
		}
		// TODO throw error
	}

	public boolean containsCondition(String identifier) {
		return _conditions.containsKey(identifier);
	}

	public Context getContext(String identifier) {
		if (containsContext(identifier)) {
			return _contexts.get(identifier);
		}
		return null;
		// TODO throw error
	}

	public void addContext(String identifier, Context c) {
		if (!_symbols.contains(identifier)) {
			_symbols.add(identifier);
			_contexts.put(identifier, c);
		}
		// TODO throw error
	}

	public boolean containsContext(String identifier) {
		return _contexts.containsKey(identifier);
	}

	public Hint getHint(String identifier) {
		if (containsHint(identifier)) {
			return _hints.get(identifier);
		}
		return null;
		// TODO throw error
	}

	public void addHint(String identifier, Hint h) {
		if (!_symbols.contains(identifier)) {
			_symbols.add(identifier);
			_hints.put(identifier, h);
		}
		// TODO throw error
	}

	public boolean containsHint(String identifier) {
		return _hints.containsKey(identifier);
	}

	public Signature getSignature(String identifier) {
		if (containsSignature(identifier)) {
			return _signatures.get(identifier);
		}
		return null;
		// TODO throw error
	}

	public void addSignature(String identifier, Signature s) {
		if (!_symbols.contains(identifier)) {
			_symbols.add(identifier);
			_signatures.put(identifier, s);
		}
		// TODO throw error
	}

	public boolean containsSignature(String identifer) {
		return _signatures.containsKey(identifer);
	}

	public Feature getFeature(String identifier) {
		if (containsFeature(identifier)) {
			return _features.get(identifier);
		}
		return null;
		// TODO throw error
	}

	public void addFeature(String identifier, Feature f) {
		if (!_symbols.contains(identifier)) {
			_symbols.add(identifier);
			_features.put(identifier, f);
		}
		// TODO throw error
	}

	public boolean containsFeature(String identifier) {
		return _features.containsKey(identifier);
	}

	public Rule getRule(String identifier) {
		if (containsRule(identifier)) {
			return _rules.get(identifier);
		}
		return null;
		// TODO throw error
	}

	public void addRule(String identifier, Rule r) {
		if (!_symbols.contains(identifier)) {
			_symbols.add(identifier);
			_rules.put(identifier, r);
		}
		// TODO throw error
	}

	public boolean containsRule(String identifier) {
		return _rules.containsKey(identifier);
	}

	public Set<String> getFeaturesForCondition(String identifier) {
		if (containsFeaturesForCondition(identifier)) {
			return _con_feature.get(identifier);
		}
		return null;
		// TODO throw error
	}

	public void addFeatureForCondition(String identifier, String f) {
		Set<String> features = null;
		if (containsFeaturesForCondition(identifier)) {
			features = _con_feature.get(identifier);
		} else {
			features = new HashSet<String>();
			_con_feature.put(identifier, features);
		}
		features.add(f);
	}

	public boolean containsFeaturesForCondition(String identifier) {
		return _con_feature.containsKey(identifier);
	}
}
