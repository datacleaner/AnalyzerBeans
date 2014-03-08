package org.eobjects.analyzer.beans.composition;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.FileProperty;
import org.eobjects.analyzer.beans.api.FileProperty.FileAccessMode;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.transform.AbstractWrappedAnalysisJobTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.metamodel.util.Func;
import org.eobjects.metamodel.util.Resource;

@TransformerBean("Invoke child Analysis job")
@Description("Wraps another (external) Analysis job's transformations and invokes them as an integrated part of the current job. Using this transformation you can compose parent and child jobs for more coarse or more fine granularity of transformations.")
public class InvokeChildAnalysisJobTransformer extends AbstractWrappedAnalysisJobTransformer {

    @Configured
    InputColumn<?>[] input;

    @Configured("Analysis job")
    @FileProperty(accessMode = FileAccessMode.OPEN, extension = ".analysis.xml")
    Resource analysisJobResource;

    @Override
    protected AnalysisJob createWrappedAnalysisJob() {
        AnalysisJob job = analysisJobResource.read(new Func<InputStream, AnalysisJob>() {
            @Override
            public AnalysisJob eval(InputStream in) {
                JaxbJobReader reader = new JaxbJobReader(getAnalyzerBeansConfiguration());
                AnalysisJob job = reader.read(in);
                return job;
            }
        });
        return job;
    }

    @Override
    protected Map<InputColumn<?>, InputColumn<?>> getInputColumnConversion(AnalysisJob wrappedAnalysisJob) {
        Collection<InputColumn<?>> sourceColumns = wrappedAnalysisJob.getSourceColumns();
        if (input.length != sourceColumns.size()) {
            throw new IllegalStateException("Wrapped job defines " + sourceColumns.size()
                    + " columns, but transformer input defines " + input.length);
        }

        Map<InputColumn<?>, InputColumn<?>> result = new LinkedHashMap<InputColumn<?>, InputColumn<?>>();
        int i = 0;
        Iterator<InputColumn<?>> it = sourceColumns.iterator();
        while (it.hasNext()) {
            InputColumn<?> parentColumn = input[i];
            InputColumn<?> childColumn = it.next();
            result.put(parentColumn, childColumn);
            i++;
        }

        return result;
    }

}
