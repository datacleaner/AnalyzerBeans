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
package org.eobjects.analyzer.job.concurrent;

import org.eobjects.analyzer.job.tasks.Task;

/**
 * Interface for the execution engine in AnalyzerBeans. The {@link TaskRunner}
 * is responsible for executing tasks, typically of rather small sizes. A task
 * runner is an abstraction over such execution details as thread pools, timer
 * services and clustering environments.
 */
public interface TaskRunner {

    public void run(Task task, TaskListener listener);

    public void run(TaskRunnable taskRunnable);

    public void shutdown();

    public void assistExecution();
}
