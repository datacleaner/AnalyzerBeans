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

import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.convert.ResourceConverter.ResourceTypeHandler;
import org.eobjects.metamodel.util.FileResource;
import org.eobjects.metamodel.util.Resource;

/**
 * {@link ResourceTypeHandler} for {@link FileResource}s.
 */
public class FileResourceTypeHandler implements ResourceTypeHandler<FileResource> {

    @Override
    public boolean isParserFor(Class<? extends Resource> resourceType) {
        return ReflectionUtils.is(resourceType, FileResource.class);
    }

    @Override
    public String getScheme() {
        return "file";
    }

    @Override
    public FileResource parsePath(String path) {
        File file = new File(path);
        return new FileResource(file);
    }

    @Override
    public String createPath(Resource resource) {
        String path = ((FileResource) resource).getFile().getPath();
        path = path.replaceAll("\\\\", "/");
        return path;
    }

}
