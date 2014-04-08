package org.eobjects.analyzer.metadata;

import java.util.List;

import org.eobjects.metamodel.schema.Table;

/**
 * Defines metadata about a {@link Table}.
 */
public interface TableMetadata extends HasMetadataAnnotations, HasColumnMetadata {

    /**
     * Gets {@link ColumnGroupMetadata} by the name of the group.
     * 
     * @param groupName
     * @return a {@link ColumnGroupMetadata} object, or null if no group
     *         matching the name was found
     */
    public ColumnGroupMetadata getColumnGroupMetadataByName(String groupName);

    /**
     * Gets all {@link ColumnGroupMetadata} objects available.
     * 
     * @return
     */
    public List<ColumnGroupMetadata> getColumnGroupMetadata();
}
