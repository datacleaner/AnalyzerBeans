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
package org.eobjects.analyzer.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public final class DataStoreBasedSynonymCatalog implements SynonymCatalog {

    private static final long serialVersionUID = 1L;
    
    private final transient WeakHashMap<String, String> _masterTermCache = new WeakHashMap<String, String>();
    private final String _nameOfSynonymCatalog;
    private final boolean _caseSensitive;
    private final Column _masterTerm;
    private Datastore _dataStore;

    public DataStoreBasedSynonymCatalog(String name,Column masterTerm, boolean caseSensitive, Datastore dataStore) {
        _nameOfSynonymCatalog = name;
        _caseSensitive = caseSensitive;
        _masterTerm = masterTerm;
        _dataStore = dataStore;
    }

    @Override
    public String getName() {
        return _nameOfSynonymCatalog;
    }

    public boolean isCaseSensitive() {
        return _caseSensitive;
    }
    
    @Override
    public Collection<Synonym> getSynonyms() {
        
        DataContextProvider dataContextProvider = _dataStore.getDataContextProvider();
        DataContext dataContext = dataContextProvider.getDataContext();
        Table table = _masterTerm.getTable();
        Column[] columns = table.getColumns();
        
        Query query = dataContext.query().from(table.getName()).select(columns).toQuery();
        DataSet results = dataContext.executeQuery(query);
                
        List<Synonym> synonyms = new ArrayList<Synonym>();
        
        while(results.next()){
            synonyms.add(new DataStoreBasedSynonym(results.getRow(), _caseSensitive, _masterTerm));
        }
        dataContextProvider.close();
        return synonyms;
   }


    @Override
    public String getMasterTerm(String term) {
        if (term == null) {
            return null;
        }
        String masterTerm = _masterTermCache.get(term);
        if (masterTerm != null) {
            return masterTerm;
        }
        
        DataContextProvider dataContextProvider = _dataStore.getDataContextProvider();
        DataContext dataContext = dataContextProvider.getDataContext();
        Table table = _masterTerm.getTable();
        Column[] columns = table.getColumns();
        
        Query query = dataContext.query().from(table.getName()).select(columns).toQuery();
        DataSet results = dataContext.executeQuery(query);

            while (results.next()) {
            	Row row = results.getRow();
                DataStoreBasedSynonym synonym = new DataStoreBasedSynonym(row, _caseSensitive, _masterTerm);
                masterTerm = synonym.getMasterTerm();
                if (term.equals(masterTerm) || synonym.getSynonyms().containsValue(term)) {
                    _masterTermCache.put(term, masterTerm);
                    return masterTerm;
                }
            }
            return null;
    }

}
