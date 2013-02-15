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
package org.eobjects.analyzer.beans.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a method as a closing method. The method is invoked to
 * release resources that the object is holding (such as open files).
 * 
 * Components can either annotate methods with this annotation or implement the
 * java.io.Closeable interface to ensure that resources are released.
 * 
 * This annotation is to be understood as the opposite of the @Initialize
 * method.
 * 
 * @see Initialize
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Close {
    
    /**
     * Determines if the initialize method is to be executed in a distributed
     * fashion, potentially on multiple nodes in a cluster.
     * 
     * By default initialization methods will only initialize internal state of
     * the component, and is thus fully distributable. But if the initialization
     * touches outside resources, such as datastores, files or other, then the
     * initialization is typically NOT distributable, since timing of the
     * initializations accross the cluster isn't predictable.
     * 
     * @return true if this initialization method is distributable. In that case
     *         it will be invoked on all nodes of a cluster. If the value is
     *         false, the initialization method will ONLY be invoked on a single
     *         (master) node and all other nodes will not have this particular
     *         initialization method invoked.
     */
    public boolean distributed() default true;
}