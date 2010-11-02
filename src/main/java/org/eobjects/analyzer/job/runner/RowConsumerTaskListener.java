package org.eobjects.analyzer.job.runner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.tasks.Task;

public final class RowConsumerTaskListener implements TaskListener {

	private final BlockingQueue<Integer> countingQueue = new LinkedBlockingQueue<Integer>();
	private final AtomicBoolean errorsReported = new AtomicBoolean(false);
	private final AnalysisListener _analysisListener;
	private final AnalysisJob _analysisJob;

	public RowConsumerTaskListener(AnalysisJob analysisJob, AnalysisListener analysisListener) {
		_analysisListener = analysisListener;
		_analysisJob = analysisJob;
	}

	@Override
	public void onBegin(Task task) {
	}

	@Override
	public void onComplete(Task task) {
		boolean submitted = false;
		while (!submitted) {
			try {
				// add a zero to indicate succesful completions
				countingQueue.put(Integer.valueOf(0));
				submitted = true;
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void onError(Task task, Throwable throwable) {
		boolean alreadyRegisteredError = errorsReported.getAndSet(true);
		if (!alreadyRegisteredError) {
			_analysisListener.errorUknown(_analysisJob, throwable);
		}

		boolean submitted = false;
		while (!submitted) {
			try {
				// add a 1 to indicate errornous completions
				countingQueue.put(Integer.valueOf(0));
				submitted = true;
			} catch (InterruptedException e) {
			}
		}
	}

	public boolean isErrornous() {
		return errorsReported.get();
	}

	public void awaitTasks(int numTasks) {
		for (int i = 0; i < numTasks; i++) {
			// the blocking queue's take method will block and wait for more
			// entries in the
			// queue
			try {
				countingQueue.take();
			} catch (InterruptedException e) {
				// if interrupted we will have to try once more, thus we
				// increment numTasks
				numTasks++;
			}
		}
	}
}
