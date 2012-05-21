package org.eobjects.analyzer.beans.valuedist
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.result.html.HtmlRenderer
import org.eobjects.analyzer.result.html.SimpleHtmlFragment
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat
import scala.collection.JavaConversions._
import scala.xml.Node
import org.eobjects.analyzer.result.html.GoogleChartHeadElement
import org.eobjects.analyzer.result.html.HtmlUtils

@RendererBean(classOf[HtmlRenderingFormat])
class ValueDistributionResultHtmlRenderer(includeChart: Boolean) extends HtmlRenderer[ValueDistributionResult] {

  def this() = this(true)

  def handleFragment(frag: SimpleHtmlFragment, result: ValueDistributionResult) = {

    if (includeChart) {
      frag.addHeadElement(GoogleChartHeadElement);
    }

    val html = <div class="valueDistributionResultContainer">
                 {
                   if (result.isGroupingEnabled()) {
                     result.getGroupedValueDistributionResults().map(r => {
                       renderGroupResult(r, frag)
                     })
                   } else {
                     val r = result.getSingleValueDistributionResult();
                     renderGroupResult(r, frag);
                   }
                 }
               </div>;

    frag.addBodyElement(html.toString())
  }

  def renderGroupResult(result: ValueDistributionGroupResult, frag: SimpleHtmlFragment): Node = {
    val chartElementId: String = if (includeChart) HtmlUtils.createElementId() else "";
    
    frag.addHeadElement(new ValueDistributionChartScriptHeadElement(result, chartElementId));

    return <div class="valueDistributionGroupPanel">
             {
               if (result.getGroupName() != null) {
                 <h3>Group: { result.getGroupName() }</h3>
               }
             }
             {
               if (includeChart) {
                 <div class="valueDistributionChart" id={chartElementId}>
                 </div>
               }
             }
             {
               if (result.getTopValues().getActualSize() + result.getBottomValues().getActualSize() > 0) {
                 <table class="valueDistributionValueTable">
                   {
                     result.getTopValues().getValueCounts().map(vc => {
                       <tr><td>{ vc.getValue() }</td><td>{ vc.getCount() }</td></tr>
                     })
                   }
                   {
                     result.getBottomValues().getValueCounts().map(vc => {
                       <tr><td>{ vc.getValue() }</td><td>{ vc.getCount() }</td></tr>
                     })
                   }
                 </table>
               }
             }
             <table class="valueDistributionSummaryTable">
               <tr><td>Total count</td><td>{ result.getTotalCount() }</td></tr>
               <tr><td>Distinct count</td><td>{ result.getDistinctCount() }</td></tr>
               <tr><td>Unique count</td><td>{ result.getUniqueCount() }</td></tr>
               <tr><td>Null count</td><td>{ result.getNullCount() }</td></tr>
             </table>
           </div>;
  }
}