package org.eobjects.analyzer.beans.script;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.reference.Function;
import org.eobjects.analyzer.util.JavaClassHandler;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TransformerBean("Java function transformer")
public class JavaFunctionTransformer implements Transformer<String> {

	private static final Logger logger = LoggerFactory.getLogger(JavaFunctionTransformer.class);

	@Configured
	InputColumn<String> column;

	@Configured
	String javaCode = "package org.eobjects.analyzer.user.scripts;\n\n" + "import org.eobjects.analyzer.util.*;\n"
			+ "import org.eobjects.analyzer.reference.*;\n\n"
			+ "public class MyFunction implements Function<String,String> {\n"
			+ "\tpublic String run(String str) throws Exception {\n" + "\t\treturn str;\n" + "\t}" + "}";

	private Function<String, String> function;

	public JavaFunctionTransformer() {
	}

	/**
	 * Constructor used for testing
	 * 
	 * @param javaCode
	 * @param column
	 */
	public JavaFunctionTransformer(String javaCode, InputColumn<String> column) {
		this.javaCode = javaCode;
		this.column = column;
		init();
	}

	@SuppressWarnings("unchecked")
	@Initialize
	public void init() {
		logger.debug("Function code:\n{}", javaCode);
		try {
			logger.debug("Getting JavaClassHandler...");
			JavaClassHandler javaClassHandler = UserScripts.JAVA_CLASS_HANDLER;

			logger.debug("Compiling function code...");
			Class<?> functionClass = javaClassHandler.compileAndLoad(javaCode);
			assert ReflectionUtils.is(functionClass, Function.class);

			logger.debug("Instantiating function...");
			function = (Function<String, String>) functionClass.newInstance();

			logger.debug("Initialization success!");
		} catch (Exception e) {
			logger.error("Could not compile, load or instantiate function code", e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public OutputColumns getOutputColumns() {
		return OutputColumns.singleOutputColumn();
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String value = inputRow.getValue(column);
		try {
			logger.debug("value before function.run(...): {}", value);
			value = function.run(value);
			logger.debug("value after function.run(...): {}", value);
		} catch (Exception e) {
			logger.error("Exception when invoking run(...) method on function", e);
		}
		return new String[] { value };
	}

}
