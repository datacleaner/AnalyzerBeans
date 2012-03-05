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
package org.eobjects.analyzer.beans;

import java.util.Map;

import javax.inject.Inject;

import org.codehaus.jackson.map.ObjectMapper;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.DataStructuresCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.StringUtils;

@TransformerBean("Read & parse JSON document")
@Description("Parses a JSON document (as a string) and materializes the data structure it represents")
@Categorized(DataStructuresCategory.class)
public class ParseJsonTransformer implements Transformer<Object> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    @Configured(order = 1)
    @Description("Column containing JSON documents to parse")
    InputColumn<String> json;

    @Inject
    @Configured(order = 2)
    Class<?> dataType = Map.class;

    public ParseJsonTransformer() {

    }

    public ParseJsonTransformer(InputColumn<String> json) {
        this.json = json;
    }

    @Override
    public OutputColumns getOutputColumns() {
        String[] names = new String[] { json.getName() + " (as Map)" };
        Class<?>[] types = new Class[] { dataType };
        return new OutputColumns(names, types);
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        final String jsonString = inputRow.getValue(json);
        final Object result = parse(jsonString, dataType, mapper);

        return new Object[] { result };
    }

    public static <E> E parse(final String jsonString, final Class<E> dataType,
            final ObjectMapper objectMapper) {
        if (StringUtils.isNullOrEmpty(jsonString)) {
            return null;
        } else {
            try {
                return objectMapper.readValue(jsonString, dataType);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Exception occurred while parsing JSON", e);
            }
        }
    }
    
    public void setDataType(Class<?> dataType) {
        this.dataType = dataType;
    }
    
    public void setJson(InputColumn<String> json) {
        this.json = json;
    }
}
