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

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;

/**
 * A simple execution servlet to deploy on a slave node
 */
public class SlaveServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SERVLET_CONTEXT_ATTRIBUTE_CONFIGURATION = "org.eobjects.analyzer.configuration";

    @Inject
    AnalyzerBeansConfiguration _configuration;

    public SlaveServlet() {
        this(null);
    }

    public SlaveServlet(AnalyzerBeansConfiguration configuration) {
        super();
        _configuration = configuration;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (_configuration == null) {
            final ServletContext servletContext = config.getServletContext();
            final Object attribute = servletContext.getAttribute(SERVLET_CONTEXT_ATTRIBUTE_CONFIGURATION);
            if (attribute != null && attribute instanceof AnalyzerBeansConfiguration) {
                _configuration = (AnalyzerBeansConfiguration) attribute;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final SlaveServletHelper helper = new SlaveServletHelper(_configuration);
        helper.handleRequest(req, resp);
    }
}
