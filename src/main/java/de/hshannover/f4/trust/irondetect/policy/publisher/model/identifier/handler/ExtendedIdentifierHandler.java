package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.DomHelpers;
import de.hshannover.f4.trust.ifmapj.binding.IfmapStrings;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.IdentifierHandler;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers.Helpers;
import de.hshannover.f4.trust.ifmapj.identifier.Identity;
import de.hshannover.f4.trust.ifmapj.log.IfmapJLog;
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.ExtendedIdentifier;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.DocumentUtils;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;

/**
 * An {@link ExtendedIdentifierHandler} transforms an {@link ExtendedIdentifier} to a XML {@link Element}. This XML
 * {@link Element} is represented by an {@link Identity} identifier with IdentityType = 'other' and other-type =
 * 'extended'.
 * 
 * @author Marcel Reichenbach
 */
public abstract class ExtendedIdentifierHandler<T extends ExtendedIdentifier> implements IdentifierHandler<T> {

	private static DocumentBuilder mDocumentBuilder;

	static {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			mDocumentBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IfmapJLog.error("Could not get DocumentBuilder instance [" + e.getMessage() + "]");
			throw new RuntimeException(e);
		}
	}

	public abstract Element toExtendedElement(Identifier i, Document doc) throws MarshalException;

	@Override
	public Element toElement(Identifier i, Document doc) throws MarshalException {
		Helpers.checkIdentifierType(i, this);

		Document extendetDocument = mDocumentBuilder.newDocument();

		Element extendetElement = toExtendedElement(i, extendetDocument);
		extendetDocument.appendChild(extendetElement);

		Identity identity = Identifiers.createExtendedIdentity(extendetDocument);

		return Identifiers.toElement(identity, doc);
	}

	@Override
	public T fromElement(Element element) throws UnmarshalException {
		// Are we responsible? return null if not.
		if (!DomHelpers.elementMatches(element, IfmapStrings.IDENTITY_EL_NAME)) {
			return null;
		}

		IdentifierHandler<?> ih = Identifiers.getHandlerFor(Identity.class);

		Identifier identifier = ih.fromElement(element);
		
		if(identifier instanceof Identity){
			Identity identity = (Identity) identifier;
			Element rootElement = getExtendedRootElement(identity.getName());
			
			if (rootElement != null) {
				return fromExtendedElement(rootElement);
			}
		}
		return null;
	}

	public abstract T fromExtendedElement(Element e) throws UnmarshalException;

	public Element getExtendedRootElement(String xmlExtendedElement) {

		Document extendetIdentifier = DocumentUtils.parseEscapedXmlString(xmlExtendedElement);

		if (extendetIdentifier != null) {
			Element rootElement = extendetIdentifier.getDocumentElement();
			return rootElement;
		}
		return null;
	}

	protected void appendListAsChild(Element parent, List<Element> elementList) {
		for (Element e : elementList) {
			parent.appendChild(e);
		}
	}

	protected Element buildElement(String elementQualifiedName, String textContent, Document doc) {
		Element featureExpressionElement = doc.createElementNS(null, elementQualifiedName);
		featureExpressionElement.setTextContent(textContent);
		return featureExpressionElement;
	}

	protected Element buildParameterExpression(String expression, Document doc) {
		return buildElement(PolicyStrings.PARAMETER_EXPRESSION_EL_NAME, escapeXml(expression), doc);
	}

	protected static String escapeXml(String input) {

		String ret = input;

		String[] unwanted = { "&", "<", ">", "\"", "'" };
		String[] replaceBy = { "&amp;", "&lt;", "&gt;", "&quot;", "&apos;" };

		for (int i = 0; i < unwanted.length; i++) {
			ret = ret.replace(unwanted[i], replaceBy[i]);
		}

		return ret;
	}

	protected Element buildContextElement(String id, List<Element> parameterExpressionElements, Document doc) {
		Element contextElement = doc.createElementNS(null, PolicyStrings.CONTEXT_EL_NAME);
		Element idElement = buildElement(PolicyStrings.ID_EL_NAME, id, doc);

		contextElement.appendChild(idElement);

		for (Element e : parameterExpressionElements) {
			contextElement.appendChild(e);
		}

		return contextElement;
	}

	protected List<Element> buildContextElements(Map<String, List<String>> context, Document doc) {
		List<Element> contextElements = new ArrayList<Element>();

		for (Entry<String, List<String>> entry : context.entrySet()) {
			List<Element> parameterExpressions = new ArrayList<Element>();
			for (String parameterExpression : entry.getValue()) {
				parameterExpressions.add(buildParameterExpression(parameterExpression, doc));
			}
			contextElements.add(buildContextElement(entry.getKey(), parameterExpressions, doc));
		}

		return contextElements;
	}

	protected final boolean policyElementMatches(Element e, String elementName ){
		return elementName.equals(e.getNodeName());
	}
}
