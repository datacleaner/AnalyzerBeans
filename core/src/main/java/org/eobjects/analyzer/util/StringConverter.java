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
package org.eobjects.analyzer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.beans.api.Convertable;
import org.eobjects.analyzer.beans.api.Converter;
import org.eobjects.analyzer.configuration.InjectionManager;
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

	public final String serialize(Object o) {
		List<Converter<?>> converters = new ArrayList<Converter<?>>();

		if (o != null) {
			Convertable convertable = o.getClass().getAnnotation(Convertable.class);
			if (convertable != null) {
				Class<? extends Converter<?>> converterClass = convertable.value();
				try {
					@SuppressWarnings("unchecked")
					Converter<Object> converter = (Converter<Object>) converterClass.newInstance();
					converters.add(converter);
				} catch (Exception e) {
					if (e instanceof RuntimeException) {
						throw (RuntimeException) e;
					}
					throw new IllegalStateException("Error occurred while using instantiating: " + converterClass, e);
				}
			}
		}

		converters.add(new ConfigurationItemConverter());
		converters.add(new StandardTypeConverter());

		DelegatingConverter converter = new DelegatingConverter(converters);
		converter.initializeAll(_injectionManager);

		return converter.toString(o);
	}

	/**
	 * Deserializes a string to a Java object.
	 * 
	 * @param <E>
	 * @param str
	 * @param type
	 * @param schemaNavigator
	 *            schema navigator to use when type is Column, Table or Schema.
	 * @return
	 */
	public final <E> E deserialize(String str, Class<E> type) {
		logger.debug("deserialize(\"{}\", {})", str, type);

		Collection<Converter<?>> converters = new ArrayList<Converter<?>>();

		Convertable convertable = type.getAnnotation(Convertable.class);
		if (convertable != null) {
			Class<? extends Converter<?>> converterClass = convertable.value();
			try {
				Converter<?> converter = converterClass.newInstance();
				converters.add(converter);
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new IllegalStateException("Error occurred while instantiating converter: " + converterClass, e);
			}
		}

		converters.add(new ConfigurationItemConverter());
		converters.add(new StandardTypeConverter());

		DelegatingConverter converter = new DelegatingConverter(converters);
		converter.initializeAll(_injectionManager);

		@SuppressWarnings("unchecked")
		E result = (E) converter.fromString(type, str);
		return result;
	}
}