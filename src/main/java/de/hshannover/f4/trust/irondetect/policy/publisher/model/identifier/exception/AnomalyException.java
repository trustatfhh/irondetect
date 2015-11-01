package de.hshannover.f4.trust.irondetect.policy.publisher.model.identifier.exception;

public class AnomalyException extends PolicyIdentifierException{

	private static final long serialVersionUID = 1698326028180708849L;

	public AnomalyException(String msg) {
		super(msg);
	}

	public AnomalyException(String msg, String... args) {
		this(String.format(msg, (Object[]) args));
	}

}
