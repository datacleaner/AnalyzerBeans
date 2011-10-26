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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Converter;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.configuration.InjectionPoint;
import org.eobjects.analyzer.lifecycle.MemberInjectionPoint;

/**
 * Main converter used by {@link StringConverter}. This converter will delegate
 * and compose conversions based on relevant converter implementations, such as
 * {@link NullConverter}, {@link ArrayConverter}, {@link StandardTypeConverter}
 * and {@link ConfigurationItemConverter}.
 * 
 * @author Kasper Sørensen
 * 
 */
public class DelegatingConverter implements Converter<Object> {

	private static final String[][] ESCAPE_MAPPING = { { "&amp;", "&" }, { "&#91;", "[" }, { "&#93;", "]" },
			{ "&#44;", "," }, { "&lt;", "<" }, { "&gt;", ">" }, { "&quot;", "\"" }, { "&copy;", "\u00a9" },
			{ "&reg;", "\u00ae" }, { "&euro;", "\u20a0" } };

	private final List<Converter<?>> _converters;
	private final NullConverter _nullConverter;
	private final ArrayConverter _arrayConverter;

	public DelegatingConverter() {
		this(null);
	}

	public DelegatingConverter(Collection<Converter<?>> converters) {
		_converters = new ArrayList<Converter<? extends Object>>();
		_nullConverter = new NullConverter();
		_arrayConverter = new ArrayConverter(this);

		if (converters != null) {
			_converters.addAll(converters);
		}
	}

	public List<Converter<?>> getConverters() {
		return _converters;
	}

	@Override
	public Object fromString(Class<?> type, String serializedForm) {
		if (type == null || serializedForm == null || _nullConverter.isNull(serializedForm)) {
			return _nullConverter.fromString(type, serializedForm);
		}

		if (type.isArray()) {
			return _arrayConverter.fromString(type, serializedForm);
		}

		serializedForm = unescape(serializedForm);

		for (Converter<?> converter : _converters) {
			if (converter.isConvertable(type)) {
				Object result = converter.fromString(type, serializedForm);
				return result;
			}
		}
		throw new IllegalStateException("Could not find matching converter for type: " + type);
	}

	@Override
	public String toString(Object instance) {
		if (null == instance) {
			return _nullConverter.toString(instance);
		}
		
		if (instance.getClass().isArray()) {
			return _arrayConverter.toString(instance);
		}
		
		final Class<? extends Object> type = instance.getClass();
		for (Converter<?> converter : _converters) {
			if (converter.isConvertable(type)) {
				@SuppressWarnings("unchecked")
				Converter<Object> castedConverter = (Converter<Object>) converter;
				String serializedForm = castedConverter.toString(instance);
				
				serializedForm = escape(serializedForm);
				
				return serializedForm;
			}
		}

		throw new IllegalStateException("Could not find matching converter for instance: " + instance);
	}

	@Override
	public boolean isConvertable(Class<?> instance) {
		return true;
	}

	/**
	 * Initializes all converters contained with injections
	 * 
	 * @param injectionManager
	 */
	public void initializeAll(InjectionManager injectionManager) {
		if (injectionManager != null) {
			for (Converter<?> converter : _converters) {
				Field[] fields = ReflectionUtils.getFields(converter.getClass(), Inject.class);
				for (Field field : fields) {
					final Object value;
					if (field.getType() == Converter.class) {
						// Injected converters are used as callbacks. They
						// should be assigned to the outer converter, which is
						// this.
						value = this;
					} else {
						InjectionPoint<Object> injectionPoint = new MemberInjectionPoint<Object>(field, converter);
						value = injectionManager.getInstance(injectionPoint);
					}
					try {
						field.set(converter, value);
					} catch (Exception e) {
						throw new IllegalStateException("Could not initialize converter: " + converter, e);
					}
				}
			}
		}
	}

	private static final String escape(String str) {
		for (String[] mapping : ESCAPE_MAPPING) {
			String escapedValue = mapping[1];
			if (str.contains(escapedValue)) {
				str = str.replace(escapedValue, mapping[0]);
			}
		}
		return str;
	}

	private static final String unescape(String str) {
		for (String[] mapping : ESCAPE_MAPPING) {
			String unescapedValue = mapping[0];
			if (str.contains(unescapedValue)) {
				str = str.replaceAll(unescapedValue, mapping[1]);
			}
		}
		return str;
	}
}
