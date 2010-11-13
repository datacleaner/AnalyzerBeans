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
