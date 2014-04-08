package org.eobjects.analyzer.metadata;

import java.util.Map;

/**
 * {@link MetadataAnnotationAdaptor} for {@link LocalizedName}.
 */
public class LocalizedNameAnnotationAdaptor implements MetadataAnnotationAdaptor<LocalizedName> {

    @Override
    public String getAnnotationName() {
        return "LocalizedName";
    }

    @Override
    public LocalizedName convertFromAnnotation(MetadataAnnotation annotation) {
        final Map<String, String> parameters = annotation.getParameters();
        return new LocalizedName(parameters);
    }

    @Override
    public MetadataAnnotation convertToAnnotation(LocalizedName object) {
        final Map<String, String> map = object.getDisplayNamesAsMap();
        return new MetadataAnnotationImpl("LocalizedName", map);
    }

}
