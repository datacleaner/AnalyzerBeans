package org.eobjects.analyzer.job.concurrency;

import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.Task;

import junit.framework.TestCase;

public class SingleThreadedTaskRunnerTest extends TestCase {

	private static class SimpleRecursiveTask implements Task {
		private TaskRunner taskRunner;
		private Task nextTask;
		private char charToPrint;
		private StringBuilder sb;

		public SimpleRecursiveTask(StringBuilder sb, char c, Task nextTask, TaskRunner taskRunner) {
			this.sb = sb;
			this.charToPrint = c;
			this.nextTask = nextTask;
			this.taskRunner = taskRunner;
		}

		@Override
		public void execute() throws Exception {
			sb.append(charToPrint);
			if (nextTask != null) {
				taskRunner.run(nextTask, null);
			}
			sb.append(charToPrint);
		}
	}

	public void testNonQueuedChronology() throws Exception {
		SingleThreadedTaskRunner runner = new SingleThreadedTaskRunner(false);

		StringBuilder sb = new StringBuilder();
		Task task3 = new SimpleRecursiveTask(sb, 'c', null, null);
		Task task2 = new SimpleRecursiveTask(sb, 'b', task3, runner);
		Task task1 = new SimpleRecursiveTask(sb, 'a', task2, runner);

		runner.run(task1, null);

		assertEquals("abccba", sb.toString());
	}

	public void testQueuedChronology() throws Exception {
		SingleThreadedTaskRunner runner = new SingleThreadedTaskRunner();

		StringBuilder sb = new StringBuilder();
		Task task3 = new SimpleRecursiveTask(sb, 'c', null, null);
		Task task2 = new SimpleRecursiveTask(sb, 'b', task3, runner);
		Task task1 = new SimpleRecursiveTask(sb, 'a', task2, runner);

		runner.run(task1, null);

		assertEquals("aabbcc", sb.toString());
	}
}
