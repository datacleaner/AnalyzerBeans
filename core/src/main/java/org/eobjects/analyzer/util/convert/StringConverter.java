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
package org.eobjects.analyzer.util.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.beans.api.Convertable;
import org.eobjects.analyzer.beans.api.Converter;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for converting objects to and from string representations as
 * used for example in serialized XML jobs.
 * 
 * The string converter currently supports instances and arrays of:
 * <ul>
 * <li>Boolean</li>
 * <li>Byte</li>
 * <li>Short</li>
 * <li>Integer</li>
 * <li>Long</li>
 * <li>Float</li>
 * <li>Double</li>
 * <li>Character</li>
 * <li>String</li>
 * <li>java.io.File</li>
 * <li>java.util.Date</li>
 * <li>java.sql.Date</li>
 * <li>java.util.Calendar</li>
 * <li>java.util.regex.Pattern</li>
 * <li>org.eobjects.analyzer.reference.Dictionary</li>
 * <li>org.eobjects.analyzer.reference.SynonymCatalog</li>
 * <li>org.eobjects.analyzer.reference.StringPattern</li>
 * <li>org.eobjects.analyzer.connection.Datastore</li>
 * <li>org.eobjects.metamodel.schema.Column</li>
 * <li>org.eobjects.metamodel.schema.Table</li>
 * <li>org.eobjects.metamodel.schema.Schema</li>
 * </ul>
 * 
 * @author Kasper Sørensen
 * @author Nancy Sharma
 */
public final class StringConverter {

	private static final Logger logger = LoggerFactory.getLogger(StringConverter.class);

	private final InjectionManager _injectionManager;

	public StringConverter(InjectionManager injectionManager) {
		_injectionManager = injectionManager;
	}

	/**
	 * Serializes a Java object to a String representation.
	 * 
	 * @param o
	 *            the object to serialize
	 * @return a String representation of the Java object
	 */
	public final String serialize(final Object o) {
		return serialize(o, new ArrayList<Class<? extends Converter<?>>>(0));
	}

	public final String serialize(final Object o, final Class<? extends Converter<?>> converterClass) {
		final Collection<Class<? extends Converter<?>>> col = new ArrayList<Class<? extends Converter<?>>>();
		if (converterClass != null) {
			col.add(converterClass);
		}
		return serialize(o, col);
	}

	/**
	 * Serializes a Java object to a String representation.
	 * 
	 * @param o
	 *            the object to serialize
	 * @param converterClasses
	 *            an optional collection of custom converter classes
	 * @return a String representation of the Java object
	 */
	public final String serialize(final Object o, final Collection<Class<? extends Converter<?>>> converterClasses) {
		final List<Converter<?>> converterList = new ArrayList<Converter<?>>();

		if (converterClasses != null) {
			for (Class<? extends Converter<?>> converterClass : converterClasses) {
				converterList.add(createConverter(converterClass));
			}
		}

		if (o != null) {
			Convertable convertable = ReflectionUtils.getAnnotation(o.getClass(), Convertable.class);
			if (convertable != null) {
				Class<? extends Converter<?>> converterClass = convertable.value();
				converterList.add(createConverter(converterClass));
			}
		}

		converterList.add(new ConfigurationItemConverter());
		converterList.add(new StandardTypeConverter());

		DelegatingConverter converter = new DelegatingConverter(converterList);
		converter.initializeAll(_injectionManager);

		return converter.toString(o);
	}

	private Converter<?> createConverter(Class<? extends Converter<?>> converterClass) {
		try {
			Converter<?> converter = converterClass.newInstance();
			return converter;
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new IllegalStateException("Error occurred while using instantiating: " + converterClass, e);
		}
	}

	/**
	 * Deserializes a String into a Java object of the particular type.
	 * 
	 * @param str
	 *            the serialized string representation
	 * @param type
	 *            the requested type
	 * @return a Java object matching the String representation
	 */
	public final <E> E deserialize(String str, Class<E> type) {
		return deserialize(str, type, new ArrayList<Class<? extends Converter<?>>>(0));
	}

	public final <E> E deserialize(String str, Class<E> type, Class<? extends Converter<?>> converterClass) {
		Collection<Class<? extends Converter<?>>> col = new ArrayList<Class<? extends Converter<?>>>();
		if (converterClass != null) {
			col.add(converterClass);
		}
		return deserialize(str, type, col);
	}

	/**
	 * Deserializes a String into a Java object of the particular type.
	 * 
	 * @param str
	 *            the serialized string representation
	 * @param type
	 *            the requested type
	 * @param converterClasses
	 *            an optional collection of custom converters to apply when
	 *            deserializing
	 * @return a Java object matching the String representation
	 */
	public final <E> E deserialize(String str, Class<E> type, Collection<Class<? extends Converter<?>>> converterClasses) {
		logger.debug("deserialize(\"{}\", {})", str, type);

		Collection<Converter<?>> converterList = new ArrayList<Converter<?>>();

		if (converterClasses != null) {
			for (Class<? extends Converter<?>> converterClass : converterClasses) {
				converterList.add(createConverter(converterClass));
			}
		}

		Convertable convertable = ReflectionUtils.getAnnotation(type, Convertable.class);
		if (convertable != null) {
			Class<? extends Converter<?>> converterClass = convertable.value();
			converterList.add(createConverter(converterClass));
		}

		converterList.add(new ConfigurationItemConverter());
		converterList.add(new StandardTypeConverter());

		DelegatingConverter converter = new DelegatingConverter(converterList);
		converter.initializeAll(_injectionManager);

		@SuppressWarnings("unchecked")
		E result = (E) converter.fromString(type, str);
		return result;
	}
}