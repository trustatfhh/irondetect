package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.exception;

public class HintException extends PolicyIdentifierException{

	private static final long serialVersionUID = 8649321757703079025L;

	public HintException(String msg) {
		super(msg);
	}

	public HintException(String msg, String... args) {
		this(String.format(msg, (Object[]) args));
	}

}
