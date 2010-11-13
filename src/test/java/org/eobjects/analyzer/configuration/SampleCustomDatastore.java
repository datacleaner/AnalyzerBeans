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

import java.io.File;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.SingleDataContextProvider;
import org.junit.Ignore;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;

@Ignore
public class SampleCustomDatastore implements Datastore {

	private static final long serialVersionUID = 1L;

	@Configured
	String name;

	@Configured
	File xmlFile;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DataContextProvider getDataContextProvider() {
		DataContext dc = DataContextFactory.createXmlDataContext(xmlFile, false, false);
		return new SingleDataContextProvider(dc, this);
	}

	@Override
	public void close() {
	}

}
