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
package org.eobjects.analyzer.job;

import org.eobjects.analyzer.descriptors.DescriptorProvider;

/**
 * Exception thrown in case a job is being opened and it references an
 * unexisting component, such as an analyzer or transformer name which is not
 * resolved using the {@link DescriptorProvider}.
 * 
 * @author Kasper Sørensen
 */
public class NoSuchComponentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Class<?> _expectedComponentType;
    private final String _name;

    public NoSuchComponentException(Class<?> expectedComponentType, String name) {
        super();
        _expectedComponentType = expectedComponentType;
        _name = name;
    }

    @Override
    public String getMessage() {
        return "No such " + (_expectedComponentType == null ? "component" : _expectedComponentType.getSimpleName())
                + " descriptor: " + _name;
    }
}
