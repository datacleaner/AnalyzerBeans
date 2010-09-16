package org.eobjects.analyzer.cli;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.JaxbConfigurationFactory;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.AnalysisJobBuilder;
import org.eobjects.analyzer.job.JaxbJobFactory;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.result.renderer.TextRenderingFormat;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public final class Main {

	public static enum ListType {
		ANALYZERS, TRANSFORMERS, DATASTORES, SCHEMAS, TABLES, COLUMNS
	}

	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	@Option(name = "-conf", aliases = { "-configuration",
			"--configuration-file" }, usage = "XML file describing the configuration of AnalyzerBeans", required = true)
	private File configurationFile;

	@Option(name = "-job", aliases = { "--job-file" }, usage = "An analysis job XML file to execute")
	private File jobFile;

	@Option(name = "-list", usage = "Used to print a list of various elements available in the configuration")
	private ListType listType;

	@Option(name = "-ds", aliases = { "-datastore", "--datastore-name" }, usage = "Name of datastore when printing a list of schemas, tables or columns")
	private String datastoreName;

	@Option(name = "-s", aliases = { "-schema", "--schema-name" }, usage = "Name of schema when printing a list of tables or columns")
	private String schemaName;

	@Option(name = "-t", aliases = { "-table", "--table-name" }, usage = "Name of table when printing a list of columns")
	private String tableName;

	public static void main(String[] args) {
		Main main = new Main();
		CmdLineParser parser = new CmdLineParser(main);
		try {
			parser.parseArgument(args);
			main.run();
		} catch (CmdLineException e) {
			parser.setUsageWidth(120);
			parser.printUsage(System.out);
		}
	}

	public void run() {
		AnalyzerBeansConfiguration configuration = new JaxbConfigurationFactory()
				.create(configurationFile);
		try {
			if (jobFile != null) {
				runJob(configuration);
			} else if (listType != null) {
				switch (listType) {
				case ANALYZERS:
					printAnalyzers(configuration);
					break;
				case TRANSFORMERS:
					printTransformers(configuration);
					break;
				case DATASTORES:
					printDatastores(configuration);
					break;
				case SCHEMAS:
					printSchemas(configuration);
					break;
				case TABLES:
					printTables(configuration);
					break;
				case COLUMNS:
					printColumns(configuration);
					break;
				default:
					throw new IllegalArgumentException("Unknown list type: "
							+ listType);
				}
			} else {
				throw new IllegalArgumentException(
						"Neither --job-file nor --list-type is specified. Try running with -usage to see usage help.");
			}
		} catch (Exception e) {
			logger.error("Exception thrown in {}", e, this);
			System.err.println("Error: " + e.getMessage());
		} finally {
			configuration.getTaskRunner().shutdown();
		}
	}

	private void printColumns(AnalyzerBeansConfiguration configuration) {
		if (datastoreName == null) {
			System.err.println("You need to specify the datastore name!");
		} else if (tableName == null) {
			System.err.println("You need to specify a table name!");
		} else {
			Datastore ds = configuration.getDatastoreCatalog().getDatastore(
					datastoreName);
			if (ds == null) {
				System.err.println("No such datastore: " + datastoreName);
			} else {
				DataContext dc = ds.getDataContextProvider().getDataContext();
				Schema schema;
				if (schemaName == null) {
					schema = dc.getDefaultSchema();
				} else {
					schema = dc.getSchemaByName(schemaName);
				}
				if (schema == null) {
					System.err.println("No such schema: " + schemaName);
				} else {
					Table table = schema.getTableByName(tableName);
					if (table == null) {
						System.out.println("No such table: " + tableName);
					} else {
						String[] columnNames = table.getColumnNames();
						System.out.println("Columns:");
						System.out.println("--------");
						for (String columnName : columnNames) {
							System.out.println(columnName);
						}
					}
				}
			}
		}
	}

	private void printTables(AnalyzerBeansConfiguration configuration) {
		if (datastoreName == null) {
			System.err.println("You need to specify the datastore name!");
		} else {
			Datastore ds = configuration.getDatastoreCatalog().getDatastore(
					datastoreName);
			if (ds == null) {
				System.err.println("No such datastore: " + datastoreName);
			} else {
				DataContext dc = ds.getDataContextProvider().getDataContext();
				Schema schema;
				if (schemaName == null) {
					schema = dc.getDefaultSchema();
				} else {
					schema = dc.getSchemaByName(schemaName);
				}
				if (schema == null) {
					System.err.println("No such schema: " + schemaName);
				} else {
					String[] tableNames = schema.getTableNames();
					if (tableNames == null || tableNames.length == 0) {
						System.err.println("No tables in schema!");
					} else {
						System.out.println("Tables:");
						System.out.println("-------");
						for (String tableName : tableNames) {
							System.out.println(tableName);
						}
					}
				}
			}
		}
	}

	private void printSchemas(AnalyzerBeansConfiguration configuration) {
		if (datastoreName == null) {
			System.err.println("You need to specify the datastore name!");
		} else {
			Datastore ds = configuration.getDatastoreCatalog().getDatastore(
					datastoreName);
			if (ds == null) {
				System.err.println("No such datastore: " + datastoreName);
			} else {
				String[] schemaNames = ds.getDataContextProvider()
						.getDataContext().getSchemaNames();
				if (schemaNames == null || schemaNames.length == 0) {
					System.out.println("No schemas in datastore!");
				} else {
					System.out.println("Schemas:");
					System.out.println("--------");
					for (String schemaName : schemaNames) {
						System.out.println(schemaName);
					}
				}
			}
		}
	}

	private void printDatastores(AnalyzerBeansConfiguration configuration) {
		String[] datastoreNames = configuration.getDatastoreCatalog()
				.getDatastoreNames();
		if (datastoreNames == null || datastoreNames.length == 0) {
			System.out.println("No datastores configured!");
		} else {
			System.out.println("Datastores:");
			System.out.println("-----------");
			for (String datastoreName : datastoreNames) {
				System.out.println(datastoreName);
			}
		}
	}

	protected void runJob(AnalyzerBeansConfiguration configuration) {
		AnalysisJobBuilder analysisJobBuilder = new JaxbJobFactory(
				configuration).create(jobFile);

		List<AnalyzerResult> results = new AnalysisRunnerImpl(configuration)
				.run(analysisJobBuilder.toAnalysisJob()).getResults();

		RendererFactory rendererFinder = new RendererFactory(
				configuration.getDescriptorProvider());

		for (AnalyzerResult result : results) {
			System.out.println("\nRESULT:");

			Renderer<? super AnalyzerResult, ? extends CharSequence> renderer = rendererFinder
					.getRenderer(result, TextRenderingFormat.class);
			CharSequence renderedResult = renderer.render(result);

			System.out.println(renderedResult);
		}
	}

	protected void printAnalyzers(AnalyzerBeansConfiguration configuration) {
		Collection<AnalyzerBeanDescriptor<?>> descriptors = configuration
				.getDescriptorProvider().getAnalyzerBeanDescriptors();
		if (descriptors == null || descriptors.isEmpty()) {
			System.out.println("No analyzers configured!");
		} else {
			System.out.println("Analyzers:");
			System.out.println("----------");
			printBeanDescriptors(descriptors);
		}
	}

	private void printTransformers(AnalyzerBeansConfiguration configuration) {
		Collection<TransformerBeanDescriptor<?>> descriptors = configuration
				.getDescriptorProvider().getTransformerBeanDescriptors();
		if (descriptors == null || descriptors.isEmpty()) {
			System.out.println("No transformers configured!");
		} else {
			System.out.println("Transformers:");
			System.out.println("-------------");
			printBeanDescriptors(descriptors);
		}
	}

	protected void printBeanDescriptors(
			Collection<? extends BeanDescriptor<?>> descriptors) {
		for (BeanDescriptor<?> descriptor : descriptors) {
			System.out.println("name: " + descriptor.getDisplayName());
			Set<ConfiguredPropertyDescriptor> propertiesForInput = descriptor
					.getConfiguredPropertiesForInput();
			if (propertiesForInput.size() == 1) {
				ConfiguredPropertyDescriptor propertyForInput = propertiesForInput
						.iterator().next();
				if (propertyForInput != null) {
					if (propertyForInput.isArray()) {
						System.out
								.println(" - Consumes multiple input columns (type: "
										+ propertyForInput
												.getInputColumnDataTypeFamily()
										+ ")");
					} else {
						System.out
								.println(" - Consumes a single input column (type: "
										+ propertyForInput
												.getInputColumnDataTypeFamily()
										+ ")");
					}
				}
			} else {
				System.out.println(" - Consumes " + propertiesForInput.size()
						+ " named inputs");
				for (ConfiguredPropertyDescriptor propertyForInput : propertiesForInput) {
					if (propertyForInput.isArray()) {
						System.out.println("   Input columns: "
								+ propertyForInput.getName()
								+ " (type: "
								+ propertyForInput
										.getInputColumnDataTypeFamily() + ")");
					} else {
						System.out.println("   Input column: "
								+ propertyForInput.getName()
								+ " (type: "
								+ propertyForInput
										.getInputColumnDataTypeFamily() + ")");
					}
				}
			}

			Set<ConfiguredPropertyDescriptor> properties = descriptor
					.getConfiguredProperties();
			for (ConfiguredPropertyDescriptor property : properties) {
				if (!property.isInputColumn()) {
					System.out.println(" - Property: name="
							+ property.getName() + ", type="
							+ property.getBaseType().getSimpleName()
							+ ", required=" + property.isRequired());
				}
			}
		}
	}

	@Override
	public String toString() {
		return "Main[configurationFile="
				+ (configurationFile == null ? "null" : configurationFile
						.getName()) + ", jobFile="
				+ (jobFile == null ? "null" : jobFile.getName())
				+ ", listType=" + listType + ", datastoreName=" + datastoreName
				+ ", schemaName=" + schemaName + ", tableName=" + tableName
				+ "]";
	}
}
