package org.eobjects.analyzer.util.batch;

import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;

@TransformerBean("Mock batcher")
public class MockBatchTransformer extends BatchTransformer {

    @Configured
    InputColumn<String> input;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns("BAZ");
    }

    @Override
    public void map(BatchSource<InputRow> source, BatchSink<Object[]> sink) {
        List<InputRow> list = source.toList();
        List<String> values = CollectionUtils.map(list, new Func<InputRow, String>() {
            @Override
            public String eval(InputRow row) {
                return row.getValue(input);
            }
        });

        // sort the values
        Collections.sort(values);

        for (int i = 0; i < source.size(); i++) {
            String value = values.get(i);
            sink.setOutput(i, new Object[] { value });
        }
    }

}
