package org.eobjects.analyzer.result.html
import org.junit.Test
import org.junit.Assert
import org.scalatest.junit.AssertionsForJUnit

class FlotChartHeadElementTest extends AssertionsForJUnit {

  @Test
  def testToHtml = {
    val html = FlotChartHeadElement.toHtml(new DefaultHtmlRenderingContext());
    
    Assert.assertEquals("""<script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/jquery/1.9.1/jquery.min.js" language="javascript"></script>""" + "\n" + """<script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/flot/0.7/jquery.flot.min.js" language="javascript"></script>""" + "\n" + """<script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/flot/0.7/jquery.flot.pie.min.js" language="javascript"></script>""".replaceAll("\r\n", "\n"), html.replaceAll("\r\n", "\n"));
  }

}