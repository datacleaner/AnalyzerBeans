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
package org.eobjects.analyzer.util.convert;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eobjects.analyzer.util.VFSUtils;
import org.eobjects.analyzer.util.VfsResource;
import org.eobjects.analyzer.util.convert.ResourceConverter.ResourceTypeHandler;
import org.eobjects.metamodel.util.FileResource;
import org.eobjects.metamodel.util.Resource;
import org.eobjects.metamodel.util.UrlResource;

import junit.framework.TestCase;

public class ResourceConverterTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals("url", ResourceConverter.parseStructure("url://foobar").getScheme());
        assertEquals("foobar", ResourceConverter.parseStructure("url://foobar").getPath());
        assertEquals("file", ResourceConverter.parseStructure("file://c:/blabla").getScheme());
        assertEquals("c:/blabla", ResourceConverter.parseStructure("file://c:/blabla").getPath());
        assertEquals("/c:/blabla", ResourceConverter.parseStructure("file:///c:/blabla").getPath());
    }

    public void testConvertFileResource() throws Exception {
        List<? extends ResourceTypeHandler<?>> handlers = Arrays.asList(new FileResourceTypeHandler());
        ResourceConverter converter = new ResourceConverter(handlers);

        FileResource resource1 = new FileResource("foo/bar.txt");

        String str = converter.toString(resource1);

        assertEquals("file://foo/bar.txt", str);

        Resource resource2 = converter.fromString(Resource.class, str);

        assertTrue(resource2 instanceof FileResource);
        assertEquals("foo/bar.txt", ((FileResource) resource2).getFile().getPath().replace('\\', '/'));
    }

    public void testConvertUrlResource() throws Exception {
        List<? extends ResourceTypeHandler<?>> handlers = Arrays.asList(new UrlResourceTypeHandler());
        ResourceConverter converter = new ResourceConverter(handlers);

        UrlResource resource1 = new UrlResource("http://localhost");

        String str = converter.toString(resource1);

        assertEquals("url://http://localhost", str);

        Resource resource2 = converter.fromString(Resource.class, str);

        assertTrue(resource2 instanceof UrlResource);
        assertEquals("localhost", resource2.getName());
    }

    public void testConvertVfsResource() throws Exception {
        List<? extends ResourceTypeHandler<?>> handlers = Arrays.asList(new VfsResourceTypeHandler());
        ResourceConverter converter = new ResourceConverter(handlers);

        VfsResource resource1 = new VfsResource(VFSUtils.getFileSystemManager().resolveFile("target"));

        String str = converter.toString(resource1);

        String absoluteFilePath = new File("target").getAbsolutePath().replaceAll("\\\\", "/");
        assertEquals("vfs://file:///" + absoluteFilePath, str);

        Resource resource2 = converter.fromString(Resource.class, str);

        assertTrue(resource2 instanceof VfsResource);
        assertEquals("target", resource2.getName());
    }
}
