package org.eobjects.analyzer.metadata;

import java.util.List;

import org.eobjects.metamodel.schema.Column;

/**
 * Defines methods for objects that contain column metadata.
 * 
 * @see TableMetadata
 * @see ColumnGroupMetadata
 */
public interface HasColumnMetadata {

    /**
     * Gets {@link ColumnMetadata} for a particular {@link Column}.
     * 
     * @param columnName
     * @return a {@link ColumnMetadata} object, or null if no metadata about the
     *         column is available.
     */
    public ColumnMetadata getColumnMetadataByName(String columnName);

    /**
     * Gets {@link ColumnMetadata} for a particular {@link Column}.
     * 
     * @param columnName
     * @return a {@link ColumnMetadata} object, or null if no metadata about the
     *         column is available.
     */
    public ColumnMetadata getColumnMetadata(Column column);

    /**
     * Gets all available {@link ColumnMetadata} objects.
     * 
     * @return
     */
    public List<ColumnMetadata> getColumnMetadata();
}
