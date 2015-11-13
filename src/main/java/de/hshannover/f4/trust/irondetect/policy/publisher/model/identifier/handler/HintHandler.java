package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.DomHelpers;
import de.hshannover.f4.trust.ifmapj.binding.IfmapStrings;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers.Helpers;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Hint;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;

/**
 * An {@link HintHandler} extends the ExtendedIdentifierHandler. It transforms an {@link Hint}-
 * {@link ExtendedIdentifier} to a XML {@link Element}.
 * 
 * @author Marcel Reichenbach
 */
public class HintHandler extends ExtendedIdentifierHandler<Hint> {

	@Override
	public Element toExtendedElement(Identifier i, Document doc) throws MarshalException {
		Helpers.checkIdentifierType(i, this);

		Hint hint = (Hint) i;

		String id = hint.getID();
		List<String> expressions = hint.getExpressions();

		if (id == null) {
			throw new MarshalException("No id set");
		}

		if (expressions == null) {
			throw new MarshalException("Hint with null expressions not allowed");
		}

		Element hintElement = doc.createElementNS(PolicyStrings.POLICY_IDENTIFIER_NS_URI,
				PolicyStrings.HINT_EL_NAME);
		Element idElement = doc.createElementNS(null, PolicyStrings.ID_EL_NAME);

		List<Element> expressionElements = buildProcedureExpressionElements(expressions, doc);

		idElement.setTextContent(id);

		hintElement.appendChild(idElement);
		super.appendListAsChild(hintElement, expressionElements);

		Helpers.addAdministrativeDomain(hintElement, hint);

		return hintElement;
	}

	private List<Element> buildProcedureExpressionElements(List<String> expressions, Document doc) {
		List<Element> expressionElements = new ArrayList<Element>();

		for (String s : expressions) {
			expressionElements.add(buildProcedureExpressionElement(s, doc));
		}

		return expressionElements;
	}

	private Element buildProcedureExpressionElement(String expression, Document doc) {
		return super.buildElement(PolicyStrings.PROCEDURE_EXPRESSION_EL_NAME, super.escapeXml(expression), doc);
	}

	@Override
	public Hint fromExtendedElement(Element element) throws UnmarshalException {

		String ruleId = null;
		List<String> expressionsList = new ArrayList<String>();

		String administrativeDomain = element.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		List<Element> children = DomHelpers.getChildElements(element);

		for (Element childElement : children) {
			if (super.policyElementMatches(childElement, PolicyStrings.ID_EL_NAME)) {
				ruleId = childElement.getTextContent();

			} else if (super.policyElementMatches(childElement, PolicyStrings.PROCEDURE_EXPRESSION_EL_NAME)) {
				expressionsList.add(childElement.getTextContent());
			}
		}

		if (ruleId == null || ruleId.length() == 0) {
			throw new UnmarshalException("No text content for ruleId found");
		}

		Hint hint = new Hint(ruleId, expressionsList, administrativeDomain);

		return hint;
	}

	@Override
	public Class<Hint> handles() {
		return Hint.class;
	}

}