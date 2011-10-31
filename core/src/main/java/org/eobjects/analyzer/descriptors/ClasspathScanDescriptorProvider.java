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
package org.eobjects.analyzer.descriptors;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Explorer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.util.ClassLoaderUtils;
import org.eobjects.metamodel.util.FileHelper;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Descriptor provider implementation that works by scanning particular packages
 * in the classpath for annotated classes. Descriptors will be generated based
 * on encountered annotations.
 * 
 * This implementation also supports adding single descriptors by using the
 * add... methods.
 * 
 * Classes with either of these annotations will be picked up by the classpath
 * scanner:
 * <ul>
 * <li>{@link AnalyzerBean}</li>
 * <li>{@link TransformerBean}</li>
 * <li>{@link FilterBean}</li>
 * <li>{@link RendererBean}</li>
 * </ul>
 * 
 * @author Kasper Sørensen
 */
public final class ClasspathScanDescriptorProvider extends AbstractDescriptorProvider {

	private static final Logger logger = LoggerFactory.getLogger(ClasspathScanDescriptorProvider.class);

	private final Map<Class<? extends Analyzer<?>>, AnalyzerBeanDescriptor<?>> _analyzerBeanDescriptors = new HashMap<Class<? extends Analyzer<?>>, AnalyzerBeanDescriptor<?>>();
	private final Map<Class<? extends Filter<?>>, FilterBeanDescriptor<?, ?>> _filterBeanDescriptors = new HashMap<Class<? extends Filter<?>>, FilterBeanDescriptor<?, ?>>();
	private final Map<Class<? extends Transformer<?>>, TransformerBeanDescriptor<?>> _transformerBeanDescriptors = new HashMap<Class<? extends Transformer<?>>, TransformerBeanDescriptor<?>>();
	private final Map<Class<? extends Renderer<?, ?>>, RendererBeanDescriptor> _rendererBeanDescriptors = new HashMap<Class<? extends Renderer<?, ?>>, RendererBeanDescriptor>();
	private final Map<Class<? extends Explorer<?>>, ExplorerBeanDescriptor<?>> _explorerBeanDescriptors = new HashMap<Class<? extends Explorer<?>>, ExplorerBeanDescriptor<?>>();
	private final TaskRunner _taskRunner;
	private final AtomicInteger _tasksPending;

	public ClasspathScanDescriptorProvider() {
		this(new SingleThreadedTaskRunner());
	}

	/**
	 * Constructs a {@link ClasspathScanDescriptorProvider} using a specified
	 * {@link TaskRunner}. The taskrunner will be used to perform the classpath
	 * scan, potentially in a parallel fashion.
	 * 
	 * @param taskRunner
	 */
	public ClasspathScanDescriptorProvider(TaskRunner taskRunner) {
		_taskRunner = taskRunner;
		_tasksPending = new AtomicInteger(0);
	}

	/**
	 * Scans a package in the classpath (of the current thread's context
	 * classloader) for annotated components.
	 * 
	 * @param packageName
	 *            the package name to scan
	 * @param recursive
	 *            whether or not to scan subpackages recursively
	 * @return
	 */
	public ClasspathScanDescriptorProvider scanPackage(String packageName, boolean recursive) {
		return scanPackage(packageName, recursive, ClassLoaderUtils.getParentClassLoader(), false);
	}

	/**
	 * Scans a package in the classpath (of a particular classloader) for
	 * annotated components.
	 * 
	 * @param packageName
	 *            the package name to scan
	 * @param recursive
	 *            whether or not to scan subpackages recursively
	 * @param classLoader
	 *            the classloader to use
	 * @return
	 */
	public ClasspathScanDescriptorProvider scanPackage(final String packageName, final boolean recursive,
			final ClassLoader classLoader) {
		return scanPackage(packageName, recursive, classLoader, true);
	}

	/**
	 * Scans a package in the classpath (of a particular classloader) for
	 * annotated components.
	 * 
	 * @param packageName
	 *            the package name to scan
	 * @param recursive
	 *            whether or not to scan subpackages recursively
	 * @param classLoader
	 *            the classloader to use for discovering resources in the
	 *            classpath
	 * @param strictClassLoader
	 *            whether or not classes originating from other classloaders may
	 *            be included in scan (classloaders can sometimes discover
	 *            classes from parent classloaders which may or may not be
	 *            wanted for inclusion).
	 * @return
	 */
	public ClasspathScanDescriptorProvider scanPackage(final String packageName, final boolean recursive,
			final ClassLoader classLoader, final boolean strictClassLoader) {
		_tasksPending.incrementAndGet();
		final TaskListener listener = new TaskListener() {
			@Override
			public void onBegin(Task task) {
				logger.info("Scan of '{}' beginning", packageName);
			}

			@Override
			public void onComplete(Task task) {
				logger.info("Scan of '{}' complete", packageName);
				taskDone();
			}

			@Override
			public void onError(Task task, Throwable throwable) {
				logger.info("Scan of '{}' failed: {}", packageName, throwable.getMessage());
				logger.warn("Exception occurred while scanning and installing package", throwable);
				taskDone();
			}
		};
		final Task task = new Task() {
			@Override
			public void execute() throws Exception {
				String packagePath = packageName.replace('.', '/');
				if (recursive) {
					logger.info("Scanning package path '{}' (and subpackages recursively)", packagePath);
				} else {
					logger.info("Scanning package path '{}'", packagePath);
				}
				try {
					Enumeration<URL> resources = classLoader.getResources(packagePath);
					while (resources.hasMoreElements()) {
						URL resource = resources.nextElement();
						String file = resource.getFile();
						File dir = new File(file);
						dir = new File(dir.getAbsolutePath().replaceAll("\\%20", " "));

						if (dir.isDirectory()) {
							logger.debug("Resource is a file, scanning directory: {}", dir.getAbsolutePath());
							scanDirectory(dir, recursive, classLoader, strictClassLoader);
						} else {
							URLConnection connection = resource.openConnection();
							if (connection instanceof JarURLConnection) {
								JarURLConnection jarUrlConnection = (JarURLConnection) connection;
								logger.debug("Resource is a JAR file, scanning file: "
										+ jarUrlConnection.getJarFile().getName());
								scanJar(jarUrlConnection, classLoader, packagePath, recursive, strictClassLoader);
							} else {
								throw new IllegalStateException("Unknown connection type: " + connection);
							}
						}
					}
				} catch (IOException e) {
					logger.error("Could not open classpath resource", e);
				}
			}
		};
		_taskRunner.run(task, listener);
		return this;
	}

	private void scanJar(final JarURLConnection jarUrlConnection, final ClassLoader classLoader, final String packagePath,
			final boolean recursive, final boolean strictClassLoader) throws IOException {
		JarFile jarFile = jarUrlConnection.getJarFile();

		Enumeration<JarEntry> entries = jarFile.entries();

		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String entryName = entry.getName();
			if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
				if (recursive) {
					InputStream inputStream = jarFile.getInputStream(entry);
					scanInputStream(inputStream, classLoader, strictClassLoader);
				} else {
					String trailingPart = entryName.substring(packagePath.length());
					if (trailingPart.startsWith("/")) {
						trailingPart = trailingPart.substring(1);
					}
					if (trailingPart.indexOf('/') == -1) {
						InputStream inputStream = jarFile.getInputStream(entry);
						scanInputStream(inputStream, classLoader, strictClassLoader);
					} else {
						logger.debug("Omitting recursive JAR file entry: {}", entryName);
					}
				}
			} else {
				logger.debug("Omitting JAR file entry: {}", entryName);
			}
		}
	}

	private void scanDirectory(File dir, boolean recursive, ClassLoader classLoader, final boolean strictClassLoader) {
		if (!dir.exists()) {
			throw new IllegalArgumentException("Directory '" + dir + "' does not exist");
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("The file '" + dir + "' is not a directory");
		}
		logger.debug("Scanning directory: " + dir);

		File[] classFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String filename) {
				return filename.endsWith(".class");
			}
		});

		for (File file : classFiles) {
			try {
				InputStream inputStream = new FileInputStream(file);
				scanInputStream(inputStream, classLoader, strictClassLoader);
			} catch (IOException e) {
				logger.error("Could not read file", e);
			}
		}

		if (recursive) {
			File[] subDirectories = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isDirectory();
				}
			});
			if (subDirectories != null) {
				if (logger.isInfoEnabled() && subDirectories.length > 0) {
					logger.info("Recursively scanning " + subDirectories.length + " subdirectories");
				}
				for (File subDir : subDirectories) {
					scanDirectory(subDir, true, classLoader, strictClassLoader);
				}
			}
		}
	}

	protected void scanInputStream(final InputStream inputStream, final ClassLoader classLoader,
			final boolean strictClassLoader) throws IOException {
		try {
			final ClassReader classReader = new ClassReader(inputStream);
			final BeanClassVisitor visitor = new BeanClassVisitor(classLoader);
			classReader.accept(visitor, ClassReader.SKIP_CODE);

			Class<?> beanClass = visitor.getBeanClass();
			if (beanClass == null) {
				return;
			}
			
			if (strictClassLoader && classLoader != null && beanClass.getClassLoader() != classLoader) {
				logger.warn("Scanned class did not belong to required classloader: " + beanClass + ", ignoring");
				return;
			}

			if (visitor.isAnalyzer()) {
				@SuppressWarnings("unchecked")
				Class<? extends Analyzer<?>> analyzerClass = (Class<? extends Analyzer<?>>) beanClass;
				addAnalyzerClass(analyzerClass);
			}
			if (visitor.isExplorer()) {
				@SuppressWarnings("unchecked")
				Class<? extends Explorer<?>> explorerClass = (Class<? extends Explorer<?>>) beanClass;
				addExplorerClass(explorerClass);
			}
			if (visitor.isTransformer()) {
				@SuppressWarnings("unchecked")
				Class<? extends Transformer<?>> transformerClass = (Class<? extends Transformer<?>>) beanClass;
				addTransformerClass(transformerClass);
			}
			if (visitor.isFilter()) {
				@SuppressWarnings("unchecked")
				Class<? extends Filter<? extends Enum<?>>> filterClass = (Class<? extends Filter<?>>) beanClass;
				addFilterClass(filterClass);
			}
			if (visitor.isRenderer()) {
				@SuppressWarnings("unchecked")
				Class<? extends Renderer<?, ?>> rendererClass = (Class<? extends Renderer<?, ?>>) beanClass;
				addRendererClass(rendererClass);
			}
		} finally {
			FileHelper.safeClose(inputStream);
		}
	}

	public ClasspathScanDescriptorProvider addExplorerClass(Class<? extends Explorer<?>> explorerClass) {
		ExplorerBeanDescriptor<?> descriptor = _explorerBeanDescriptors.get(explorerClass);
		if (descriptor == null) {
			try {
				descriptor = Descriptors.ofExplorer(explorerClass);
				_explorerBeanDescriptors.put(explorerClass, descriptor);
			} catch (Exception e) {
				logger.error("Unexpected error occurred while creating descriptor for: " + explorerClass, e);
			}
		}
		return this;
	}

	public ClasspathScanDescriptorProvider addAnalyzerClass(Class<? extends Analyzer<?>> clazz) {
		AnalyzerBeanDescriptor<?> descriptor = _analyzerBeanDescriptors.get(clazz);
		if (descriptor == null) {
			try {
				descriptor = Descriptors.ofAnalyzer(clazz);
				_analyzerBeanDescriptors.put(clazz, descriptor);
			} catch (Exception e) {
				logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
			}
		}
		return this;
	}

	public ClasspathScanDescriptorProvider addTransformerClass(Class<? extends Transformer<?>> clazz) {
		TransformerBeanDescriptor<? extends Transformer<?>> descriptor = _transformerBeanDescriptors.get(clazz);
		if (descriptor == null) {
			try {
				descriptor = Descriptors.ofTransformer(clazz);
				_transformerBeanDescriptors.put(clazz, descriptor);
			} catch (Exception e) {
				logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
			}
		}
		return this;
	}

	public ClasspathScanDescriptorProvider addFilterClass(Class<? extends Filter<?>> clazz) {
		FilterBeanDescriptor<? extends Filter<?>, ?> descriptor = _filterBeanDescriptors.get(clazz);
		if (descriptor == null) {
			try {
				descriptor = Descriptors.ofFilterUnbound(clazz);
				_filterBeanDescriptors.put(clazz, descriptor);
			} catch (Exception e) {
				logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
			}
		}
		return this;
	}

	public ClasspathScanDescriptorProvider addRendererClass(Class<? extends Renderer<?, ?>> clazz) {
		RendererBeanDescriptor descriptor = _rendererBeanDescriptors.get(clazz);
		if (descriptor == null) {
			try {
				descriptor = Descriptors.ofRenderer(clazz);
				_rendererBeanDescriptors.put(clazz, descriptor);
			} catch (Exception e) {
				logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
			}
		}
		return this;
	}

	private void taskDone() {
		int tasks = _tasksPending.decrementAndGet();
		if (tasks == 0) {
			synchronized (this) {
				notifyAll();
			}
		}
	}

	/**
	 * Waits for all pending tasks to finish
	 */
	private void awaitTasks() {
		if (_tasksPending.get() == 0) {
			return;
		}
		synchronized (this) {
			while (_tasksPending.get() != 0) {
				try {
					logger.warn("Scan tasks still pending, waiting");
					wait();
				} catch (InterruptedException e) {
					logger.debug("Interrupted while awaiting task completion", e);
				}
			}
		}
	}

	@Override
	public Collection<FilterBeanDescriptor<?, ?>> getFilterBeanDescriptors() {
		awaitTasks();
		return Collections.unmodifiableCollection(_filterBeanDescriptors.values());
	}

	@Override
	public Collection<AnalyzerBeanDescriptor<?>> getAnalyzerBeanDescriptors() {
		awaitTasks();
		return Collections.unmodifiableCollection(_analyzerBeanDescriptors.values());
	}

	@Override
	public Collection<TransformerBeanDescriptor<?>> getTransformerBeanDescriptors() {
		awaitTasks();
		return Collections.unmodifiableCollection(_transformerBeanDescriptors.values());
	}

	@Override
	public Collection<RendererBeanDescriptor> getRendererBeanDescriptors() {
		awaitTasks();
		return Collections.unmodifiableCollection(_rendererBeanDescriptors.values());
	}

	@Override
	public Collection<ExplorerBeanDescriptor<?>> getExplorerBeanDescriptors() {
		awaitTasks();
		return Collections.unmodifiableCollection(_explorerBeanDescriptors.values());
	}
}
