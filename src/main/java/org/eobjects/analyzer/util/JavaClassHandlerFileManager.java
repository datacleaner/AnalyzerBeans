package org.eobjects.analyzer.util;

import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JavaClassHandlerFileManager extends ForwardingJavaFileManager<JavaFileManager> {

	private static final Logger logger = LoggerFactory.getLogger(JavaClassHandlerFileManager.class);

	private ClassLoader _classPathClassLoader;

	public JavaClassHandlerFileManager(JavaFileManager fileManager, ClassLoader classPathClassLoader) {
		super(fileManager);
		_classPathClassLoader = classPathClassLoader;
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		if (location == StandardLocation.CLASS_PATH) {
			logger.debug("Intercepting CLASS_PATH location and setting classloader to provided classloader");
			return _classPathClassLoader;
		}
		return super.getClassLoader(location);
	}
}
