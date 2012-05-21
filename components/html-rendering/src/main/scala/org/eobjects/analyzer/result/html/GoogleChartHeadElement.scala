package org.eobjects.analyzer.result.html

object GoogleChartHeadElement extends HeadElement {

  def toHtml: String = {
    return <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      <!--
                      google.load("visualization", "1", {packages:["corechart"]});
                  -->
    </script>.mkString("\n")
  }
}