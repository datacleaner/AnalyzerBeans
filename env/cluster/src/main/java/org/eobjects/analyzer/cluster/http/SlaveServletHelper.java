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
package org.eobjects.analyzer.cluster.http;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SerializationUtils;
import org.eobjects.analyzer.cluster.SlaveAnalysisRunner;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper method for handling servlet requests and responses according to the
 * requests sent by the master node if it is using a {@link HttpClusterManager}.
 */
public class SlaveServletHelper {

    private static final Logger logger = LoggerFactory.getLogger(SlaveServletHelper.class);

    private final AnalyzerBeansConfiguration _configuration;

    public SlaveServletHelper(AnalyzerBeansConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("AnalyzerBeansConfiguration cannot be null");
        }
        _configuration = configuration;
    }

    /**
     * Completely handles a HTTP request and response. This method is
     * functionally equivalent of calling these methods in sequence:
     * 
     * {@link #readJob(HttpServletRequest)}
     * 
     * {@link #runJob(AnalysisJob)}
     * 
     * {@link #serializeResult(AnalysisResultFuture)}
     * 
     * {@link #sendResponse(HttpServletResponse, Serializable)}
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    public void handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        AnalysisJob job;
        try {
            job = readJob(request);
        } catch (IOException e) {
            logger.error("Failed to read job definition from HTTP request", e);
            throw e;
        }

        final Serializable resultObject;
        try {
            final AnalysisResultFuture resultFuture = runJob(job);
            resultObject = serializeResult(resultFuture);
        } catch (RuntimeException e) {
            logger.error("Unexpected error occurred while running slave job", e);
            throw e;
        }

        try {
            sendResponse(response, resultObject);
        } catch (IOException e) {
            logger.error("Failed to send job result through HTTP response", e);
            throw e;
        }
    }

    public Serializable serializeResult(AnalysisResultFuture resultFuture) {
        // wait for result to be ready
        resultFuture.await();

        final Serializable resultObject;
        if (resultFuture.isSuccessful()) {
            resultObject = new SimpleAnalysisResult(resultFuture.getResultMap());
        } else {
            resultObject = new ArrayList<Throwable>(resultFuture.getErrors());
        }
        return resultObject;
    }

    public AnalysisJob readJob(HttpServletRequest request) throws IOException {
        final JaxbJobReader reader = new JaxbJobReader(_configuration);
        final ServletInputStream inputStream = request.getInputStream();
        try {
            final AnalysisJob job = reader.read(inputStream);
            return job;
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    public AnalysisResultFuture runJob(AnalysisJob job) {
        final AnalysisRunner runner = new SlaveAnalysisRunner(_configuration);
        final AnalysisResultFuture resultFuture = runner.run(job);
        return resultFuture;
    }

    public void sendResponse(HttpServletResponse response, Serializable object) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        try {
            SerializationUtils.serialize(object, outputStream);
        } finally {
            outputStream.flush();
        }
    }
}
