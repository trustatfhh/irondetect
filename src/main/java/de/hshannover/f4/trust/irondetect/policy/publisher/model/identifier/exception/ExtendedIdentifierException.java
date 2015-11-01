package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.exception;

public class ExtendedIdentifierException extends Exception {

	private static final long serialVersionUID = -660777274915469023L;

	public ExtendedIdentifierException(String msg) {
		super(msg);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [Message: " + super.getMessage() + "]";
	}
}
