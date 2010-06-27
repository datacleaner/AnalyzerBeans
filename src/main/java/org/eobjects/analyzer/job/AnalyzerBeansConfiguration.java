package org.eobjects.analyzer.job;

import java.io.Serializable;

import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.lifecycle.CollectionProvider;

/**
 * Represents the configuration of the AnalyzerBeans application. The
 * configuration can provide all the needed providers and catalogs used by
 * AnalyzerBeans to configure and execute jobs.
 * 
 * @author Kasper Sørensen
 */
public interface AnalyzerBeansConfiguration extends Serializable {

	public DescriptorProvider getDescriptorProvider();

	public CollectionProvider getCollectionProvider();

	public DatastoreCatalog getDatastoreCatalog();

	public ConcurrencyProvider getConcurrencyProvider();
}
