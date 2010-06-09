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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.ClassReader;

public class ClasspathScanDescriptorProvider implements DescriptorProvider {

	protected final Log _log = LogFactory.getLog(getClass());
	private Map<Class<?>, AnalyzerBeanDescriptor> _descriptors = new HashMap<Class<?>, AnalyzerBeanDescriptor>();

	public List<AnalyzerBeanDescriptor> scanPackage(String packageName,
			boolean recursive) {
		List<AnalyzerBeanDescriptor> analyzerDescriptors = new LinkedList<AnalyzerBeanDescriptor>();

		String packagePath = packageName.replace('.', '/');
		try {
			Enumeration<URL> resources = ClassLoader
					.getSystemResources(packagePath);
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				File dir = new File(resource.getFile());
				analyzerDescriptors.addAll(scanDirectory(dir, recursive));
			}
		} catch (IOException e) {
			_log.error(e);
		}

		return analyzerDescriptors;
	}

	private List<AnalyzerBeanDescriptor> scanDirectory(File dir,
			boolean recursive) {
		if (!dir.exists()) {
			throw new IllegalArgumentException("Directory '" + dir
					+ "' does not exist");
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("The file '" + dir
					+ "' is not a directory");
		}
		_log.info("Scanning directory: " + dir);

		List<AnalyzerBeanDescriptor> analyzerDescriptors = new LinkedList<AnalyzerBeanDescriptor>();

		File[] classFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String filename) {
				return filename.endsWith(".class");
			}
		});

		for (File file : classFiles) {
			try {
				AnalyzerBeansClassVisitor visitor = new AnalyzerBeansClassVisitor();
				ClassReader classReader = new ClassReader(new FileInputStream(
						file));
				classReader.accept(visitor, ClassReader.SKIP_CODE);

				if (visitor.isAnalyzer()) {
					Class<?> analyzerClass = visitor.getAnalyzerClass();
					AnalyzerBeanDescriptor descriptor = _descriptors
							.get(analyzerClass);
					if (descriptor == null) {
						descriptor = new AnalyzerBeanDescriptor(analyzerClass);
						_descriptors.put(analyzerClass, descriptor);
					}
					analyzerDescriptors.add(descriptor);
				}
			} catch (IOException e) {
				_log.error(e);
			}
		}

		if (recursive) {
			File[] subDirectories = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isDirectory();
				}
			});
			if (_log.isInfoEnabled() && subDirectories.length > 0) {
				_log.info("Recursively scanning " + subDirectories.length
						+ " subdirectories");
			}
			for (File subDir : subDirectories) {
				analyzerDescriptors.addAll(scanDirectory(subDir, true));
			}
		}
		return analyzerDescriptors;
	}

	@Override
	public Collection<AnalyzerBeanDescriptor> getDescriptors() {
		return _descriptors.values();
	}

	public void putDescriptor(Class<?> analyzerClass,
			AnalyzerBeanDescriptor descriptor) {
		_descriptors.put(analyzerClass, descriptor);
	}

	@Override
	public AnalyzerBeanDescriptor getDescriptorForClass(
			Class<?> analyzerBeanClass) {
		return _descriptors.get(analyzerBeanClass);
	}
}
