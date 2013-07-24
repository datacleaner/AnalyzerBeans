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
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.util.ClassLoaderUtils;

import junit.framework.TestCase;

public class ClasspathScanDescriptorProviderTest extends TestCase {

    private MultiThreadedTaskRunner taskRunner = new MultiThreadedTaskRunner(2);

    public void testScanOnlySingleJar() throws Exception {
        // File that only contains the XML decoder transformer
        File pluginFile1 = new File("src/test/resources/plugin-only-xml-transformer.jar");
        // File that only contains the Datastore writer analyzer
        File pluginFile2 = new File("src/test/resources/plugin-only-datastore-writer.jar");

        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);
        assertEquals(0, provider.getAnalyzerBeanDescriptors().size());
        assertEquals(0, provider.getTransformerBeanDescriptors().size());
        File[] files = new File[] { pluginFile1, pluginFile2 };
        provider = provider.scanPackage("org.eobjects", true, ClassLoaderUtils.createClassLoader(files), false, files);
        assertEquals(1, provider.getAnalyzerBeanDescriptors().size());
        assertEquals(1, provider.getTransformerBeanDescriptors().size());

        assertEquals("org.eobjects.analyzer.beans.writers.WriteToDatastoreAnalyzer", provider
                .getAnalyzerBeanDescriptors().iterator().next().getComponentClass().getName());
        assertEquals("org.eobjects.analyzer.beans.transform.XmlDecoderTransformer", provider
                .getTransformerBeanDescriptors().iterator().next().getComponentClass().getName());
    }

    public void testScanNonExistingPackage() throws Exception {
        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);
        Collection<AnalyzerBeanDescriptor<?>> analyzerDescriptors = provider.scanPackage(
                "org.eobjects.analyzer.nonexistingbeans", true).getAnalyzerBeanDescriptors();
        assertEquals("[]", Arrays.toString(analyzerDescriptors.toArray()));

        assertEquals("[]", provider.getTransformerBeanDescriptors().toString());
        assertEquals("[]", provider.getRendererBeanDescriptors().toString());
    }

    public void testScanPackageRecursive() throws Exception {
        ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
        Collection<AnalyzerBeanDescriptor<?>> analyzerDescriptors = descriptorProvider.scanPackage(
                "org.eobjects.analyzer.beans.mock", true).getAnalyzerBeanDescriptors();
        Object[] array = analyzerDescriptors.toArray();
        assertEquals("[AnnotationBasedAnalyzerBeanDescriptor[org.eobjects.analyzer.beans.mock.AnalyzerMock]]",
                Arrays.toString(array));

        array = descriptorProvider.getExplorerBeanDescriptors().toArray();
        assertEquals("[AnnotationBasedExplorerBeanDescriptor[org.eobjects.analyzer.beans.mock.ExploringAnalyzerMock]]",
                Arrays.toString(array));

        Collection<TransformerBeanDescriptor<?>> transformerBeanDescriptors = descriptorProvider
                .getTransformerBeanDescriptors();
        assertEquals("[AnnotationBasedTransformerBeanDescriptor[org.eobjects.analyzer.beans.mock.TransformerMock]]",
                Arrays.toString(transformerBeanDescriptors.toArray()));

        analyzerDescriptors = new ClasspathScanDescriptorProvider(taskRunner).scanPackage("org.eobjects.analyzer.job.builder",
                true).getAnalyzerBeanDescriptors();
        assertEquals(0, analyzerDescriptors.size());
    }

    public void testScanRenderers() throws Exception {
        ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
        Collection<RendererBeanDescriptor<?>> rendererBeanDescriptors = descriptorProvider.scanPackage(
                "org.eobjects.analyzer.result.renderer", true).getRendererBeanDescriptors();
        assertEquals(
                "[AnnotationBasedRendererBeanDescriptor[org.eobjects.analyzer.result.renderer.CrosstabTextRenderer], "
                        + "AnnotationBasedRendererBeanDescriptor[org.eobjects.analyzer.result.renderer.DefaultTextRenderer]]",
                new TreeSet<RendererBeanDescriptor<?>>(rendererBeanDescriptors).toString());
    }
    
    public void testScanJarFilesOnClasspath() throws Exception {
     // File that only contains the XML decoder transformer
        File pluginFile1 = new File("src/test/resources/plugin-only-xml-transformer.jar");
        // File that only contains the Datastore writer analyzer
        File pluginFile2 = new File("src/test/resources/plugin-only-datastore-writer.jar");

        File[] files = new File[] { pluginFile1, pluginFile2 };
        ClassLoader classLoader = ClassLoaderUtils.createClassLoader(files);
        
        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);
        
        assertEquals(0, provider.getAnalyzerBeanDescriptors().size());
        assertEquals(0, provider.getTransformerBeanDescriptors().size());

        provider = provider.scanPackage("org.eobjects", true, classLoader, true);
        assertEquals(1, provider.getAnalyzerBeanDescriptors().size());
        assertEquals(1, provider.getTransformerBeanDescriptors().size());

        assertEquals("org.eobjects.analyzer.beans.writers.WriteToDatastoreAnalyzer", provider
                .getAnalyzerBeanDescriptors().iterator().next().getComponentClass().getName());
        assertEquals("org.eobjects.analyzer.beans.transform.XmlDecoderTransformer", provider
                .getTransformerBeanDescriptors().iterator().next().getComponentClass().getName());   
    }
    
    public void testIsClassInPackageNonRecursive() throws Exception {
        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);
        
        assertTrue(provider.isClassInPackage("foo/bar/Baz.class", "foo/bar", false));
        assertTrue(provider.isClassInPackage("foo/bar/Foobar.class", "foo/bar", false));
        
        assertFalse(provider.isClassInPackage("foo/bar/baz/Baz.class", "foo/bar", false));
        
        assertFalse(provider.isClassInPackage("foo/baz/Baz.class", "foo/bar", false));
        assertFalse(provider.isClassInPackage("foo/Baz.class", "foo/bar", false));
    }
    
    public void testIsClassInPackageRecursive() throws Exception {
        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);
        
        assertTrue(provider.isClassInPackage("foo/bar/Baz.class", "foo/bar", true));
        assertTrue(provider.isClassInPackage("foo/bar/Foobar.class", "foo/bar", true));
        
        assertTrue(provider.isClassInPackage("foo/bar/baz/Baz.class", "foo/bar", true));
        
        assertFalse(provider.isClassInPackage("foo/baz/Baz.class", "foo/bar", true));
        assertFalse(provider.isClassInPackage("foo/Baz.class", "foo/bar", true));
    }
}
