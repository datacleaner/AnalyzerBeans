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
package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object that can handle the task of consuming rows.
 * 
 * TODO: Improve doc
 */
public class ConsumeRowHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConsumeRowHandler.class);

    private final List<RowProcessingConsumer> _consumers;
    private final Collection<? extends Outcome> _alwaysSatisfiedOutcomes;

    public static class Configuration {
        public boolean includeNonDistributedTasks = true;
        public AnalysisListener analysisListener = new InfoLoggingAnalysisListener();
        public boolean includeAnalyzers = true;
        public Collection<? extends Outcome> alwaysSatisfiedOutcomes;
    }

    /**
     * Builds a {@link ConsumeRowHandler} based on a job, and the configuration
     * to read the job's consumers
     * 
     * @param job
     * @param analyzerBeansConfiguration
     * @param configuration
     */
    public ConsumeRowHandler(AnalysisJob job, AnalyzerBeansConfiguration analyzerBeansConfiguration,
            Configuration configuration) {
        // TODO: Verify that there is only one source table
        _consumers = extractConsumers(job, analyzerBeansConfiguration, configuration);
        _alwaysSatisfiedOutcomes = configuration.alwaysSatisfiedOutcomes;
    }

    /**
     * Builds a {@link ConsumeRowHandler} based on a list of consumers.
     * 
     * @param consumers
     */
    public ConsumeRowHandler(List<RowProcessingConsumer> consumers) {
        this(consumers, null);
    }

    /**
     * Builds a {@link ConsumeRowHandler} based on a list of consumers as well
     * as a collection of always-satisfied outcomes.
     * 
     * @param consumers
     * @param alwaysSatisfiedOutcomes
     */
    public ConsumeRowHandler(List<RowProcessingConsumer> consumers,
            Collection<? extends Outcome> alwaysSatisfiedOutcomes) {
        _consumers = consumers;
        _alwaysSatisfiedOutcomes = alwaysSatisfiedOutcomes;
    }

    /**
     * Gets the {@link RowProcessingConsumer}s that this handler is working on.
     * 
     * @return
     */
    public List<RowProcessingConsumer> getConsumers() {
        return _consumers;
    }

    public List<InputRow> consume(InputRow row) {
        OutcomeSink outcomes = new OutcomeSinkImpl(_alwaysSatisfiedOutcomes);
        ConsumeRowHandlerDelegate delegate = new ConsumeRowHandlerDelegate(_consumers, row, 0, outcomes);
        List<InputRow> result = delegate.consume();
        return result;
    }

    private List<RowProcessingConsumer> extractConsumers(AnalysisJob analysisJob,
            AnalyzerBeansConfiguration analyzerBeansConfiguration, Configuration configuration) {
        final InjectionManager injectionManager = analyzerBeansConfiguration.getInjectionManager(analysisJob);
        final ReferenceDataActivationManager referenceDataActivationManager = new ReferenceDataActivationManager();

        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, referenceDataActivationManager,
                configuration.includeNonDistributedTasks);
        SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(analysisJob);

        /**
         * Use a single threaded task runner since this handler is invoked in a
         * blocking way - the calling code may itself be multithreaded without
         * issues.
         */
        final SingleThreadedTaskRunner taskRunner = new SingleThreadedTaskRunner();

        final AnalysisListener analysisListener = configuration.analysisListener;
        final RowProcessingPublishers rowProcessingPublishers = new RowProcessingPublishers(analysisJob,
                analysisListener, taskRunner, lifeCycleHelper, sourceColumnFinder);

        final Collection<RowProcessingPublisher> publisherCollection = rowProcessingPublishers
                .getRowProcessingPublishers();

        // TODO: Throw if not correct
        assert publisherCollection.size() == 1;

        final RowProcessingPublisher publisher = publisherCollection.iterator().next();

        final AtomicReference<Throwable> errorReference = new AtomicReference<Throwable>();

        publisher.initializeConsumers(new TaskListener() {
            @Override
            public void onError(Task task, Throwable throwable) {
                logger.error("Exception thrown while initializing consumers.", throwable);
                errorReference.compareAndSet(null, throwable);
            }

            @Override
            public void onComplete(Task task) {
                logger.info("Consumers initialized successfully.");
            }

            @Override
            public void onBegin(Task task) {
                logger.info("Beginning the process of initializing consumers.");
            }
        });

        final Throwable throwable = errorReference.get();
        if (throwable != null) {
            if (throwable instanceof RuntimeException) {

            }
        }

        List<RowProcessingConsumer> consumers = publisher.getConfigurableConsumers();
        if (!configuration.includeAnalyzers) {
            consumers = removeAnalyzers(consumers);
        }

        consumers = RowProcessingPublisher.sortConsumers(consumers);
        return consumers;
    }

    private List<RowProcessingConsumer> removeAnalyzers(List<RowProcessingConsumer> consumers) {
        final List<RowProcessingConsumer> result = new ArrayList<RowProcessingConsumer>();
        for (RowProcessingConsumer rowProcessingConsumer : consumers) {
            final Object component = rowProcessingConsumer.getComponent();
            if (!(component instanceof Analyzer<?>)) {
                result.add(rowProcessingConsumer);
            }
        }
        return result;
    }
}
