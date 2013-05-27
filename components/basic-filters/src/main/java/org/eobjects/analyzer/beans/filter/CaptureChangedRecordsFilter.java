/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.beans.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.beans.api.FileProperty;
import org.eobjects.analyzer.beans.api.FileProperty.FileAccessMode;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.categories.DateAndTimeCategory;
import org.eobjects.analyzer.beans.categories.FilterCategory;
import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter for archieving a "change data capture" mechanism based on a
 * "last modified" field. After each execution, the greatest timestamp is
 * recorded and picked up successively by the next run.
 */
@FilterBean("Capture changed records")
@Description("Include only records that have changed since the last time you ran the job. This filter assumes a field containing the timestamp of the latest change for each record, and stores the greatest encountered value in order to update the filter's future state.")
@Distributed(false)
@Categorized({ FilterCategory.class, DateAndTimeCategory.class })
public class CaptureChangedRecordsFilter implements Filter<ValidationCategory> {

    private static final Logger logger = LoggerFactory.getLogger(CaptureChangedRecordsFilter.class);

    @Configured
    @Description("Column containing the last modification timestamp or date.")
    InputColumn<Object> lastModifiedColumn;

    @Configured
    @Description("A file used to persist and load the latest state of this data capture component.")
    @FileProperty(extension = "properties", accessMode = FileAccessMode.SAVE)
    File captureStateFile;

    private Date _lastModifiedThreshold;
    private Date _greatestEncounteredDate;

    @Initialize
    public void initialize() throws IOException {
        final Properties properties = loadProperties();
        final String key = getPropertyKey();
        final String lastModified = properties.getProperty(key);
        final Date date = ConvertToDateTransformer.getInternalInstance().transformValue(lastModified);
        _lastModifiedThreshold = date;
    }

    @Close
    public void close() throws IOException {
        if (_greatestEncounteredDate != null) {
            final Properties properties = loadProperties();
            final String key = getPropertyKey();
            properties.setProperty(key, "" + _greatestEncounteredDate.getTime());
            final Writer writer = FileHelper.getBufferedWriter(captureStateFile);
            try {
                properties.store(writer, null);
            } finally {
                FileHelper.safeClose(writer);
            }
        }
    }

    /**
     * Gets the key to use in the capture state file. If possible we want to
     * avoid using a hardcoded key, since the same file may be used for multiple
     * purposes, even multiple filters of the same type. Of course this is not
     * desired configuration, but may be more convenient for lazy users!
     * 
     * @return
     */
    private String getPropertyKey() {
        if (lastModifiedColumn.isPhysicalColumn()) {
            Table table = lastModifiedColumn.getPhysicalColumn().getTable();
            if (table != null && !StringUtils.isNullOrEmpty(table.getName())) {
                return table.getName() + "." + lastModifiedColumn.getName() + ".GreatestLastModifiedTimestamp";
            }
        }
        return lastModifiedColumn.getName() + ".GreatestLastModifiedTimestamp";
    }

    private Properties loadProperties() throws IOException {
        final Properties properties = new Properties();
        if (!captureStateFile.exists() || !captureStateFile.isFile()) {
            logger.info("Capture state file does not exist: {}", captureStateFile);
            return properties;
        }
        final BufferedReader reader = FileHelper.getBufferedReader(captureStateFile);
        try {
            properties.load(reader);
        } finally {
            FileHelper.safeClose(reader);
        }
        return properties;
    }

    @Override
    public ValidationCategory categorize(InputRow inputRow) {
        final Object lastModified = inputRow.getValue(lastModifiedColumn);
        final Date date = ConvertToDateTransformer.getInternalInstance().transformValue(lastModified);
        
        if (date != null) {
            synchronized (this) {
                if (_greatestEncounteredDate == null || _greatestEncounteredDate.before(date)) {
                    _greatestEncounteredDate = date;
                }
            }
        }
        
        if (_lastModifiedThreshold == null) {
            return ValidationCategory.VALID;
        }

        if (date == null) {
            logger.info("Date value of {} was null, returning INVALID category: {}", lastModifiedColumn.getName(),
                    inputRow);
            return ValidationCategory.INVALID;
        }

        if (_lastModifiedThreshold.before(date)) {
            return ValidationCategory.VALID;
        }
        return ValidationCategory.INVALID;
    }
}
