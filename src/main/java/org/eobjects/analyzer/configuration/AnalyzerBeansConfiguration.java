package org.eobjects.analyzer.configuration;

import java.io.Serializable;

import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;

/**
 * Represents the configuration of the AnalyzerBeans application. The
 * configuration can provide all the needed providers and catalogs used by
 * AnalyzerBeans to configure and execute jobs.
 * 
 * @author Kasper Sørensen
 */
public interface AnalyzerBeansConfiguration extends Serializable {

	public DatastoreCatalog getDatastoreCatalog();

	public ReferenceDataCatalog getReferenceDataCatalog();

	public DescriptorProvider getDescriptorProvider();

	public CollectionProvider getCollectionProvider();

	public TaskRunner getTaskRunner();
}
