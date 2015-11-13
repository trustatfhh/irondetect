package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.handler;

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
import de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.Policy;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;

/**
 * An {@link PolicyHandler} extends the ExtendedIdentifierHandler. It transforms an {@link Policy}-
 * {@link ExtendedIdentifier} to a XML {@link Element}.
 * 
 * @author Marcel Reichenbach
 */
public class PolicyHandler extends ExtendedIdentifierHandler<Policy> {

	@Override
	public Element toExtendedElement(Identifier i, Document doc) throws MarshalException {
		Helpers.checkIdentifierType(i, this);

		Policy policy = (Policy) i;

		String id = policy.getID();

		if (id == null) {
			throw new MarshalException("No id set");
		}

		Element policyElement = doc.createElementNS(PolicyStrings.POLICY_IDENTIFIER_NS_URI,
				PolicyStrings.POLICY_EL_NAME);
		Element idElement = doc.createElementNS(null, PolicyStrings.ID_EL_NAME);
		idElement.setTextContent(id);

		policyElement.appendChild(idElement);

		Helpers.addAdministrativeDomain(policyElement, policy);

		return policyElement;
	}
	
	@Override
	public Policy fromExtendedElement(Element element) throws UnmarshalException {

		Element child = null;

		String administrativeDomain = element.getAttribute(IfmapStrings.IDENTIFIER_ATTR_ADMIN_DOMAIN);
		List<Element> children = DomHelpers.getChildElements(element);

		if (children.size() != 1) {
			throw new UnmarshalException("Bad " + PolicyStrings.POLICY_EL_NAME + " element? Has " + children.size()
					+ " child elements.");
		}

		child = children.get(0);

		if (!super.policyElementMatches(child, PolicyStrings.ID_EL_NAME)) {
			throw new UnmarshalException("Unknown child element in " + PolicyStrings.POLICY_EL_NAME + " element: "
					+ child.getLocalName());
		}

		String ruleId = child.getTextContent();

		if (ruleId == null || ruleId.length() == 0) {
			throw new UnmarshalException("No text content for ruleId found");
		}

		Policy policy = new Policy(ruleId, administrativeDomain);

		return policy;
	}

	@Override
	public Class<Policy> handles() {
		return Policy.class;
	}

}
