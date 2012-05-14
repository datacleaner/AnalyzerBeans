package org.eobjects.analyzer.result.html;

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

    public final String toJavaScriptInvocation() {
        return _functionName + "();return false;";
    }
}
