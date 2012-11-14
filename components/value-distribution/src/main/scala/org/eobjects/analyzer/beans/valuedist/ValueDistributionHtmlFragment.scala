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

class ValueDistributionHtmlFragment(result: ValueDistributionResult, rendererFactory: RendererFactory) extends HtmlFragment {

  val frag = new SimpleHtmlFragment();

  override def initialize(context: HtmlRenderingContext) {
    frag.addHeadElement(GoogleChartHeadElement);

    val html = <div class="valueDistributionResultContainer">
                 {
                   if (result.isGroupingEnabled()) {
                     result.getGroupedValueDistributionResults().map(r => {
                       renderGroupResult(r, context)
                     })
                   } else {
                     val r = result.getSingleValueDistributionResult();
                     renderGroupResult(r, context);
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

  def renderGroupResult(groupResult: ValueDistributionGroupResult, context: HtmlRenderingContext): scala.xml.Node = {
    val chartElementId: String = context.createElementId();

    frag.addHeadElement(new ValueDistributionChartScriptHeadElement(groupResult, chartElementId));

    // create a big sorted set of value counts.
    val valueCounts = new TreeSet[ValueCount]();
    valueCounts.addAll(groupResult.getTopValues().getValueCounts());
    valueCounts.addAll(groupResult.getBottomValues().getValueCounts());

    if (groupResult.getUniqueCount() > 0) {
      valueCounts.add(new ValueCount(LabelUtils.UNIQUE_LABEL, groupResult.getUniqueCount()));
    }

    if (groupResult.getNullCount() > 0) {
      valueCounts.add(new ValueCount(null, groupResult.getNullCount()));
    }

    return <div class="valueDistributionGroupPanel">
             {
               if (groupResult.getGroupName() != null) {
                 <h3>Group: { groupResult.getGroupName() }</h3>
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
                       <tr><td>{ LabelUtils.getLabel(vc.getValue()) }</td><td>{ getCount(groupResult, vc, context) }</td></tr>
                     })
                   }
                 </table>
               }
             }
             <table class="valueDistributionSummaryTable">
               <tr><td>Total count</td><td>{ groupResult.getTotalCount() }</td></tr>
               <tr><td>Distinct count</td><td>{ groupResult.getDistinctCount() }</td></tr>
             </table>
           </div>;
  }

  def getCount(groupResult: ValueDistributionGroupResult, vc: ValueCount, context: HtmlRenderingContext): scala.xml.Node = {
    if (!groupResult.isAnnotationsEnabled()) {
      return <span>{ vc.getCount() }</span>
    }

    var value = vc.getValue();
    if (LabelUtils.NULL_LABEL.equals(value)) {
      value = null;
    } else if (LabelUtils.BLANK_LABEL.equals(value)) {
      value = "";
    }

    val annotatedRowsResult = groupResult.getAnnotatedRows(value);

    if (annotatedRowsResult == null) {
      if (LabelUtils.UNIQUE_LABEL.equals(value) && groupResult.isUniqueValuesAvailable()) {
        val uniqueValues = groupResult.getUniqueValues()
        val elementId = context.createElementId();
        
        val listResult = new ListResult(uniqueValues);

        val bodyElement = new DrillToDetailsBodyElement(elementId, rendererFactory, listResult);
        frag.addBodyElement(bodyElement);

        val invocation = bodyElement.toJavaScriptInvocation()
        
        return <a class="drillToDetailsLink" href="#" onclick={ invocation }>{ vc.getCount() }</a>
      } else {
        return <span>{ vc.getCount() }</span>;
      }
    } else {
      val elementId = context.createElementId();

      val bodyElement = new DrillToDetailsBodyElement(elementId, rendererFactory, annotatedRowsResult);
      frag.addBodyElement(bodyElement);

      val invocation = bodyElement.toJavaScriptInvocation()

      return <a class="drillToDetailsLink" href="#" onclick={ invocation }>{ vc.getCount() }</a>
    }
  }
}