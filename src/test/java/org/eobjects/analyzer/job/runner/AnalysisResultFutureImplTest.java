package org.eobjects.analyzer.job.runner;

import java.util.LinkedList;
import java.util.Queue;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.analyzer.job.concurrent.JobCompletionListener;
import org.eobjects.analyzer.result.AnalyzerResult;

public class AnalysisResultFutureImplTest extends TestCase {

	public void testIsSuccessful() throws Exception {
		Queue<AnalyzerResult> resultQueue = new LinkedList<AnalyzerResult>();
		JobCompletionListener jobCompletionListener = EasyMock.createMock(JobCompletionListener.class);

		EasyMock.expect(jobCompletionListener.isDone()).andReturn(true).times(2);

		EasyMock.replay(jobCompletionListener);

		AnalysisResultFutureImpl resultFuture = new AnalysisResultFutureImpl(resultQueue, jobCompletionListener);
		assertTrue(resultFuture.isSuccessful());

		resultFuture.addError(new Exception());
		assertFalse(resultFuture.isSuccessful());

		EasyMock.verify(jobCompletionListener);
	}
}
