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
package org.eobjects.analyzer.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.util.Collection;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eobjects.metamodel.util.FileHelper;

public class JavaClassHandlerImpl implements JavaClassHandler {

	public static final String PACKAGE_TOKEN = "package ";
	public static final String CLASS_TOKEN = "class ";

	private ClassLoader _classLoader;
	private File _classDirectory;
	private JavaCompiler _compiler;

	private static final Logger logger = LoggerFactory.getLogger(JavaClassHandlerImpl.class);

	public JavaClassHandlerImpl(final File classDirectory) throws IllegalArgumentException {
		if (classDirectory == null) {
			throw new IllegalArgumentException("class directory cannot be null");
		}
		if (!classDirectory.exists() || !classDirectory.isDirectory()) {
			throw new IllegalArgumentException("class directory must be an existing directory");
		}

		_classDirectory = classDirectory;
		final URL url;
		try {
			url = classDirectory.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		_classLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
			public URLClassLoader run() {
				return new URLClassLoader(new URL[] { url }, getClass().getClassLoader());
			}
		});
	}

	@Override
	public Class<?> compileAndLoad(String javaCode) throws IllegalArgumentException, IllegalStateException {
		if (javaCode == null) {
			throw new IllegalArgumentException("javaCode cannot be null");
		}

		final String packageName = getPackageName(javaCode);
		logger.debug("package name: {}", packageName);
		final String className = getClassName(javaCode);
		logger.debug("class name: {}", className);

		final String qualifiedClassName;
		final File dir;

		if (packageName == null) {
			qualifiedClassName = className;
			dir = _classDirectory;
		} else {
			qualifiedClassName = packageName + '.' + className;
			String folderName = packageName.replaceAll("\\.", "\\" + File.separatorChar);
			dir = new File(_classDirectory, folderName);
		}

		if (dir.exists()) {
			if (dir.isDirectory()) {
				logger.debug("directory {} already exists", dir.getAbsolutePath());
			} else {
				throw new IllegalStateException(dir.getAbsolutePath() + " exists but is not a directory!");
			}
		} else {
			boolean result = dir.mkdirs();
			if (result) {
				logger.debug("{} created", dir.getAbsolutePath());
			} else {
				throw new IllegalStateException("Could not create directory: " + dir.getAbsolutePath());
			}
		}

		File file = new File(dir, className + ".java");
		logger.info("Saving java code for class {} in {}", qualifiedClassName, file.getAbsolutePath());
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new IllegalStateException("Could not create source .java file", e);
			}
		}

		FileHelper.writeStringAsFile(file, javaCode);

		if (_compiler == null) {
			_compiler = ToolProvider.getSystemJavaCompiler();
		}

		if (_compiler == null) {
			throw new IllegalStateException(
					"No Java compiler available, please use a JDK and not a JRE for custom user-written Java classes");
		}

		StandardJavaFileManager standardFileManager = _compiler.getStandardFileManager(null, null, null);
		JavaFileManager fileManager = new JavaClassHandlerFileManager(standardFileManager, _classLoader);

		Iterable<? extends JavaFileObject> javaFiles = standardFileManager.getJavaFileObjects(file);
		JavaFileObject javaFile = javaFiles.iterator().next();

		StringWriter out = new StringWriter();
		List<String> options = new LinkedList<String>();

		Collection<JavaFileObject> compilationUnits = CollectionUtils.set(javaFile);
		CompilationTask task = _compiler.getTask(out, fileManager, null, options, null, compilationUnits);

		boolean result = task.call();
		String output = out.toString();
		if (!result) {
			String message;
			if (logger.isErrorEnabled()) {
				logger.error(output);
				message = "Could not compile class. See log for details.";
			} else {
				message = "Could not compile class. \"" + output + '"';
			}
			throw new IllegalArgumentException(message);
		}

		if (logger.isInfoEnabled()) {
			logger.info("compilation success, output was: \"{}\"", output);
		}

		try {
			return Class.forName(qualifiedClassName, true, _classLoader);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	protected String getPackageName(String javaCode) {
		String packageName = null;
		int i1 = javaCode.indexOf(PACKAGE_TOKEN);
		if (i1 != -1) {
			int i2 = javaCode.indexOf(";", i1 + PACKAGE_TOKEN.length());
			if (i2 != -1) {
				packageName = trimJavaSymbol(javaCode.substring(i1 + PACKAGE_TOKEN.length(), i2));
				logger.debug("found package name: {}", packageName);
			}
		}
		return packageName;
	}

	protected String getClassName(String javaCode) {
		int i1 = 0;
		boolean foundClass = false;
		while (!foundClass) {
			i1 = javaCode.indexOf(CLASS_TOKEN, i1 + 1);
			if (i1 == -1) {
				throw new IllegalArgumentException("Could not find class name");
			}

			if (i1 == 0) {
				foundClass = true;
			} else {
				char charBefore = javaCode.charAt(i1 - 1);
				if (!Character.isLetter(charBefore)) {
					String codeBefore = javaCode.substring(0, i1);
					int importIndex = codeBefore.lastIndexOf("import");
					int semicolonIndex = codeBefore.lastIndexOf(';');
					if (importIndex == -1) {
						foundClass = true;
					} else {
						if (semicolonIndex != -1) {
							if (semicolonIndex > importIndex) {
								foundClass = true;
							}
						}
					}
				}
			}
		}

		int bodyIndex = javaCode.indexOf("{", i1 + CLASS_TOKEN.length());
		int typeParamIndex = javaCode.indexOf("<", i1 + CLASS_TOKEN.length());
		int implIndex = javaCode.indexOf(" implements", i1 + CLASS_TOKEN.length());
		int extendsIndex = javaCode.indexOf(" extends", i1 + CLASS_TOKEN.length());

		int i2 = minButPositive(bodyIndex, typeParamIndex, implIndex, extendsIndex);
		String className = trimJavaSymbol(javaCode.substring(i1 + CLASS_TOKEN.length(), i2));

		return className;
	}

	private int minButPositive(int... numbers) {
		int result = Integer.MAX_VALUE;
		for (int i : numbers) {
			if (i >= 0 && i < result) {
				result = i;
			}
		}
		return result;
	}

	private String trimJavaSymbol(String str) {
		CharIterator it = new CharIterator(str);
		while (it.hasNext()) {
			it.next();
			if (it.isWhitespace()) {
				it.remove();
			}
		}
		return it.toString();
	}
}
