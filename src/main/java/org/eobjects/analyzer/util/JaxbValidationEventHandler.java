package org.eobjects.analyzer.util;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JaxbValidationEventHandler implements ValidationEventHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(JaxbValidationEventHandler.class);

	@Override
	public boolean handleEvent(ValidationEvent event) {
		int severity = event.getSeverity();
		if (severity == ValidationEvent.WARNING) {
			logger.warn("encountered JAXB parsing warning: "
					+ event.getMessage());
			return true;
		}

		logger.warn("encountered JAXB parsing error: " + event.getMessage());
		return false;
	}
}
