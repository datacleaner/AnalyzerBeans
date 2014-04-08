package org.eobjects.analyzer.metadata;

import java.util.Collection;
import java.util.List;

import org.eobjects.metamodel.schema.Column;

import com.google.common.collect.ImmutableList;

/**
 * Default (immutable) implementation of {@link TableMetadata}.
 */
public final class TableMetadataImpl extends AbstractHasMetadataAnnotations implements TableMetadata {

    private final String _tableName;
    private final ImmutableList<ColumnMetadata> _columnMetadata;
    private final ImmutableList<ColumnGroupMetadata> _columnGroupMetadata;

    public TableMetadataImpl(String tableName, Collection<? extends ColumnMetadata> columnMetadata,
            Collection<? extends ColumnGroupMetadata> columnGroupMetadata,
            Collection<? extends MetadataAnnotation> annotations) {
        super(annotations);
        _tableName = tableName;
        _columnMetadata = ImmutableList.copyOf(columnMetadata);
        _columnGroupMetadata = ImmutableList.copyOf(columnGroupMetadata);
    }

    @Override
    public String getName() {
        return _tableName;
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

    @Override
    public ColumnGroupMetadata getColumnGroupMetadataByName(String groupName) {
        return getByName(groupName, _columnGroupMetadata);
    }

    @Override
    public List<ColumnGroupMetadata> getColumnGroupMetadata() {
        return _columnGroupMetadata;
    }

}
