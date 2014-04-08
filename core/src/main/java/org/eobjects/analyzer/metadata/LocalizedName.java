package org.eobjects.analyzer.metadata;

import java.util.Locale;
import java.util.Map;

/**
 * Represents metadata about localized/I18N names of columns, tables etc.
 */
public final class LocalizedName {

    private final Map<String, String> _map;

    public LocalizedName(Map<String, String> parameters) {
        _map = parameters;
    }

    public String getDisplayName(String locale) {
        if (locale == null) {
            return null;
        }
        return _map.get(locale);
    }

    public String getDisplayName(String... locales) {
        if (locales == null) {
            return null;
        }
        for (String locale : locales) {
            String result = getDisplayName(locale);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public String getDisplayName(Locale locale) {
        if (locale == null) {
            return null;
        }
        final String localeString = locale.toString();
        final String language = locale.getLanguage();
        return getDisplayName(localeString, language);
    }

    public Map<String, String> getDisplayNamesAsMap() {
        return _map;
    }
}
