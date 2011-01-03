package org.eobjects.analyzer.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.junit.Before;
import org.junit.Test;

import dk.eobjects.metamodel.schema.Column;


public class DataStoreBasedSynonymCatalogTest {
	
	private  DataStoreBasedSynonymCatalog _dataStoreBasedSynonymCatalog;
	
	@Before
	public void createCsvDataStore() {
		CsvDatastore csvDatastore = new CsvDatastore("region", "src/test/resources/datastore-synonym-countries.csv");
		List<Datastore> datastores = new LinkedList<Datastore>();
		datastores.add(csvDatastore);
		Column regionAsColumn = getRegionAsColumn(csvDatastore);
		_dataStoreBasedSynonymCatalog =  new DataStoreBasedSynonymCatalog(csvDatastore.getName(), regionAsColumn, csvDatastore);
	}

	@Test
	public void shouldReturnCorrectMasterTerm(){
		assertNull(_dataStoreBasedSynonymCatalog.getMasterTerm("region"));
		assertEquals("DNK", _dataStoreBasedSynonymCatalog.getMasterTerm("Denmark"));
		assertEquals("GBR", _dataStoreBasedSynonymCatalog.getMasterTerm("Great Britain"));
		assertEquals("DNK", _dataStoreBasedSynonymCatalog.getMasterTerm("DK"));
	}
	
	@Test
	public void shouldReturnAllSynonyms(){
		Collection<Synonym> synonyms = _dataStoreBasedSynonymCatalog.getSynonyms();
		org.junit.Assert.assertEquals(3, synonyms.size());
	}

	@Test
	public void shouldReturnNameOfTheCatalog(){
		org.junit.Assert.assertSame("region", _dataStoreBasedSynonymCatalog.getName());
	}
	
	private Column getRegionAsColumn(CsvDatastore csvDatastore) {
		DataContextProvider dataContextProvider = csvDatastore.getDataContextProvider();
		Column regionAsColumn = dataContextProvider.getSchemaNavigator().convertToColumn("region");
		return regionAsColumn;
	}

}
