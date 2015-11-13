package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.DomHelpers;
import de.hshannover.f4.trust.ifmapj.binding.IfmapStrings;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers.Helpers;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Anomaly;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;

/**
 * An {@link AnomalyHandler} extends the ExtendedIdentifierHandler. It transforms an {@link Anomaly}-
 * {@link ExtendedIdentifier} to a XML {@link Element}.
 * 
 * @author Marcel Reichenbach
 */
public class AnomalyHandler extends ExtendedIdentifierHandler<Anomaly> {

	@Override
	public Element toExtendedElement(Identifier i, Document doc) throws MarshalException {
		Helpers.checkIdentifierType(i, this);

		Anomaly anomaly = (Anomaly) i;

		String id = anomaly.getID();
		List<String> expressions = anomaly.getExpressions();
		Map<String, List<String>> context = anomaly.getContext();

		if (id == null) {
			throw new MarshalException("No id set");
		}

		if (expressions == null) {
			throw new MarshalException("Anomaly with null expressions not allowed");
		}

		if (context == null) {
			throw new MarshalException("Anomaly with null context not allowed");
		}

		Element anomalyElement = doc.createElementNS(PolicyStrings.POLICY_IDENTIFIER_NS_URI,
				PolicyStrings.ANOMALY_EL_NAME);
		Element idElement = doc.createElementNS(null, PolicyStrings.ID_EL_NAME);

		List<Element> expressionElements = buildHintExpressionElements(expressions, doc);
		List<Element> contextElements = super.buildContextElements(context, doc);

		idElement.setTextContent(id);

		anomalyElement.appendChild(idElement);
		super.appendListAsChild(anomalyElement, expressionElements);
		super.appendListAsChild(anomalyElement, contextElements);

		Helpers.addAdministrativeDomain(anomalyElement, anomaly);

		return anomalyElement;
	}

	private List<Element> buildHintExpressionElements(List<String> expressions, Document doc) {
		List<Element> expressionElements = new ArrayList<Element>();

		for (String s : expressions) {
			expressionElements.add(buildHintExpressionElement(s, doc));
		}

		return expressionElements;
	}

	private Element buildHintExpressionElement(String expression, Document doc) {
		return super.buildElement(PolicyStrings.HINT_EXPRESSION_EL_NAME, super.escapeXml(expression), doc);
	}

	@Override
	public Anomaly fromExtendedElement(Element element) throws UnmarshalException {

		String ruleId = null;
		List<String> expressionList = new ArrayList<String>();
		Map<String, List<String>> contextList = new HashMap<String, List<String>>();

		String administrativeDomain = element.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		List<Element> children = DomHelpers.getChildElements(element);

		for (Element childElement : children) {
			if (super.policyElementMatches(childElement, PolicyStrings.ID_EL_NAME)) {
				ruleId = childElement.getTextContent();
			} else if (super.policyElementMatches(childElement, PolicyStrings.HINT_EXPRESSION_EL_NAME)) {
				expressionList.add(childElement.getTextContent());
			} else if (super.policyElementMatches(childElement, PolicyStrings.CONTEXT_EL_NAME)) {
				// context elements
				String contextId = null;
				List<String> parameterExpressionList = new ArrayList<String>();

				List<Element> contextChildren = DomHelpers.getChildElements(childElement);

				for (Element contextChildElement : contextChildren) {
					if (super.policyElementMatches(contextChildElement, PolicyStrings.ID_EL_NAME)) {
						contextId = contextChildElement.getTextContent();
					} else if (super.policyElementMatches(contextChildElement,
							PolicyStrings.PARAMETER_EXPRESSION_EL_NAME)) {
						parameterExpressionList.add(contextChildElement.getTextContent());
					}
				}

				contextList.put(contextId, parameterExpressionList);
			}
		}

		if (ruleId == null || ruleId.length() == 0) {
			throw new UnmarshalException("No text content for ruleId found");
		}

		Anomaly anomaly = new Anomaly(ruleId, expressionList, administrativeDomain, contextList);

		return anomaly;
	}

	@Override
	public Class<Anomaly> handles() {
		return Anomaly.class;
	}

}
