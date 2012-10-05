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
package org.eobjects.analyzer.beans.datastructures;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.DataStructuresCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Transformer for selecting values from maps.
 * 
 * @author Kasper Sørensen
 * @author Shekhar Gulati
 * @author Saurabh Gupta
 */
@TransformerBean("Select values from key/value map")
@Description("Given a specified list of keys, this transformer will select the values from a key/value map and place them as columns within the record")
@Categorized(DataStructuresCategory.class)
public class SelectFromMapTransformer implements Transformer<Object> {

    @Inject
    @Configured
    InputColumn<Map<String, ?>> mapColumn;

    @Inject
    @Configured
    String[] keys;

    @Inject
    @Configured
    Class<?>[] types;

    @Inject
    @Configured
    @Description("Verify that expected type and actual type are the same")
    boolean verifyTypes = false;

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public void setTypes(Class<?>[] types) {
        this.types = types;
    }

    public void setMapColumn(InputColumn<Map<String, ?>> mapColumn) {
        this.mapColumn = mapColumn;
    }

    public void setVerifyTypes(boolean verifyTypes) {
        this.verifyTypes = verifyTypes;
    }

    @Override
    public OutputColumns getOutputColumns() {
        String[] keys = this.keys;
        Class<?>[] types = this.types;
        if (keys.length != types.length) {
            // odd case sometimes encountered with invalid configurations or
            // while building a job
            final int length = Math.min(keys.length, types.length);
            keys = Arrays.copyOf(keys, length);
            types = Arrays.copyOf(types, length);
        }
        return new OutputColumns(keys, types);
    }

    @Override
    public Object[] transform(InputRow row) {
        final Map<String, ?> map = row.getValue(mapColumn);
        final Object[] result = new Object[keys.length];

        if (map == null) {
            return result;
        }

        for (int i = 0; i < keys.length; i++) {
            Object value = find(map, keys[i]);
            if (verifyTypes) {
                value = types[i].cast(value);
            }
            result[i] = value;
        }

        return result;
    }

    private Object find(Map<String, ?> map, String key) {
        final Object result = map.get(key);
        if (result == null) {
            final int indexOfDot = key.indexOf('.');
            if (indexOfDot != -1) {
                final String prefix = key.substring(0, indexOfDot);
                final Object nestedObject = map.get(prefix);
                if (nestedObject instanceof Map) {
                    final String remainingPart = key.substring(indexOfDot + 1);
                    @SuppressWarnings("unchecked")
                    final Map<String, ?> nestedMap = (Map<String, ?>) nestedObject;
                    return find(nestedMap, remainingPart);
                }
            }
        }
        return result;
    }

}
