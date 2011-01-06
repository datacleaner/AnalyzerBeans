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
 * Annotation that marks a no-arg method as an initializing method. Use this
 * annotation if you need to initialize the state of a bean before it starts
 * executing a job.
 * 
 * The @Initialize annotation can be used on methods in the following component
 * types:
 * 
 * <ul>
 * <li>AnalyzerBeans</li>
 * <li>TransformerBeans</li>
 * <li>FilterBeans</li>
 * <li>Dictionaries</li>
 * <li>SynonymCatalog</li>
 * <li>StringPattern</li>
 * <li>... and custom configuration elements, such as custom datastores and
 * custom task runners (in which case the semantics are a bit different - they
 * will only be initialized once, just after loading the configuration).</li>
 * </ul>
 * 
 * The method is invoked after any @Configured and @Provided methods/fields are
 * invoked/assigned but before any business methods (such as run(...) on an
 * analyzer) are invoked.
 * 
 * @Initialize is often used in conjunction with the @Close annotation.
 * 
 * @see Close
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Initialize {
}