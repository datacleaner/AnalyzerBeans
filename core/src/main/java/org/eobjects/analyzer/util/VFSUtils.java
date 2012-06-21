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
import java.lang.reflect.Method;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.local.LocalFile;

public class VFSUtils {

    /**
     * Converts (if possible) a {@link FileObject} to a {@link File}. Use with
     * caution since {@link FileObject} is generally preferred.
     * 
     * @param fileObject
     * @return
     */
    public static File toFile(FileObject fileObject) {
        if (fileObject instanceof LocalFile) {
            Method method = ReflectionUtils.getMethod(LocalFile.class, "getLocalFile");
            try {
                method.setAccessible(true);
                Object result = method.invoke(fileObject);
                return (File) result;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }
}
