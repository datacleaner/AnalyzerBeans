package org.eobjects.analyzer.beans.valuedist
import org.eobjects.analyzer.result.html.HeadElement
import scala.collection.JavaConversions._
import org.eobjects.analyzer.result.html.HtmlUtils

class ValueDistributionChartScriptHeadElement(result: ValueDistributionGroupResult, chartElementId: String) extends HeadElement {

  def toHtml: String = {
    val valueCounts = result.getTopValues().getValueCounts() ++ result.getBottomValues().getValueCounts()
    if (result.getNullCount() > 0) {
      valueCounts.add(new ValueCount("<null>", result.getNullCount()));
    }
    if (result.getUniqueCount() > 0) {
      valueCounts.add(new ValueCount("<unique>", result.getUniqueCount()));
    }
    
    
    return """<script type="text/javascript">
                   google.setOnLoadCallback(function() {
                     var elem = document.getElementById("""" + chartElementId + """");
                     var options = {};
                     
                     var data = google.visualization.arrayToDataTable([
                         ['Value', 'Count'],""" +
      valueCounts.map(valueCountMapper).mkString(",") + """
                     ]);
                     
                     var chart = new google.visualization.PieChart(elem);
                     chart.draw(data, options);
                     
                   });
               </script>"""
  }

  def valueCountMapper(vc: ValueCount) = {
    "['" + HtmlUtils.escapeToJsonString(vc.getValue()) + "', " + vc.getCount() + "]";
  }
}