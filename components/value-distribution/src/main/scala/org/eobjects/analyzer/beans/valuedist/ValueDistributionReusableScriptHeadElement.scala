package org.eobjects.analyzer.beans.valuedist
import scala.collection.JavaConversions.collectionAsScalaIterable
import org.eobjects.analyzer.result.html.HeadElement
import org.eobjects.analyzer.result.html.HtmlRenderingContext
import org.eobjects.analyzer.result.ValueCountingAnalyzerResult
import org.eobjects.analyzer.util.LabelUtils
import org.eobjects.analyzer.result.ValueCount

/**
 * Defines reusable script parts for value distribution results
 */
object ValueDistributionReusableScriptHeadElement extends HeadElement {

  override def toHtml(context: HtmlRenderingContext): String = {
    val flotBaseLocation = "http://cdnjs.cloudflare.com/ajax/libs/flot/0.7/jquery.flot.min.js";
    val flotPiePluginLocation = "http://cdnjs.cloudflare.com/ajax/libs/flot/0.7/jquery.flot.pie.min.js";
    return """<script type="text/javascript">
//<![CDATA[
function draw_value_distribution_pie(chartElement, chartData, retries) {
    wait_for_script_load('jQuery', function() {
        importJS('""" + flotBaseLocation +  """', 'jQuery.plot', function() {
            importJS('""" + flotPiePluginLocation + """', "jQuery.plot.plugins[0]", function() {
                var elem = document.getElementById(chartElement);
                
                try {
                    jQuery.plot(elem, chartData, {
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
                    jQuery(elem).bind("plothover", function(event, pos, obj) {
                        if (!obj) { return; }
                        percent = parseFloat(obj.series.percent).toFixed(2);
                        jQuery("#hover").html('<span style="font-weight: bold; color: '+obj.series.color+'">'+obj.series.label+' ('+percent+'%)</span>');
                    });
                    jQuery(elem).bind("plotclick", function(event, pos, obj) {
                        if (!obj) { return; }
                        percent = parseFloat(obj.series.percent).toFixed(2);
                        alert(''+obj.series.label+': '+percent+'%');
                    });
                } catch (err) {
                    // error can sometimes occur due to load time issues
                    if (retries > 0) {
                        retries = retries-1;
                        draw_value_distribution_pie(chartElement, chartData, retries);
                    }
                }
            });
        });
    });
}
//]]>
</script>
<style type="text/css">
.graph {
    width: 400px;
    height: 300px;
}
</style>"""
  }
}