package org.eobjects.analyzer.engine;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sleepycat.bind.ByteArrayBinding;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.BooleanBinding;
import com.sleepycat.bind.tuple.DoubleBinding;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.collections.StoredList;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import dk.eobjects.metamodel.util.FileHelper;

public class ProvidedCollectionProvider {

	private Environment environment;

	public void provide(Object analyzerBean,
			AnalyzerBeanDescriptor analyzerBeanDescriptor) {
		List<ProvidedDescriptor> providedDescriptors = analyzerBeanDescriptor.getProvidedDescriptors();
		for (ProvidedDescriptor providedDescriptor : providedDescriptors) {
			if (providedDescriptor.isList()) {
				List<?> list = createList(providedDescriptor.getTypeArgument(0));
				providedDescriptor.assignValue(analyzerBean, list);
				
				//TODO: Add clean up mechanism
			} else if (providedDescriptor.isMap()) {
				Map<?, ?> map = createMap(providedDescriptor.getTypeArgument(0),
						providedDescriptor.getTypeArgument(1));
				providedDescriptor.assignValue(analyzerBean, map);
				
				//TODO: Add clean up mechanism
			} else {
				throw new IllegalStateException();
			}
		}
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

	private List<?> createList(Type valueType) {
		try {
			EntryBinding valueBinding = createBinding(valueType);
			return new StoredList(createDatabase(), valueBinding, true);
		} catch (DatabaseException e) {
			throw new IllegalStateException(e);
		}
	}

	private Map<?, ?> createMap(Type keyType, Type valueType) {
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
