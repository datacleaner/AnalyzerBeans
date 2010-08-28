package org.eobjects.analyzer.cli;

import java.io.File;
import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.JaxbConfigurationFactory;
import org.eobjects.analyzer.job.AnalysisJobBuilder;
import org.eobjects.analyzer.job.JaxbJobFactory;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public final class Main {

	@Option(name = "-job", usage = "XML file describing the analysis job", required = true)
	private File jobFile;

	@Option(name = "-configuration", usage = "XML file describing the configuration of AnalyzerBeans", required = true)
	private File configurationFile;

	public static void main(String[] args) {
		Main main = new Main();
		CmdLineParser parser = new CmdLineParser(main);
		try {
			parser.parseArgument(args);
			main.run();
		} catch (CmdLineException e) {
			e.printStackTrace();
			parser.printUsage(System.out);
		}
	}

	public void run() {
		AnalyzerBeansConfiguration configuration = new JaxbConfigurationFactory()
				.create(configurationFile);
		AnalysisJobBuilder analysisJobBuilder = new JaxbJobFactory(
				configuration).create(jobFile);

		List<AnalyzerResult> results = new AnalysisRunnerImpl(configuration)
				.run(analysisJobBuilder.toAnalysisJob()).getResults();

		for (AnalyzerResult analyzerResult : results) {
			System.out.println("RESULT: " + analyzerResult);
		}

		TaskRunner taskRunner = configuration.getTaskRunner();
		taskRunner.shutdown();
	}
}
