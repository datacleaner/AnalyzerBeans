package org.eobjects.analyzer.result.html

/**
 * Head element which loads Google visualization (aka Google charts).
 */
object FlotChartHeadElement extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    
    return <script language="javascript" type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script language="javascript" type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/flot/0.7/jquery.flot.min.js"></script>
<script language="javascript" type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/flot/0.7/jquery.flot.pie.min.js"></script>.mkString("\n")
  }
}