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

/**
 * Head element which generates a javascript element in the head.
 */
public abstract class AbstractScriptHeadElement implements HeadElement {

    private final String _functionName;

    public AbstractScriptHeadElement() {
        _functionName = HtmlUtils.createFunctionName();
    }

    @Override
    public String toHtml() {
        JavascriptFunctionBuilder fb = new JavascriptFunctionBuilder(_functionName);
        buildFunction(fb);
        return fb.toHeadElementHtml();
    }

    protected abstract void buildFunction(JavascriptFunctionBuilder fb);

    /**
     * Returns a piece of executable JavaScript, usable to insert in an onclick
     * event handler or similar. Executing the JavaScript will fire the script
     * that was built.
     * 
     * @return
     */
    public final String toJavaScriptInvocation() {
        return _functionName + "();return false;";
    }
}
