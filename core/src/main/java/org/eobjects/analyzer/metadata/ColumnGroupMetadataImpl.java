package org.eobjects.analyzer.metadata;

import java.util.Collection;
import java.util.List;

import org.eobjects.metamodel.schema.Column;

import com.google.common.collect.ImmutableList;

/**
 * Default (immutable) implementation of {@link ColumnGroupMetadata}.
 */
public final class ColumnGroupMetadataImpl extends AbstractHasMetadataAnnotations implements ColumnGroupMetadata {

    private final String _columnGroupName;
    private final ImmutableList<ColumnMetadata> _columnMetadata;

    public ColumnGroupMetadataImpl(String columnGroupName, Collection<? extends ColumnMetadata> columnMetadata,
            Collection<? extends MetadataAnnotation> annotations) {
        super(annotations);
        _columnGroupName = columnGroupName;
        _columnMetadata = ImmutableList.copyOf(columnMetadata);
    }

    @Override
    public String getName() {
        return _columnGroupName;
    }

    @Override
    public ColumnMetadata getColumnMetadataByName(String columnName) {
        return getByName(columnName, _columnMetadata);
    }

    @Override
    public ColumnMetadata getColumnMetadata(Column column) {
        if (column == null) {
            return null;
        }
        final String columnName = column.getName();
        return getColumnMetadataByName(columnName);
    }

    @Override
    public List<ColumnMetadata> getColumnMetadata() {
        return _columnMetadata;
    }

}
