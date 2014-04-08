package org.eobjects.analyzer.metadata;

import java.util.Collection;
import java.util.List;

import org.eobjects.metamodel.schema.Table;

import com.google.common.collect.ImmutableList;

/**
 * Default (immutable) implementation of {@link SchemaMetadata}.
 */
public final class SchemaMetadataImpl extends AbstractHasMetadataAnnotations implements SchemaMetadata {

    private final ImmutableList<TableMetadata> _tableMetadata;
    private final String _schemaName;

    public SchemaMetadataImpl(String schemaName, Collection<? extends TableMetadata> tableMetadata,
            Collection<? extends MetadataAnnotation> annotations) {
        super(annotations);
        _schemaName = schemaName;
        _tableMetadata = ImmutableList.copyOf(tableMetadata);
    }

    @Override
    public String getName() {
        return _schemaName;
    }

    @Override
    public TableMetadata getTableMetadataByName(String tableName) {
        return getByName(tableName, _tableMetadata);
    }

    @Override
    public TableMetadata getTableMetadata(Table table) {
        if (table == null) {
            return null;
        }
        final String tableName = table.getName();
        return getTableMetadataByName(tableName);
    }

    @Override
    public List<TableMetadata> getTableMetadata() {
        return _tableMetadata;
    }

}
