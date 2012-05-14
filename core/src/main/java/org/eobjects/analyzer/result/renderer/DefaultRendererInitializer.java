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
package org.eobjects.analyzer.result.renderer;

import java.lang.reflect.Field;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRendererInitializer implements RendererInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRendererInitializer.class);

    private final DescriptorProvider _descriptorProvider;

    public DefaultRendererInitializer(DescriptorProvider descriptorProvider) {
        _descriptorProvider = descriptorProvider;
    }

    @Override
    public void initialize(Renderer<?, ?> renderer) {
        Field[] injectFields = ReflectionUtils.getFields(renderer.getClass(), Inject.class);
        for (Field injectField : injectFields) {
            if (injectField.getType() == DescriptorProvider.class) {
                injectField.setAccessible(true);
                try {
                    injectField.set(renderer, _descriptorProvider);
                } catch (Exception e) {
                    logger.warn("Failed to inject RendererFactory into field: {}", injectField);
                }
            }
        }
    }

}
