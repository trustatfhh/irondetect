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
package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier;

import static de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.exception.PolicyIdentifierException.MSG_CONTEXT_FOR_CONTEXT_ID_NOT_PRESENT;
import static de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.exception.PolicyIdentifierException.MSG_CONTEXT_ID_NOT_PRESENT;
import static de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.exception.PolicyIdentifierException.MSG_EXPRESSION_NOT_PRESENT;
import static de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.exception.PolicyIdentifierException.MSG_INDEX_TOO_BIG_FOR_CONTEXT_ID;
import static de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.exception.PolicyIdentifierException.MSG_INDEX_TOO_BIG_FOR_EXPRESSIONS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hshannover.f4.trust.ifmapj.identifier.IdentifierWithAd;
import de.hshannover.f4.trust.ifmapj.identifier.Identity;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.exception.SignatureException;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.Check;

/**
 * An {@link Signature} is an extended identifier. It is represented by an {@link Identity} identifier with IdentityType
 * = other
 * ,other-type = extended and with an administrative domain.
 * 
 * @author Marcel Reichenbach
 */
public class Signature extends IdentifierWithAd implements ExtendedIdentifier {

	private String mID;

	private List<String> mExpressions;

	private Map<String, List<String>> mContext;

	/**
	 * The {@link Signature} constructor. Checks the parameter, if null throws {@link NullPointerException}.
	 * 
	 * @param id The {@link Signature}-ID
	 * @param expressions All expressions for the {@link Signature}
	 * @param admDom The administrative domain for {@link Signature}
	 * @param context All context for the {@link Signature}
	 */
	public Signature(String id, List<String> expressions, String admDom, Map<String, List<String>> context) {
		super(admDom);

		Check.ifNull(id, String.format(Check.MSG_PARAMETER_IS_NULL, "id"));
		Check.ifNull(expressions, String.format(Check.MSG_PARAMETER_IS_NULL, "expressions"));
		Check.ifNull(admDom, String.format(Check.MSG_PARAMETER_IS_NULL, "admDom"));
		Check.ifNull(context, String.format(Check.MSG_PARAMETER_IS_NULL, "context"));

		setId(id);
		mExpressions = expressions;
		mContext = context;
	}

	/**
	 * The {@link Signature} constructor. Checks the parameter, if null throws {@link NullPointerException}.
	 * Initializes with an empty expression- and context-collection.
	 * 
	 * @param id The {@link Signature}-ID
	 * @param admDom The administrative domain for {@link Signature}
	 */
	public Signature(String id, String admDom) {
		super(admDom);

		Check.ifNull(id, String.format(Check.MSG_PARAMETER_IS_NULL, "id"));
		Check.ifNull(admDom, String.format(Check.MSG_PARAMETER_IS_NULL, "admDom"));

		mExpressions = new ArrayList<String>();
		mContext = new HashMap<String, List<String>>();

		setId(id);
	}

	/**
	 * 
	 * @return {@link Signature}-ID
	 */
	public String getID() {
		return mID;
	}

	/**
	 * Set the {@link Signature}-ID.
	 * Checks the parameter id, if null throws {@link NullPointerException}.
	 * 
	 * @param id {@link Signature}-ID
	 */
	public void setId(String id) {
		Check.ifNull(id, String.format(Check.MSG_PARAMETER_IS_NULL, "id"));

		mID = id;
	}

	/**
	 * 
	 * @return A expressions copy
	 */
	public List<String> getExpressions() {
		return new ArrayList<String>(mExpressions);
	}

	/**
	 * Add a new expression.
	 * Checks the parameter expression, if null throws {@link NullPointerException}.
	 * 
	 * @param expression The expression String
	 */
	public void addFeatureExpression(String expression) {
		Check.ifNull(expression, String.format(Check.MSG_PARAMETER_IS_NULL, "expression"));

		mExpressions.add(expression);
	}

	/**
	 * Checks the parameter index, if index < 0 throws {@link IndexOutOfBoundsException}.
	 * 
	 * @param index
	 * @throws SignatureException If the index is too big for expression list-size.
	 */
	public void removeExpression(int index) throws SignatureException {
		Check.indexNumber(index, String.format(Check.MSG_IS_LESS_THAN_ZERO, index));

		if (index < mExpressions.size()) {
			mExpressions.remove(index);
		} else {
			throw new SignatureException(MSG_INDEX_TOO_BIG_FOR_EXPRESSIONS, String.valueOf(index));
		}
	}

	/**
	 * Checks the parameter, if null throws {@link NullPointerException}.
	 * 
	 * @param expression
	 * @throws SignatureException If the expression is not found
	 */
	public void removeExpression(String expression) throws SignatureException {
		Check.ifNull(expression, String.format(Check.MSG_PARAMETER_IS_NULL, "expression"));

		if (mExpressions.contains(expression)) {
			mExpressions.remove(expression);
		} else {
			throw new SignatureException(MSG_EXPRESSION_NOT_PRESENT, expression);
		}
	}

	/**
	 * 
	 * @return A {@link Map} copy of context
	 */
	public Map<String, List<String>> getContext() {
		return new HashMap<String, List<String>>(mContext);
	}

	/**
	 * Checks the parameters, if null throws {@link NullPointerException}.
	 * 
	 * @param contextId
	 * @param context
	 */
	public void addContext(String contextId, String context) {
		Check.ifNull(contextId, String.format(Check.MSG_PARAMETER_IS_NULL, "contextId"));
		Check.ifNull(context, String.format(Check.MSG_PARAMETER_IS_NULL, "context"));

		if (!mContext.containsKey(contextId)) {
			mContext.put(contextId, new ArrayList<String>());
		}

		mContext.get(contextId).add(context);
	}

	/**
	 * Checks the parameter contextId, if null throws {@link NullPointerException}.
	 * 
	 * @param contextId
	 * @throws SignatureException If the contextId is not found
	 */
	public void removeContext(String contextId) throws SignatureException {
		Check.ifNull(contextId, String.format(Check.MSG_PARAMETER_IS_NULL, "contextId"));

		if (mContext.containsKey(contextId)) {
			mContext.remove(contextId);
		} else {
			throw new SignatureException(MSG_CONTEXT_ID_NOT_PRESENT, contextId);
		}
	}

	/**
	 * Checks the parameters, if null throws {@link NullPointerException}.
	 * 
	 * @param contextId
	 * @param context
	 * @throws SignatureException
	 */
	public void removeContext(String contextId, String context) throws SignatureException {
		Check.ifNull(contextId, String.format(Check.MSG_PARAMETER_IS_NULL, "contextId"));
		Check.ifNull(context, String.format(Check.MSG_PARAMETER_IS_NULL, "context"));

		if (mContext.containsKey(contextId)) {
			if (mContext.get(contextId).contains(context)) {
				mContext.get(contextId).remove(context);
			} else {
				throw new SignatureException(MSG_CONTEXT_FOR_CONTEXT_ID_NOT_PRESENT, context, contextId);
			}
		} else {
			throw new SignatureException(MSG_CONTEXT_ID_NOT_PRESENT, contextId);
		}
	}

	/**
	 * Checks the parameter contextId, if null throws {@link NullPointerException}.
	 * Checks the parameter contextIndex, if index < 0 throws {@link IndexOutOfBoundsException}.
	 * 
	 * @param contextId
	 * @param contextIndex
	 * @throws SignatureException If context id not present or index too big for context id
	 */
	public void removeContext(String contextId, int contextIndex) throws SignatureException {
		Check.ifNull(contextId, String.format(Check.MSG_PARAMETER_IS_NULL, "contextId"));
		Check.indexNumber(contextIndex, String.format(Check.MSG_IS_LESS_THAN_ZERO, contextIndex));

		if (mContext.containsKey(contextId)) {
			if (contextIndex < mContext.get(contextId).size()) {
				mContext.get(contextId).remove(contextIndex);
			} else {
				throw new SignatureException(MSG_INDEX_TOO_BIG_FOR_CONTEXT_ID, String.valueOf(contextIndex));
			}
		} else {
			throw new SignatureException(MSG_CONTEXT_ID_NOT_PRESENT, contextId);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(getClass().getSimpleName());
		sb.append('(');
		sb.append(getID());
		sb.append(' ');
		sb.append(':');

		for (String s : getExpressions()) {
			sb.append(' ');
			sb.append(s);
		}

		sb.append(')');

		return sb.toString();
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + getID().hashCode();
		result = prime * result + getAdministrativeDomain().hashCode();

		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}

		if (other == this) {
			return true;
		}

		if (!(other instanceof Signature)) {
			return false;
		}

		Signature otherItem = (Signature) other;

		if (!getID().equals(otherItem.getID())) {
			return false;
		}

		return super.equals(otherItem);
	}
}
