package org.eobjects.analyzer.result.html
import org.junit.Test
import org.junit.Assert
import org.scalatest.junit.AssertionsForJUnit

class GoogleChartHeadElementTest extends AssertionsForJUnit {

  @Test
  def testToHtml = {
    val html = GoogleChartHeadElement.toHtml(new DefaultHtmlRenderingContext());
    Assert.assertEquals("""<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript"><!--
      google.load("visualization", "1", {packages:["corechart"]});
    --></script>""".replaceAll("\r\n", "\n"), html.replaceAll("\r\n", "\n"));
  }

}