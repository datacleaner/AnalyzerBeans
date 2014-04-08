package org.eobjects.analyzer.metadata;

import org.eobjects.metamodel.schema.Column;

/**
 * Defines metadata about a group of {@link Column}s that logically belong
 * together, e.g. all columns pertaining to an address, a name or something like
 * that.
 */
public interface ColumnGroupMetadata extends HasMetadataAnnotations, HasColumnMetadata {

}
