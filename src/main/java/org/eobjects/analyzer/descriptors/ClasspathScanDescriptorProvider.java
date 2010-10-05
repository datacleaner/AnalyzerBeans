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
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClasspathScanDescriptorProvider extends
		AbstractDescriptorProvider {

	private static final Logger logger = LoggerFactory
			.getLogger(ClasspathScanDescriptorProvider.class);

	private Map<Class<? extends Analyzer<?>>, AnalyzerBeanDescriptor<?>> _analyzerBeanDescriptors = new HashMap<Class<? extends Analyzer<?>>, AnalyzerBeanDescriptor<?>>();
	private Map<Class<? extends Transformer<?>>, TransformerBeanDescriptor<?>> _transformerBeanDescriptors = new HashMap<Class<? extends Transformer<?>>, TransformerBeanDescriptor<?>>();
	private Map<Class<? extends Renderer<?, ?>>, RendererBeanDescriptor> _rendererBeanDescriptors = new HashMap<Class<? extends Renderer<?, ?>>, RendererBeanDescriptor>();

	public ClasspathScanDescriptorProvider scanPackage(String packageName,
			boolean recursive) {
		String packagePath = packageName.replace('.', '/');
		logger.debug("Scanning package path: {}", packagePath);
		try {
			Enumeration<URL> resources = ClassLoader
					.getSystemResources(packagePath);
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				File dir = new File(resource.getFile());

				if (dir.isDirectory()) {
					logger.debug("Resource is a file, scanning directory: {}",
							dir.getAbsolutePath());
					scanDirectory(dir, recursive);
				} else {
					URLConnection connection = resource.openConnection();
					if (connection instanceof JarURLConnection) {
						JarURLConnection jarUrlConnection = (JarURLConnection) connection;
						logger.debug("Resource is a JAR file, scanning file: "
								+ jarUrlConnection.getJarFile().getName());
						scanJar(jarUrlConnection, packagePath, recursive);
					} else {
						throw new IllegalStateException(
								"Unknown connection type: " + connection);
					}
				}
			}
		} catch (IOException e) {
			logger.error("Could not open classpath resource", e);
		}

		return this;
	}

	private void scanJar(JarURLConnection jarUrlConnection, String packagePath,
			boolean recursive) throws IOException {
		JarFile jarFile = jarUrlConnection.getJarFile();
		Enumeration<JarEntry> entries = jarFile.entries();

		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String entryName = entry.getName();
			if (entryName.startsWith(packagePath)
					&& entryName.endsWith(".class")) {
				if (recursive) {
					InputStream inputStream = jarFile.getInputStream(entry);
					scanInputStream(inputStream);
				} else {
					String trailingPart = entryName.substring(packagePath
							.length());
					if (trailingPart.startsWith("/")) {
						trailingPart = trailingPart.substring(1);
					}
					if (trailingPart.indexOf('/') == -1) {
						InputStream inputStream = jarFile.getInputStream(entry);
						scanInputStream(inputStream);
					} else {
						logger.debug("Omitting recursive JAR file entry: {}",
								entryName);
					}
				}
			} else {
				logger.debug("Omitting JAR file entry: {}", entryName);
			}
		}
	}

	private void scanDirectory(File dir, boolean recursive) {
		if (!dir.exists()) {
			throw new IllegalArgumentException("Directory '" + dir
					+ "' does not exist");
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("The file '" + dir
					+ "' is not a directory");
		}
		logger.info("Scanning directory: " + dir);

		File[] classFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String filename) {
				return filename.endsWith(".class");
			}
		});

		for (File file : classFiles) {
			try {
				InputStream inputStream = new FileInputStream(file);
				scanInputStream(inputStream);
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
			if (logger.isInfoEnabled() && subDirectories.length > 0) {
				logger.info("Recursively scanning " + subDirectories.length
						+ " subdirectories");
			}
			for (File subDir : subDirectories) {
				scanDirectory(subDir, true);
			}
		}
	}

	protected void scanInputStream(InputStream inputStream) throws IOException {
		ClassReader classReader = new ClassReader(inputStream);
		BeanClassVisitor visitor = new BeanClassVisitor();
		classReader.accept(visitor, ClassReader.SKIP_CODE);

		if (visitor.isAnalyzer()) {
			@SuppressWarnings("unchecked")
			Class<? extends Analyzer<?>> analyzerClass = (Class<? extends Analyzer<?>>) visitor
					.getBeanClass();
			AnalyzerBeanDescriptor<?> descriptor = _analyzerBeanDescriptors
					.get(analyzerClass);
			if (descriptor == null) {
				descriptor = AnnotationBasedAnalyzerBeanDescriptor
						.create(analyzerClass);
				_analyzerBeanDescriptors.put(analyzerClass, descriptor);
			}
		}
		if (visitor.isTransformer()) {
			@SuppressWarnings("unchecked")
			Class<? extends Transformer<?>> transformerClass = (Class<? extends Transformer<?>>) visitor
					.getBeanClass();
			TransformerBeanDescriptor<?> descriptor = _transformerBeanDescriptors
					.get(transformerClass);
			if (descriptor == null) {
				descriptor = AnnotationBasedTransformerBeanDescriptor
						.create(transformerClass);
				_transformerBeanDescriptors.put(transformerClass, descriptor);
			}
		}
		if (visitor.isRenderer()) {
			@SuppressWarnings("unchecked")
			Class<? extends Renderer<?, ?>> rendererClass = (Class<? extends Renderer<?, ?>>) visitor
					.getBeanClass();
			RendererBeanDescriptor descriptor = _rendererBeanDescriptors
					.get(rendererClass);
			if (descriptor == null) {
				descriptor = new AnnotationBasedRendererBeanDescriptor(
						rendererClass);
				_rendererBeanDescriptors.put(rendererClass, descriptor);
			}
		}
	}

	@Override
	public Collection<AnalyzerBeanDescriptor<?>> getAnalyzerBeanDescriptors() {
		return Collections.unmodifiableCollection(_analyzerBeanDescriptors
				.values());
	}

	@Override
	public Collection<TransformerBeanDescriptor<?>> getTransformerBeanDescriptors() {
		return Collections.unmodifiableCollection(_transformerBeanDescriptors
				.values());
	}

	@Override
	public Collection<RendererBeanDescriptor> getRendererBeanDescriptors() {
		return Collections.unmodifiableCollection(_rendererBeanDescriptors
				.values());
	}
}
