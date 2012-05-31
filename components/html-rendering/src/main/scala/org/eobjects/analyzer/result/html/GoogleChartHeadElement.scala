package org.eobjects.analyzer.result.html

/**
 * Head element which loads Google visualization (aka Google charts).
 */
object GoogleChartHeadElement extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    return <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      <!--
                      google.load("visualization", "1", {packages:["corechart"]});
                  -->
    </script>.mkString("\n")
  }
}