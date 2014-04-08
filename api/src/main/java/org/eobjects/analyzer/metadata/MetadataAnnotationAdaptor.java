package org.eobjects.analyzer.metadata;

/**
 * Component with responsibility to adapt a particular type of
 * {@link MetadataAnnotation} into a specialized class which is easier to
 * consume and use for specialized use-cases.
 * 
 * @param <T>
 *            the object type that will be converted to/from the
 *            {@link MetadataAnnotation}.
 */
public interface MetadataAnnotationAdaptor<T> {

    /**
     * Gets the name of the annotation that this adaptor will be adapting
     * to/from.
     * 
     * @return
     */
    public String getAnnotationName();

    /**
     * Converts a {@link MetadataAnnotation} object into a specialized object.
     * 
     * @param annotation
     * @return
     */
    public T convertFromAnnotation(MetadataAnnotation annotation);

    /**
     * Converts a specialized object back to a {@link MetadataAnnotation}
     * object.
     * 
     * @param object
     * @return
     */
    public MetadataAnnotation convertToAnnotation(T object);
}
