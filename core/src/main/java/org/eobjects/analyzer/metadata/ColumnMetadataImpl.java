package org.eobjects.analyzer.metadata;

import java.util.Collection;

/**
 * Default (immutable) implementation of {@link ColumnMetadata}
 */
public class ColumnMetadataImpl extends AbstractHasMetadataAnnotations implements ColumnMetadata {

    private final String _columnName;

    public ColumnMetadataImpl(String columnName, Collection<? extends MetadataAnnotation> annotations) {
        super(annotations);
        _columnName = columnName;
    }

    @Override
    public String getName() {
        return _columnName;
    }

}
