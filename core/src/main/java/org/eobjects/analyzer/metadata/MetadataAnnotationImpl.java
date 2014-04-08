package org.eobjects.analyzer.metadata;

import java.io.Serializable;
import java.util.Map;

/**
 * Default implementation of {@link MetadataAnnotation}.
 */
public final class MetadataAnnotationImpl implements MetadataAnnotation, Serializable {

    private static final long serialVersionUID = 1L;

    private final String _name;
    private final Map<String, String> _parameters;

    public MetadataAnnotationImpl(String name, Map<String, String> parameters) {
        _name = name;
        _parameters = parameters;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public Map<String, String> getParameters() {
        return _parameters;
    }

}
