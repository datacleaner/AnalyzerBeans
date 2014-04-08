package org.eobjects.analyzer.metadata;

import java.util.List;

/**
 * Defines methods for objects that contain {@link MetadataAnnotation}s.
 */
public interface HasMetadataAnnotations {

    /**
     * Gets a {@link MetadataAnnotation} by name.
     * 
     * @param annotationName
     * @return the annotation, or null if no annotation with the particular name
     *         was found.
     */
    public MetadataAnnotation getAnnotation(String annotationName);

    /**
     * Adapts a particular annotation into a specialized object using a
     * {@link MetadataAnnotationAdaptor}.
     * 
     * @param annotationAdaptor
     * @return the specialized object, or null if no applicable annotation to
     *         adapt was found
     */
    public <M> M getAdaptedAnnotation(MetadataAnnotationAdaptor<M> annotationAdaptor);

    /**
     * Gets all available {@link MetadataAnnotation}.
     * 
     * @return
     */
    public List<MetadataAnnotation> getAnnotations();
}
