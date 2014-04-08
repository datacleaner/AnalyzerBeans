package org.eobjects.analyzer.metadata;

import java.util.List;

import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;

/**
 * Defines metadata about a {@link Schema}.
 */
public interface SchemaMetadata extends HasMetadataAnnotations {

    /**
     * Gets {@link TableMetadata} about a particular {@link Table}.
     * 
     * @param tableName
     * @return a {@link TableMetadata} object, or null if no metadata is defined
     *         about the table
     */
    public TableMetadata getTableMetadataByName(String tableName);

    /**
     * Gets {@link TableMetadata} about a particular {@link Table}.
     * 
     * @param table
     * @return a {@link TableMetadata} object, or null if no metadata is defined
     *         about the table
     */
    public TableMetadata getTableMetadata(Table table);

    /**
     * Gets all available {@link TableMetadata} objects.
     * 
     * @return
     */
    public List<TableMetadata> getTableMetadata();
}
