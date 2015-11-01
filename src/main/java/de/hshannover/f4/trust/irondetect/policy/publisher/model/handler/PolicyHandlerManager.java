package de.hshannover.f4.trust.irondetect.policy.publisher.model.handler;

import de.hshannover.f4.trust.irondetect.model.PolicyData;

/**
 * A {@link PolicyHandlerManager} search the right {@link PolicyHandler} by the {@link PolicyData} instance class name.
 * 
 * @author Marcel Reichenbach
 */
public class PolicyHandlerManager {

	private static final String POST_CLASS_PATH = "de.hshannover.f4.trust.irondetect.policy.publisher.model.handler.";
	
	/**
	 * Return a {@link PolicyHandler} by the {@link PolicyData} instance class name.
	 */
	public static PolicyHandler<?> getHandlerFor(PolicyData data) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {

		Class<?> handlerClazz = Class.forName(POST_CLASS_PATH + "Policy" + data.getClass().getSimpleName() + "Handler");

		PolicyHandler<?> eventHandler = (PolicyHandler<?>) handlerClazz.newInstance();

		return eventHandler;
	}

}