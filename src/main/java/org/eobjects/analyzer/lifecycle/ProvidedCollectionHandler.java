package org.eobjects.analyzer.lifecycle;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eobjects.analyzer.descriptors.AnnotationHelper;
import org.eobjects.analyzer.descriptors.ProvidedDescriptor;

import com.sleepycat.bind.ByteArrayBinding;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.BooleanBinding;
import com.sleepycat.bind.tuple.DoubleBinding;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import dk.eobjects.metamodel.util.FileHelper;

public class ProvidedCollectionHandler {

	private Environment environment;

	public Object createProvidedCollection(ProvidedDescriptor providedDescriptor) {
		if (providedDescriptor.isList()) {
			List<?> list = createList(providedDescriptor.getTypeArgument(0));
			return list;
		} else if (providedDescriptor.isMap()) {
			Map<?, ?> map = createMap(providedDescriptor.getTypeArgument(0),
					providedDescriptor.getTypeArgument(1));
			return map;
		} else {
			// This should never happen (is checked by the ProvidedDescriptor)
			throw new IllegalStateException();
		}
	}

	public void cleanUp(Object obj) {
		StoredMap<?, ?> map;
		if (obj instanceof ProvidedList<?>) {
			ProvidedList<?> list = (ProvidedList<?>) obj;
			map = (StoredMap<?, ?>) list.getWrappedMap();
		} else if (obj instanceof StoredMap<?, ?> ) {
			map = (StoredMap<?, ?>) obj;
		} else {
			throw new IllegalStateException("Cannot clean up object: " + obj);
		}
		map.clear();

		// _environment.removeDatabase(null, databaseName);
		// _environment.compress();
		// _environment.cleanLog();
		// _environment.sync();
		// File home = _environment.getHome();
		// _environment.close();
		// File[] databaseFiles = home.listFiles(new FilenameFilter() {
		//
		// public boolean accept(File dir, String name) {
		// return name.endsWith(".jdb");
		// }
		// });
		// for (File file : databaseFiles) {
		// file.deleteOnExit();
		// }

		// TODO: Find out if we can clean it up further (eg. delete it
		// physically from disk?)
	}

	private Environment getEnvironment() throws DatabaseException {
		if (environment == null) {
			EnvironmentConfig config = new EnvironmentConfig();
			config.setAllowCreate(true);
			File tempDir = FileHelper.getTempDir();
			environment = new Environment(tempDir, config);
		}
		return environment;
	}

	private Database createDatabase() throws DatabaseException {
		DatabaseConfig databaseConfig = new DatabaseConfig();
		databaseConfig.setAllowCreate(true);
		String databaseName = UUID.randomUUID().toString();
		Database database = getEnvironment().openDatabase(null, databaseName,
				databaseConfig);
		return database;
	}

	public <E> List<E> createList(Class<E> valueType)
			throws IllegalStateException {
		Map<Integer, E> map = createMap(Integer.class, valueType);

		// Berkeley StoredLists are non-functional!
		// return new StoredList<E>(createDatabase(), valueBinding, true);

		return new ProvidedList<E>(map);
	}

	public <K, V> Map<K, V> createMap(Class<K> keyType, Class<V> valueType)
			throws IllegalStateException {
		try {
			EntryBinding<K> keyBinding = createBinding(keyType);
			EntryBinding<V> valueBinding = createBinding(valueType);
			return new StoredMap<K, V>(createDatabase(), keyBinding,
					valueBinding, true);
		} catch (DatabaseException e) {
			throw new IllegalStateException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <E> EntryBinding<E> createBinding(Class<E> type)
			throws UnsupportedOperationException {
		if (AnnotationHelper.isBoolean(type)) {
			return (EntryBinding<E>) new BooleanBinding();
		}
		if (AnnotationHelper.isInteger(type)) {
			return (EntryBinding<E>) new IntegerBinding();
		}
		if (AnnotationHelper.isLong(type)) {
			return (EntryBinding<E>) new LongBinding();
		}
		if (AnnotationHelper.isDouble(type)) {
			return (EntryBinding<E>) new DoubleBinding();
		}
		if (AnnotationHelper.isString(type)) {
			return (EntryBinding<E>) new StringBinding();
		}
		if (AnnotationHelper.isByteArray(type)) {
			return (EntryBinding<E>) new ByteArrayBinding();
		}
		throw new UnsupportedOperationException(
				"Cannot provide collection of type " + type);
	}
}
