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
      val displayCount: String = vc.getCount().toString();
      val displayText: String = vc.getValue();
    }

    return """<script type="text/javascript">
     var data = [
     """ +
      valueCounts.map(vc => {
        "{label:\"" + context.escapeJson(LabelUtils.getValueLabel(vc.getValue())) + "\", " + "data:" + +vc.getCount() + "}" + "";
      }).mkString(",") + """
     ];
     wait_for_script_load('jQuery', function() {
        importJS('http://cdnjs.cloudflare.com/ajax/libs/flot/0.7/jquery.flot.min.js', 'jQuery.plot', function() {
            importJS('http://cdnjs.cloudflare.com/ajax/libs/flot/0.7/jquery.flot.pie.min.js', "jQuery.plot.plugins[0]", function() {
                var elem = jQuery("#""" + chartElementId +  """");
                jQuery.plot(elem, data, {
                    series: {
                        pie: {
                            show: true,
                            radius: 3/5,
                        }
                    },
                    grid: {
                        hoverable: true,
                        clickable: true
                    }
                });
                elem.bind("plothover", function(event, pos, obj) {
                    if (!obj) { return; }
                    percent = parseFloat(obj.series.percent).toFixed(2);
                    jQuery("#hover").html('<span style="font-weight: bold; color: '+obj.series.color+'">'+obj.series.label+' ('+percent+'%)</span>');
                });
                elem.bind("plotclick", function(event, pos, obj) {
                    if (!obj) { return; }
                    percent = parseFloat(obj.series.percent).toFixed(2);
                    alert(''+obj.series.label+': '+percent+'%');
                });
            });
        });
    });
</script>
"""
  }
}