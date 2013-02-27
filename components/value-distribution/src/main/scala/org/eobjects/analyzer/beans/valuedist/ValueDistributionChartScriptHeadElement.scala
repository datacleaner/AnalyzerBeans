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
    //<![CDATA[
    var data = [
        """ +
  valueCounts.map(vc => {
    "{label:\"" + context.escapeJson(LabelUtils.getValueLabel(vc.getValue())) + "\", " + "data:" + +vc.getCount() + "}" + "";
  }).mkString(",") + """
    ];
    draw_value_distribution_pie('""" + chartElementId +  """', data, 2);
    //]]>
</script>
"""
  }
}