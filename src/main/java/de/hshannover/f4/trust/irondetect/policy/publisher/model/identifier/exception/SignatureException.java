package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.exception;

public class SignatureException extends PolicyIdentifierException{

	private static final long serialVersionUID = 1L;

	public SignatureException(String msg) {
		super(msg);
	}

	public SignatureException(String msg, String... args) {
		this(String.format(msg, (Object[]) args));
	}

}
