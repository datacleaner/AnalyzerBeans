package org.eobjects.analyzer.beans.valuedist
import org.eobjects.analyzer.result.html.HtmlFragment
import org.eobjects.analyzer.result.renderer.RendererFactory
import org.eobjects.analyzer.result.html.HtmlRenderingContext
import org.eobjects.analyzer.result.html.HeadElement
import org.eobjects.analyzer.result.html.BodyElement
import org.eobjects.analyzer.result.html.DrillToDetailsBodyElement
import org.eobjects.analyzer.result.html.SimpleHtmlFragment
import scala.collection.JavaConversions._
import org.eobjects.analyzer.result.html.GoogleChartHeadElement
import java.util.TreeSet
import java.util.Collections
import org.eobjects.analyzer.util.LabelUtils
import org.eobjects.analyzer.result.ListResult
import org.eobjects.analyzer.result.ValueCount
import org.eobjects.analyzer.result.ValueCountingAnalyzerResult
import org.eobjects.analyzer.result.GroupedValueCountingAnalyzerResult

class ValueDistributionHtmlFragment(result: ValueCountingAnalyzerResult, rendererFactory: RendererFactory) extends HtmlFragment {

  val frag = new SimpleHtmlFragment();

  override def initialize(context: HtmlRenderingContext) {
    frag.addHeadElement(GoogleChartHeadElement);

    val html = <div class="valueDistributionResultContainer">
                 {
                   if (result.isInstanceOf[GroupedValueCountingAnalyzerResult]) {
                     val groupedResult = result.asInstanceOf[GroupedValueCountingAnalyzerResult];
                     groupedResult.getGroupResults().map(r => {
                       renderResult(r, context, true)
                     })
                   } else {
                     renderResult(result, context, false);
                   }
                 }
               </div>;

    frag.addBodyElement(html.toString())
  }

  override def getHeadElements(): java.util.List[HeadElement] = {
    return frag.getHeadElements();
  }

  override def getBodyElements(): java.util.List[BodyElement] = {
    return frag.getBodyElements();
  }

  def renderResult(result: ValueCountingAnalyzerResult, context: HtmlRenderingContext, group: Boolean): scala.xml.Node = {
    val chartElementId: String = context.createElementId();

    frag.addHeadElement(new ValueDistributionChartScriptHeadElement(result, chartElementId));

    val valueCounts = result.getValueCounts();

    val uniqueCount = result.getUniqueCount();
    if (uniqueCount != null && uniqueCount > 0) {
      val uniqueValues = result.getUniqueValues();
      if (uniqueValues == null || uniqueValues.isEmpty()) {
        val vc = new ValueCount(LabelUtils.UNIQUE_LABEL, uniqueCount);
        valueCounts.add(vc);
      } else {
        uniqueValues.foreach(str => valueCounts.add(new ValueCount(str, 1)));
      }
    }

    return <div class="valueDistributionGroupPanel">
             {
               if (group && result.getName() != null) {
                 <h3>Group: { result.getName() }</h3>
               }
             }
             {
               <div class="valueDistributionChart" id={ chartElementId }>
               </div>
             }
             {
               if (!valueCounts.isEmpty()) {
                 <table class="valueDistributionValueTable">
                   {
                     valueCounts.iterator().map(vc => {
                       <tr><td>{ LabelUtils.getLabel(vc.getValue()) }</td><td>{ getCount(result, vc, context) }</td></tr>
                     })
                   }
                 </table>
               }
             }
             <table class="valueDistributionSummaryTable">
               <tr><td>Total count</td><td>{ result.getTotalCount() }</td></tr>
               {
                 if (result.getDistinctCount() != null) {
                   <tr><td>Distinct count</td><td>{ result.getDistinctCount() }</td></tr>
                 }
               }
             </table>
           </div>;
  }

  def getCount(result: ValueCountingAnalyzerResult, vc: ValueCount, context: HtmlRenderingContext): scala.xml.Node = {
    var value = vc.getValue();
    val count = vc.getCount();
    if (LabelUtils.NULL_LABEL.equals(value)) {
      value = null;
    } else if (LabelUtils.BLANK_LABEL.equals(value)) {
      value = "";
    }

    if (count == 0) {
      return <span>{ vc.getCount() }</span>;
    }

    val annotatedRowsResult = result.getAnnotatedRowsForValue(value);

    if (annotatedRowsResult == null) {
      if (LabelUtils.UNIQUE_LABEL.equals(value)) {
        val uniqueValues = result.getUniqueValues()
        if (uniqueValues != null) {

          val elementId = context.createElementId();
          val listResult = new ListResult(uniqueValues);

          val bodyElement = new DrillToDetailsBodyElement(elementId, rendererFactory, listResult);
          frag.addBodyElement(bodyElement);

          val invocation = bodyElement.toJavaScriptInvocation()

          return <a class="drillToDetailsLink" href="#" onclick={ invocation }>{ vc.getCount() }</a>
        }
      }
      return <span>{ vc.getCount() }</span>;
    }

    val elementId = context.createElementId();

    val bodyElement = new DrillToDetailsBodyElement(elementId, rendererFactory, annotatedRowsResult);
    frag.addBodyElement(bodyElement);

    val invocation = bodyElement.toJavaScriptInvocation()

    return <a class="drillToDetailsLink" href="#" onclick={ invocation }>{ vc.getCount() }</a>
  }
}