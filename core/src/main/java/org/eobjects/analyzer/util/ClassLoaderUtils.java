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

/**
 * A utility class for dealing with {@link ClassLoader}s. The primary focus of
 * this class is to ease with handling the diversity of situations that the
 * applcation can be deployed to: Embedded in a non-system classloader, a Java
 * Web Start classloader and running in a regular system classloader.
 * 
 * @author Kasper Sørensen
 */
public class ClassLoaderUtils {

	// to find out if web start is running, use system property
	// http://lopica.sourceforge.net/faq.html#under
	public static final boolean IS_WEB_START = System.getProperty("javawebstart.version") != null;

	/**
	 * Gets an appropriate classloader for usage when performing classpath
	 * lookups and scanning.
	 * 
	 * @return
	 */
	public static ClassLoader getParentClassLoader() {
		if (IS_WEB_START) {
			return Thread.currentThread().getContextClassLoader();
		} else {
			return ClassLoaderUtils.class.getClassLoader();
		}
	}
}
