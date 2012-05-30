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
package org.eobjects.analyzer.result.html;

public final class BaseHeadElement implements HeadElement {

    private static BaseHeadElement _singleton = new BaseHeadElement();

    public static BaseHeadElement get() {
        return _singleton;
    }

    private BaseHeadElement() {
        // prevent instantiation
    }

    @Override
    public String toHtml() {
        return "<script type=\"text/javascript\" src=\"http://code.jquery.com/jquery-1.7.2.min.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"http://code.jquery.com/ui/1.8.20/jquery-ui.min.js\"></script>\n"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://code.jquery.com/ui/1.8.20/themes/base/jquery-ui.css\" />\n"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://eobjects.org/resources/datacleaner-html-rendering/analysis-result.css\" />\n"
                + "<link rel=\"shortcut icon\" href=\"http://eobjects.org/resources/datacleaner-html-rendering/linechart-icon.png\" />"
                + "<script type=\"text/javascript\">\n" + "  var analysisResult = {};\n" + "</script>";
    }

}
