package org.eobjects.analyzer.lifecycle;

import java.io.File;
import java.lang.reflect.Type;
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
import com.sleepycat.collections.StoredContainer;
import com.sleepycat.collections.StoredList;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import dk.eobjects.metamodel.util.FileHelper;

public class ProvidedCollectionHandler {

	private Environment environment;

	public StoredContainer createProvidedCollection(
			ProvidedDescriptor providedDescriptor) {
		if (providedDescriptor.isList()) {
			StoredList list = createList(providedDescriptor.getTypeArgument(0));
			return list;
		} else if (providedDescriptor.isMap()) {
			StoredMap map = createMap(providedDescriptor.getTypeArgument(0),
					providedDescriptor.getTypeArgument(1));
			return map;
		} else {
			// This should never happen (is checked by the ProvidedDescriptor)
			throw new IllegalStateException();
		}
	}

	public void cleanUp(StoredContainer container) {
		container.clear();
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

	private StoredList createList(Type valueType) {
		try {
			EntryBinding valueBinding = createBinding(valueType);
			return new StoredList(createDatabase(), valueBinding, true);
		} catch (DatabaseException e) {
			throw new IllegalStateException(e);
		}
	}

	private StoredMap createMap(Type keyType, Type valueType) {
		try {
			EntryBinding keyBinding = createBinding(keyType);
			EntryBinding valueBinding = createBinding(valueType);
			return new StoredMap(createDatabase(), keyBinding, valueBinding,
					true);
		} catch (DatabaseException e) {
			throw new IllegalStateException(e);
		}
	}

	private EntryBinding createBinding(Type type) {
		if (AnnotationHelper.isBoolean(type)) {
			return new BooleanBinding();
		}
		if (AnnotationHelper.isInteger(type)) {
			return new IntegerBinding();
		}
		if (AnnotationHelper.isLong(type)) {
			return new LongBinding();
		}
		if (AnnotationHelper.isDouble(type)) {
			return new DoubleBinding();
		}
		if (AnnotationHelper.isString(type)) {
			return new StringBinding();
		}
		if (AnnotationHelper.isByteArray(type)) {
			return new ByteArrayBinding();
		}
		throw new UnsupportedOperationException(
				"Cannot provide collection of type " + type);
	}
}
