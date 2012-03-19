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
package org.eobjects.analyzer.configuration;

import org.eobjects.metamodel.util.FileHelper;

/**
 * Defines a default implementation of the
 * {@link ConfigurationReaderInterceptor} interface. This implementation does
 * not intercept or perform any special treatment when invoked.
 * 
 * @author Kasper Sørensen
 */
public class DefaultConfigurationReaderInterceptor implements ConfigurationReaderInterceptor {

	@Override
	public String createFilename(String filename) {
		return filename;
	}

	@Override
	public String getTemporaryStorageDirectory() {
		return FileHelper.getTempDir().getAbsolutePath();
	}

	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
	    return Class.forName(className);
	}
	
	@Override
	public String getPropertyOverride(String variablePath) {
	    String result = System.getProperty(variablePath);
        return result;
	}
}
