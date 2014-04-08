package org.eobjects.analyzer.metadata;

import java.util.List;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.metamodel.schema.Schema;

/**
 * Defines metadata about a {@link Datastore}.
 */
public interface DatastoreMetadata extends HasMetadataAnnotations {

    /**
     * Gets {@link SchemaMetadata} for a particular schema
     * 
     * @param schemaName
     * @return a {@link SchemaMetadata} object, or null if no metadata is
     *         defined about the schema
     */
    public SchemaMetadata getSchemaMetadataByName(String schemaName);

    /**
     * Gets {@link SchemaMetadata} for a particular schema
     * 
     * @param schema
     * @return a {@link SchemaMetadata} object, or null if no metadata is
     *         defined about the schema
     */
    public SchemaMetadata getSchemaMetadata(Schema schema);

    /**
     * Gets all available {@link SchemaMetadata} objects.
     * 
     * @return
     */
    public List<SchemaMetadata> getSchemaMetadata();
}
