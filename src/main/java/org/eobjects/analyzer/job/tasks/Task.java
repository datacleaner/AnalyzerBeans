package org.eobjects.analyzer.job.tasks;


/**
 * Represents a (sub)task that can run in parallel when running an analysis job.
 * 
 * @author Kasper Sørensen
 */
public interface Task {

	public void execute() throws Exception;
}
