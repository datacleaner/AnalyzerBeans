package org.eobjects.analyzer.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.junit.Test;

public class ExtractJsonValuesTransformerTest {

    @Test
    public void testExtractJsonValuesTransformerWithoutAnyValidation() {
        InputColumn<String> col1 = new MockInputColumn<String>("jsonDocument",
                String.class);
        ExtractJsonValuesTransformer transformer = new ExtractJsonValuesTransformer(
                col1);
        assertEquals(1, transformer.getOutputColumns().getColumnCount());

        String json = "{\"name\":\"shekhar\",\"country\":\"india\"}";

        Map<String, ?>[] values = transformer.transform(new MockInputRow().put(
                col1, json));
        assertEquals(1, values.length);
        assertEquals(2, values[0].size());
        assertEquals("{name=shekhar, country=india}", values[0].toString());
    }

    @Test
    public void testExtractJsonNumbersAndBooleans() {
        InputColumn<String> col1 = new MockInputColumn<String>("jsonDocument",
                String.class);
        ExtractJsonValuesTransformer transformer = new ExtractJsonValuesTransformer(
                col1);
        assertEquals(1, transformer.getOutputColumns().getColumnCount());

        String json = "{\"name\":\"kasper\",\"age\":29,\"developer\":true,\"manager\":false,\"balance\":400.17}";

        Map<String, ?>[] values = transformer.transform(new MockInputRow().put(
                col1, json));

        assertEquals(1, values.length);
        assertEquals(5, values[0].size());
        assertEquals("{name=kasper, age=29, developer=true, manager=false, balance=400.17}",
                values[0].toString());
    }

    @Test
    public void shouldReturnEmptyMapWhenNoJsonDocumentExistForColumn()
            throws Exception {
        InputColumn<String> col1 = new MockInputColumn<String>("jsonDocument",
                String.class);
        ExtractJsonValuesTransformer transformer = new ExtractJsonValuesTransformer(
                col1);
        assertEquals(1, transformer.getOutputColumns().getColumnCount());
        Map<String, ?>[] values = transformer.transform(new MockInputRow());
        assertTrue(values.length == 1);
        assertEquals(true, values[0].isEmpty());
    }

    @Test
    public void shouldExtractNestedDocumentsAsCollections() throws Exception {
        InputColumn<String> col1 = new MockInputColumn<String>("jsonDocument",
                String.class);
        ExtractJsonValuesTransformer transformer = new ExtractJsonValuesTransformer(
                col1);
        assertEquals(1, transformer.getOutputColumns().getColumnCount());

        String json = "{\"name\":\"shekhar\",\"addresses\":[{\"city\":\"Delhi\",\"country:\":\"India\"},{\"city\":\"Delhi\",\"country:\":\"India\"}],\"emails\":[\"email1\",\"email2\"]}";

        Map<String, ?>[] values = transformer.transform(new MockInputRow().put(
                col1, json));
        assertEquals(1, values.length);
        Map<String, ?> map = values[0];
        assertEquals("{name=shekhar, addresses=[{city=Delhi, country:=India}, {city=Delhi, country:=India}], emails=[email1, email2]}",
                map.toString());

        assertTrue(map.get("addresses") instanceof List);
        List addresses = (List)map.get("addresses");
        assertTrue(addresses.get(0) instanceof Map);
        assertTrue(map.get("emails") instanceof List);
    }
}
