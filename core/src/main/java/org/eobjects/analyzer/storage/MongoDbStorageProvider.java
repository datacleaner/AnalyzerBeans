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
package org.eobjects.analyzer.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

/**
 * Experimental storage provider that uses MongoDB as a backing store.
 * 
 * Currently only supports row annotation factory creation, the remaining
 * storage entities are in-memory based.
 * 
 * @author Kasper Sørensen
 */
public class MongoDbStorageProvider implements StorageProvider {

	private final AtomicInteger id = new AtomicInteger(1);

	@Override
	public <E> List<E> createList(Class<E> valueType) throws IllegalStateException {
		return new ArrayList<E>();
	}

	@Override
	public <E> Set<E> createSet(Class<E> valueType) throws IllegalStateException {
		return new HashSet<E>();
	}

	@Override
	public <K, V> Map<K, V> createMap(Class<K> keyType, Class<V> valueType) throws IllegalStateException {
		return new HashMap<K, V>();
	}

	@Override
	public RowAnnotationFactory createRowAnnotationFactory() {
		try {
			Mongo mongo = new Mongo();
			DB db = mongo.getDB("analyzerbeans");
			String name = "rowannotationfactory" + id.getAndIncrement();
			DBCollection collection = db.createCollection(name, null);

			// drop the collection in case it already exists
			collection.drop();
			collection = db.createCollection(name, null);

			return new MongoDbRowAnnotationFactory(collection);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
