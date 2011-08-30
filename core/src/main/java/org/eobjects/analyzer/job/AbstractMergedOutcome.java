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

import java.util.Arrays;
import java.util.List;

import org.eobjects.metamodel.util.BaseObject;

public abstract class AbstractMergedOutcome extends BaseObject implements MergedOutcome {

	@Override
	protected final boolean classEquals(BaseObject obj) {
		// should work with all subtypes
		return obj instanceof MergedOutcome;
	}

	@Override
	protected final void decorateIdentity(List<Object> identifiers) {
		identifiers.add(getMergedOutcomeJob());
	}

	@Override
	public String toString() {
		return "MergedOutcome[output=" + Arrays.toString(getMergedOutcomeJob().getOutput()) + "]";
	}
}
