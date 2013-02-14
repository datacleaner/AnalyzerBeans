package org.eobjects.analyzer.cluster;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.ListResult;

@AnalyzerBean("Analyzer with bad reducer")
@Distributed(reducer = MockResultReducerThatWillFail.class)
public class MockAnalyzerWithBadReducer implements Analyzer<ListResult<String>> {

    @Configured
    InputColumn<?>[] input;

    @Override
    public ListResult<String> getResult() {
        List<String> list = new ArrayList<String>();
        list.add("foobar");
        return new ListResult<String>(list);
    }

    @Override
    public void run(InputRow row, int arg1) {
        // do nothing
    }

}
