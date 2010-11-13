/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
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
