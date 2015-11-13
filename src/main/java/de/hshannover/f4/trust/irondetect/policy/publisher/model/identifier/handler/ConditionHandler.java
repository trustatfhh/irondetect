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
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Condition;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;

/**
 * An {@link ConditionHandler} extends the ExtendedIdentifierHandler. It transforms an {@link Condition}-
 * {@link ExtendedIdentifier} to a XML {@link Element}.
 * 
 * @author Marcel Reichenbach
 */
public class ConditionHandler extends ExtendedIdentifierHandler<Condition> {

	@Override
	public Element toExtendedElement(Identifier i, Document doc) throws MarshalException {
		Helpers.checkIdentifierType(i, this);

		Condition condition = (Condition) i;

		String id = condition.getID();
		List<String> expressions = condition.getExpressions();

		if (id == null) {
			throw new MarshalException("No id set");
		}

		if (expressions == null) {
			throw new MarshalException("Condition with null expressions not allowed");
		}

		Element conditionElement = doc.createElementNS(PolicyStrings.POLICY_IDENTIFIER_NS_URI,
				PolicyStrings.CONDITION_EL_NAME);
		Element idElement = doc.createElementNS(null, PolicyStrings.ID_EL_NAME);

		List<Element> expressionElements = buildConditionExpressionElements(expressions, doc);

		idElement.setTextContent(id);

		conditionElement.appendChild(idElement);
		super.appendListAsChild(conditionElement, expressionElements);

		Helpers.addAdministrativeDomain(conditionElement, condition);

		return conditionElement;
	}

	private List<Element> buildConditionExpressionElements(List<String> expressions, Document doc) {
		List<Element> expressionElements = new ArrayList<Element>();

		for (String s : expressions) {
			expressionElements.add(buildConditionExpressionElement(s, doc));
		}

		return expressionElements;
	}

	private Element buildConditionExpressionElement(String expression, Document doc) {
		return super.buildElement(PolicyStrings.CONDITION_EXPRESSION_EL_NAME, super.escapeXml(expression), doc);
	}

	@Override
	public Condition fromExtendedElement(Element element) throws UnmarshalException {

		String ruleId = null;
		List<String> expressionsList = new ArrayList<String>();

		String administrativeDomain = element.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		List<Element> children = DomHelpers.getChildElements(element);

		for (Element childElement : children) {
			if (super.policyElementMatches(childElement, PolicyStrings.ID_EL_NAME)) {
				ruleId = childElement.getTextContent();

			} else if (super.policyElementMatches(childElement, PolicyStrings.CONDITION_EXPRESSION_EL_NAME)) {
				expressionsList.add(childElement.getTextContent());
			}
		}

		if (ruleId == null || ruleId.length() == 0) {
			throw new UnmarshalException("No text content for ruleId found");
		}

		Condition condition = new Condition(ruleId, expressionsList, administrativeDomain);

		return condition;
	}

	@Override
	public Class<Condition> handles() {
		return Condition.class;
	}

}
