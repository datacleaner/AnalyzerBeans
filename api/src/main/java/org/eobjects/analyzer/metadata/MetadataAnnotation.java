package org.eobjects.analyzer.metadata;

import java.util.Map;

import org.eobjects.metamodel.util.HasName;

/**
 * Represents an annotation of a metadata element such as a
 * {@link TableMetadata}, {@link ColumnGroupMetadata} or {@link ColumnMetadata}.
 * An annotation is used to
 */
public interface MetadataAnnotation extends HasName {

    /**
     * Gets the name of the annotation.
     */
    @Override
    public String getName();

    /**
     * Gets any parameters set on the annotation. Parameters may be used to
     * specify further details and behaviour for the annotations.
     * 
     * @return
     */
    public Map<String, String> getParameters();
}
