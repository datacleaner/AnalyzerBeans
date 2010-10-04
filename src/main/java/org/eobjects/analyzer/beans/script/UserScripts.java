package org.eobjects.analyzer.beans.script;

import java.io.File;

import org.eobjects.analyzer.util.JavaClassHandler;
import org.eobjects.analyzer.util.JavaClassHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserScripts {

	private static final Logger logger = LoggerFactory.getLogger(UserScripts.class);
	public static final JavaClassHandler JAVA_CLASS_HANDLER;

	static {
		File classDirectory = new File("user/code/java-classes");
		if (!classDirectory.exists()) {
			logger.debug("Creating directory for Java source and class files: {}", classDirectory.getAbsolutePath());
			if (!classDirectory.mkdirs()) {
				logger.warn("Could not create directory: {}", classDirectory.getAbsolutePath());
			}
		}
		JAVA_CLASS_HANDLER = new JavaClassHandlerImpl(classDirectory);
	}

	private UserScripts() {
		// prevent instantiation
	}
}
