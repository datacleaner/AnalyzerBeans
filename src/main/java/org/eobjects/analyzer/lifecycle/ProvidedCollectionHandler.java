package org.eobjects.analyzer.lifecycle;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private Log log = LogFactory.getLog(getClass());
	private Environment environment;
	private Boolean deleteOnExit;
	private File targetDir;

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
		} else if (obj instanceof StoredMap<?, ?>) {
			map = (StoredMap<?, ?>) obj;
		} else {
			throw new IllegalStateException("Cannot clean up object: " + obj);
		}
		map.clear();

		try {
			getEnvironment().compress();
			getEnvironment().cleanLog();
			getEnvironment().sync();
		} catch (DatabaseException e) {
			log.error("Exception occurred while cleaning up object: " + obj, e);
		}

		if (deleteOnExit) {
			initDeleteOnExit(targetDir);
		}
	}

	private void initDeleteOnExit(File dir) {
		dir.deleteOnExit();
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				initDeleteOnExit(file);
			} else if (file.isFile()) {
				file.deleteOnExit();
			} else {
				log
						.warn("Unable to set the deleteOnExit flag on file: "
								+ file);
			}
		}
	}

	private Environment getEnvironment() throws DatabaseException {
		if (environment == null) {
			EnvironmentConfig config = new EnvironmentConfig();
			config.setAllowCreate(true);
			File targetDir = createTargetDir();
			environment = new Environment(targetDir, config);
		}
		return environment;
	}

	private File createTargetDir() {
		File tempDir = FileHelper.getTempDir();
		deleteOnExit = false;
		while (targetDir == null) {
			try {
				File candidateDir = new File(tempDir.getAbsolutePath()
						+ File.separatorChar + "analyzerBeans_"
						+ UUID.randomUUID().toString());
				if (!candidateDir.exists() && candidateDir.mkdir()) {
					targetDir = candidateDir;
					deleteOnExit = true;
				}
			} catch (Exception e) {
				log
						.error(
								"Exception thrown while trying to create targetDir inside tempDir",
								e);
				targetDir = tempDir;
			}
		}
		if (log.isInfoEnabled()) {
			log
					.info("Using target directory for persistent collections (deleteOnExit="
							+ deleteOnExit
							+ "): "
							+ targetDir.getAbsolutePath());
		}
		return targetDir;
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
