package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.exception;

public class ActionException extends PolicyIdentifierException{

	private static final long serialVersionUID = 2344144982931402294L;

	public ActionException(String msg) {
		super(msg);
	}

	public ActionException(String msg, String... args) {
		this(String.format(msg, (Object[]) args));
	}

}
