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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.metamodel.util.FileHelper;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClasspathScanDescriptorProvider extends AbstractDescriptorProvider {

	private static final Logger logger = LoggerFactory.getLogger(ClasspathScanDescriptorProvider.class);

	private final Map<Class<? extends Analyzer<?>>, AnalyzerBeanDescriptor<?>> _analyzerBeanDescriptors = new HashMap<Class<? extends Analyzer<?>>, AnalyzerBeanDescriptor<?>>();
	private final Map<Class<? extends Filter<?>>, FilterBeanDescriptor<?, ?>> _filterBeanDescriptors = new HashMap<Class<? extends Filter<?>>, FilterBeanDescriptor<?, ?>>();
	private final Map<Class<? extends Transformer<?>>, TransformerBeanDescriptor<?>> _transformerBeanDescriptors = new HashMap<Class<? extends Transformer<?>>, TransformerBeanDescriptor<?>>();
	private final Map<Class<? extends Renderer<?, ?>>, RendererBeanDescriptor> _rendererBeanDescriptors = new HashMap<Class<? extends Renderer<?, ?>>, RendererBeanDescriptor>();

	public ClasspathScanDescriptorProvider scanPackage(String packageName, boolean recursive) {
		return scanPackage(packageName, recursive, Thread.currentThread().getContextClassLoader());
	}

	public ClasspathScanDescriptorProvider scanPackage(String packageName, boolean recursive, ClassLoader classLoader) {
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
					scanDirectory(dir, recursive, classLoader);
				} else {
					URLConnection connection = resource.openConnection();
					if (connection instanceof JarURLConnection) {
						JarURLConnection jarUrlConnection = (JarURLConnection) connection;
						logger.debug("Resource is a JAR file, scanning file: " + jarUrlConnection.getJarFile().getName());
						scanJar(jarUrlConnection, classLoader, packagePath, recursive);
					} else {
						throw new IllegalStateException("Unknown connection type: " + connection);
					}
				}
			}
		} catch (IOException e) {
			logger.error("Could not open classpath resource", e);
		}

		return this;
	}

	private void scanJar(JarURLConnection jarUrlConnection, ClassLoader classLoader, String packagePath, boolean recursive)
			throws IOException {
		JarFile jarFile = jarUrlConnection.getJarFile();
		Enumeration<JarEntry> entries = jarFile.entries();

		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String entryName = entry.getName();
			if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
				if (recursive) {
					InputStream inputStream = jarFile.getInputStream(entry);
					scanInputStream(inputStream, classLoader);
				} else {
					String trailingPart = entryName.substring(packagePath.length());
					if (trailingPart.startsWith("/")) {
						trailingPart = trailingPart.substring(1);
					}
					if (trailingPart.indexOf('/') == -1) {
						InputStream inputStream = jarFile.getInputStream(entry);
						scanInputStream(inputStream, classLoader);
					} else {
						logger.debug("Omitting recursive JAR file entry: {}", entryName);
					}
				}
			} else {
				logger.debug("Omitting JAR file entry: {}", entryName);
			}
		}
	}

	private void scanDirectory(File dir, boolean recursive, ClassLoader classLoader) {
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
				scanInputStream(inputStream, classLoader);
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
					scanDirectory(subDir, true, classLoader);
				}
			}
		}
	}

	protected void scanInputStream(final InputStream inputStream, final ClassLoader classLoader) throws IOException {
		try {
			final ClassReader classReader = new ClassReader(inputStream);
			final BeanClassVisitor visitor = new BeanClassVisitor(classLoader);
			classReader.accept(visitor, ClassReader.SKIP_CODE);

			if (visitor.isAnalyzer()) {
				@SuppressWarnings("unchecked")
				Class<? extends Analyzer<?>> analyzerClass = (Class<? extends Analyzer<?>>) visitor.getBeanClass();
				addAnalyzerClass(analyzerClass);
			}
			if (visitor.isTransformer()) {
				@SuppressWarnings("unchecked")
				Class<? extends Transformer<?>> transformerClass = (Class<? extends Transformer<?>>) visitor.getBeanClass();
				addTransformerClass(transformerClass);
			}
			if (visitor.isFilter()) {
				@SuppressWarnings("unchecked")
				Class<? extends Filter<? extends Enum<?>>> filterClass = (Class<? extends Filter<?>>) visitor.getBeanClass();
				addFilterClass(filterClass);
			}
			if (visitor.isRenderer()) {
				@SuppressWarnings("unchecked")
				Class<? extends Renderer<?, ?>> rendererClass = (Class<? extends Renderer<?, ?>>) visitor.getBeanClass();
				addRendererClass(rendererClass);
			}
		} finally {
			FileHelper.safeClose(inputStream);
		}
	}

	public void addAnalyzerClass(Class<? extends Analyzer<?>> clazz) {
		AnalyzerBeanDescriptor<?> descriptor = _analyzerBeanDescriptors.get(clazz);
		if (descriptor == null) {
			try {
				descriptor = AnnotationBasedAnalyzerBeanDescriptor.create(clazz);
				_analyzerBeanDescriptors.put(clazz, descriptor);
			} catch (Exception e) {
				logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
			}
		}
	}

	public void addTransformerClass(Class<? extends Transformer<?>> clazz) {
		TransformerBeanDescriptor<? extends Transformer<?>> descriptor = _transformerBeanDescriptors.get(clazz);
		if (descriptor == null) {
			try {
				descriptor = AnnotationBasedTransformerBeanDescriptor.create(clazz);
				_transformerBeanDescriptors.put(clazz, descriptor);
			} catch (Exception e) {
				logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
			}
		}
	}

	public void addFilterClass(Class<? extends Filter<?>> clazz) {
		FilterBeanDescriptor<? extends Filter<?>, ?> descriptor = _filterBeanDescriptors.get(clazz);
		if (descriptor == null) {
			try {
				descriptor = AnnotationBasedFilterBeanDescriptor.create(clazz);
				_filterBeanDescriptors.put(clazz, descriptor);
			} catch (Exception e) {
				logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
			}
		}
	}

	public void addRendererClass(Class<? extends Renderer<?, ?>> clazz) {
		RendererBeanDescriptor descriptor = _rendererBeanDescriptors.get(clazz);
		if (descriptor == null) {
			try {
				descriptor = new AnnotationBasedRendererBeanDescriptor(clazz);
				_rendererBeanDescriptors.put(clazz, descriptor);
			} catch (Exception e) {
				logger.error("Unexpected error occurred while creating descriptor for: " + clazz, e);
			}
		}
	}

	@Override
	public Collection<FilterBeanDescriptor<?, ?>> getFilterBeanDescriptors() {
		return Collections.unmodifiableCollection(_filterBeanDescriptors.values());
	}

	@Override
	public Collection<AnalyzerBeanDescriptor<?>> getAnalyzerBeanDescriptors() {
		return Collections.unmodifiableCollection(_analyzerBeanDescriptors.values());
	}

	@Override
	public Collection<TransformerBeanDescriptor<?>> getTransformerBeanDescriptors() {
		return Collections.unmodifiableCollection(_transformerBeanDescriptors.values());
	}

	@Override
	public Collection<RendererBeanDescriptor> getRendererBeanDescriptors() {
		return Collections.unmodifiableCollection(_rendererBeanDescriptors.values());
	}
}
