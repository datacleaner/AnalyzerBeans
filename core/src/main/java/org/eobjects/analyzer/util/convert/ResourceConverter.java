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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eobjects.analyzer.beans.api.Converter;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.metamodel.util.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A converter for {@link Resource}s. Because of different {@link Resource}
 * implementations, this converter delegates to a number of 'handlers' which
 * implement part of the conversion for a specific type of resource.
 */
public class ResourceConverter implements Converter<Resource> {

    private static final Logger logger = LoggerFactory.getLogger(ResourceConverter.class);

    private static final Pattern RESOURCE_PATTERN = Pattern.compile("\\b([a-zA-Z]+)://(.+)");
    private static final String DEFAULT_SCHEME = "file";

    /**
     * Represents a component capable of handling the parsing and serializing of
     * a single type of resource.
     */
    public static interface ResourceTypeHandler<E extends Resource> {
        public boolean isParserFor(Class<? extends Resource> resourceType);

        public String getScheme();

        public E parsePath(String path);

        public String createPath(Resource resource);
    }

    /**
     * Represents the parsed structure of a serialized resource
     */
    public static class ResourceStructure {

        private final String scheme;
        private final String path;

        public ResourceStructure(String scheme, String path) {
            this.scheme = scheme;
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public String getScheme() {
            return scheme;
        }
    }

    private final Map<String, ResourceTypeHandler<?>> _parsers;

    /**
     * Constructs a {@link ResourceConverter} using a set of parsers.
     * 
     * @param handlers
     */
    public ResourceConverter(Collection<? extends ResourceTypeHandler<?>> handlers) {
        _parsers = new ConcurrentHashMap<String, ResourceConverter.ResourceTypeHandler<?>>();
        for (ResourceTypeHandler<?> handler : handlers) {
            String scheme = handler.getScheme();
            _parsers.put(scheme, handler);
        }
    }

    @Override
    public Resource fromString(Class<?> type, String serializedForm) {
        final ResourceStructure structure = parseStructure(serializedForm);
        if (structure == null) {
            throw new IllegalStateException("Invalid resource format: " + serializedForm);
        }
        final String scheme = structure.getScheme();
        final ResourceTypeHandler<?> handler = _parsers.get(scheme);
        if (handler == null) {
            throw new IllegalStateException("No handler found for scheme of resource: " + serializedForm);
        }
        final Resource resource = handler.parsePath(structure.getPath());
        return resource;
    }

    @Override
    public String toString(Resource resource) {
        final Class<? extends Resource> resourceType = resource.getClass();
        final Collection<ResourceTypeHandler<?>> values = _parsers.values();
        for (ResourceTypeHandler<?> handler : values) {
            if (handler.isParserFor(resourceType)) {
                final String path = handler.createPath(resource);
                final String scheme = handler.getScheme();
                return scheme + "://" + path;
            }
        }
        throw new IllegalStateException("Could not find a resource handler for resource: " + resource);
    }

    @Override
    public boolean isConvertable(Class<?> type) {
        return ReflectionUtils.is(type, Resource.class);
    }

    /**
     * Parses a string in order to produce a {@link ResourceStructure} object
     * 
     * @param str
     * @return
     */
    public static ResourceStructure parseStructure(String str) {
        Matcher matcher = RESOURCE_PATTERN.matcher(str);
        if (!matcher.find()) {
            logger.info("Did not find any scheme definition in resource path: {}. Assuming this is a file path.", str);
            return new ResourceStructure(DEFAULT_SCHEME, str);
        }
        String scheme = matcher.group(1);
        String path = matcher.group(2);
        return new ResourceStructure(scheme, path);
    }
}
