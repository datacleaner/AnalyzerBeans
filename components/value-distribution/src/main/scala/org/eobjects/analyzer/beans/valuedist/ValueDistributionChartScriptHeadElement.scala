package org.eobjects.analyzer.beans.valuedist
import scala.collection.JavaConversions.collectionAsScalaIterable
import org.eobjects.analyzer.result.html.HeadElement
import org.eobjects.analyzer.result.html.HtmlRenderingContext
import org.eobjects.analyzer.result.ValueCountingAnalyzerResult
import org.eobjects.analyzer.util.LabelUtils
import org.eobjects.analyzer.result.ValueCount

class ValueDistributionChartScriptHeadElement(result: ValueCountingAnalyzerResult, chartElementId: String) extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    val valueCounts = result.getValueCounts();
    
    val unexpectedValueCount = result.getUnexpectedValueCount()
    if (unexpectedValueCount != null && unexpectedValueCount > 0) {
      valueCounts.add(new ValueCount(LabelUtils.UNEXPECTED_LABEL, unexpectedValueCount));
    }
    
    val uniqueCount = result.getUniqueCount();
    if (uniqueCount != null && uniqueCount > 0) {
      val vc = new ValueCount(LabelUtils.UNIQUE_LABEL, uniqueCount);
      valueCounts.add(vc);
    }

    return """<script type="text/javascript"><!--
   google.setOnLoadCallback(function() {
     var elem = document.getElementById("""" + chartElementId + """");
     var data = google.visualization.arrayToDataTable([
     ['Value', 'Count'],""" +
      valueCounts.map(vc => {
        "['" + context.escapeJson(LabelUtils.getValueLabel(vc.getValue())) + "', " + vc.getCount() + "]";
      }).mkString(",") + """
     ]);
     
     var chart = new google.visualization.PieChart(elem);
     var drawFunction = function(w, h) {
       var options = {"width": w, "height": h};
       chart.draw(data, options);
     };
     
     wait_for_script_load('jQuery', function() {
       var interval = setInterval(function() {
         var bodyWidth = $('body').width();
         var w = $(elem).width();
         if (bodyWidth - w > 4) {
           // css has been loaded
           var h = $(elem).height();
           clearInterval(interval);
           drawFunction(w,h);
         }
       }, 100);
     });
   });
--></script>"""
  }
}