package org.eobjects.analyzer.descriptors;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.Analyzer;
import org.eobjects.analyzer.beans.Transformer;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClasspathScanDescriptorProvider implements
		DescriptorProvider {

	private static final Logger logger = LoggerFactory
			.getLogger(ClasspathScanDescriptorProvider.class);
	private Map<Class<? extends Analyzer>, AnalyzerBeanDescriptor> _analyzerBeanDescriptors = new HashMap<Class<? extends Analyzer>, AnalyzerBeanDescriptor>();
	private Map<Class<? extends Transformer<?>>, TransformerBeanDescriptor> _transformerBeanDescriptors = new HashMap<Class<? extends Transformer<?>>, TransformerBeanDescriptor>();

	public ClasspathScanDescriptorProvider scanPackage(String packageName,
			boolean recursive) {
		String packagePath = packageName.replace('.', '/');
		try {
			Enumeration<URL> resources = ClassLoader
					.getSystemResources(packagePath);
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				File dir = new File(resource.getFile());
				scanDirectory(dir, recursive);
			}
		} catch (IOException e) {
			logger.error("Could not open classpath resource", e);
		}

		return this;
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
				BeanClassVisitor visitor = new BeanClassVisitor();
				ClassReader classReader = new ClassReader(new FileInputStream(
						file));
				classReader.accept(visitor, ClassReader.SKIP_CODE);

				if (visitor.isAnalyzer()) {
					@SuppressWarnings("unchecked")
					Class<? extends Analyzer> analyzerClass = (Class<? extends Analyzer>) visitor
							.getBeanClass();
					AnalyzerBeanDescriptor descriptor = _analyzerBeanDescriptors
							.get(analyzerClass);
					if (descriptor == null) {
						descriptor = new AnalyzerBeanDescriptor(analyzerClass);
						_analyzerBeanDescriptors.put(analyzerClass, descriptor);
					}
				}
				if (visitor.isTransformer()) {
					@SuppressWarnings("unchecked")
					Class<? extends Transformer<?>> transformerClass = (Class<? extends Transformer<?>>) visitor
							.getBeanClass();
					TransformerBeanDescriptor descriptor = _transformerBeanDescriptors
							.get(transformerClass);
					if (descriptor == null) {
						descriptor = new TransformerBeanDescriptor(
								transformerClass);
						_transformerBeanDescriptors.put(transformerClass,
								descriptor);
					}
				}
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

	@Override
	public Collection<AnalyzerBeanDescriptor> getAnalyzerBeanDescriptors() {
		return _analyzerBeanDescriptors.values();
	}

	@Override
	public AnalyzerBeanDescriptor getAnalyzerBeanDescriptorForClass(
			Class<? extends Analyzer> analyzerBeanClass) {
		return _analyzerBeanDescriptors.get(analyzerBeanClass);
	}

	@Override
	public Collection<TransformerBeanDescriptor> getTransformerBeanDescriptors() {
		return _transformerBeanDescriptors.values();
	}

	@Override
	public TransformerBeanDescriptor getTransformerBeanDescriptorForClass(
			Class<? extends Transformer<?>> transformerBeanClass) {
		return _transformerBeanDescriptors.get(transformerBeanClass);
	}
}
