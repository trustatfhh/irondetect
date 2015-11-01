package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers.Helpers;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Signature;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;

/**
 * An {@link SignatureHandler} extends the ExtendedIdentifierHandler. It transforms an {@link Signature}-
 * {@link ExtendedIdentifier} to a XML {@link Element}.
 * 
 * @author Marcel Reichenbach
 */
public class SignatureHandler extends ExtendedIdentifierHandler<Signature> {

	@Override
	public Element toExtendedElement(Identifier i, Document doc) throws MarshalException {
		Helpers.checkIdentifierType(i, this);

		Signature signature = (Signature) i;

		String id = signature.getID();
		List<String> expressions = signature.getExpressions();
		Map<String, List<String>> context = signature.getContext();

		if (id == null) {
			throw new MarshalException("No id set");
		}

		if (expressions == null) {
			throw new MarshalException("Signature with null expressions not allowed");
		}

		if (context == null) {
			throw new MarshalException("Signature with null context not allowed");
		}

		Element signatureElement = doc.createElementNS(PolicyStrings.POLICY_IDENTIFIER_NS_URI,
				PolicyStrings.SIGNATURE_EL_NAME);
		Element idElement = doc.createElementNS(null, PolicyStrings.ID_EL_NAME);

		List<Element> expressionElements = buildFeatureExpressionElements(expressions, doc);
		List<Element> contextElements = super.buildContextElements(context, doc);

		idElement.setTextContent(id);

		signatureElement.appendChild(idElement);
		super.appendListAsChild(signatureElement, expressionElements);
		super.appendListAsChild(signatureElement, contextElements);

		Helpers.addAdministrativeDomain(signatureElement, signature);

		return signatureElement;
	}

	private Element buildFeatureExpressionElement(String expression, Document doc) {
		return super.buildElement(PolicyStrings.FEATURE_EXPRESSION_EL_NAME, super.escapeXml(expression), doc);
	}

	private List<Element> buildFeatureExpressionElements(List<String> expressions, Document doc) {
		List<Element> expressionElements = new ArrayList<Element>();

		for (String s : expressions) {
			expressionElements.add(buildFeatureExpressionElement(s, doc));
		}

		return expressionElements;
	}

	@Override
	public Class<Signature> handles() {
		return Signature.class;
	}

}
