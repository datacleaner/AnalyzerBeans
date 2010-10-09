package org.eobjects.analyzer.job.runner;

import java.util.LinkedList;
import java.util.Queue;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.analyzer.job.concurrent.JobTaskListener;
import org.eobjects.analyzer.result.AnalyzerResult;

public class AnalysisResultFutureImplTest extends TestCase {

	public void testIsSuccessful() throws Exception {
		Queue<AnalyzerResult> resultQueue = new LinkedList<AnalyzerResult>();
		JobTaskListener jobCompletionListener = EasyMock.createMock(JobTaskListener.class);
		ErrorAware errorAware = EasyMock.createMock(ErrorAware.class);

		EasyMock.expect(jobCompletionListener.isDone()).andReturn(true).times(2);
		EasyMock.expect(errorAware.isErrornous()).andReturn(false);
		EasyMock.expect(errorAware.isErrornous()).andReturn(true);

		EasyMock.replay(jobCompletionListener, errorAware);

		AnalysisResultFutureImpl resultFuture = new AnalysisResultFutureImpl(resultQueue, jobCompletionListener, errorAware);
		assertTrue(resultFuture.isSuccessful());

		assertFalse(resultFuture.isSuccessful());

		EasyMock.verify(jobCompletionListener, errorAware);
	}
}
