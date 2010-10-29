package org.eobjects.analyzer.reference;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.util.FileHelper;

public class TextBasedDictionary implements Dictionary {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(TextBasedDictionary.class);

	private final transient WeakHashMap<String, Boolean> _containsValueCache = new WeakHashMap<String, Boolean>();
	private final String _name;
	private final String _filename;
	private final String _encoding;

	public TextBasedDictionary(String name, String filename, String encoding) {
		_name = name;
		_filename = filename;
		_encoding = encoding;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public boolean containsValue(String value) {
		if (value == null) {
			return false;
		}
		Boolean result = _containsValueCache.get(value);
		if (result == null) {
			BufferedReader reader = null;
			try {
				result = false;
				reader = FileHelper.getBufferedReader(new File(_filename), _encoding);
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					if (value.equals(line)) {
						result = true;
						break;
					}
				}
				_containsValueCache.put(value, result);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception e) {
						logger.error("Exception occurred when closing reader", e);
					}
				}
			}
		}
		return result;
	}

	@Override
	public ReferenceValues<String> getValues() {
		BufferedReader reader = null;
		try {
			List<String> values = new ArrayList<String>();
			reader = FileHelper.getBufferedReader(new File(_filename), _encoding);
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				values.add(line);
			}
			return new SimpleStringReferenceValues(values, true);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					logger.error("Exception occurred when closing reader", e);
				}
			}
		}
	}

}
