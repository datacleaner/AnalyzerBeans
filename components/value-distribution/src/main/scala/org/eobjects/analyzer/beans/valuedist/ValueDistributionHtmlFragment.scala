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
               if (groupResult.getTopValues().getActualSize() + groupResult.getBottomValues().getActualSize() > 0) {
                 <table class="valueDistributionValueTable">
                   {
                     groupResult.getTopValues().getValueCounts().map(vc => {
                       <tr><td>{ vc.getValue() }</td><td>{ getCount(groupResult, vc, context) }</td></tr>
                     })
                   }
                   {
                     groupResult.getBottomValues().getValueCounts().map(vc => {
                       <tr><td>{ vc.getValue() }</td><td>{ getCount(groupResult, vc, context) }</td></tr>
                     })
                   }
                 </table>
               }
             }
             <table class="valueDistributionSummaryTable">
               <tr><td>Total count</td><td>{ groupResult.getTotalCount() }</td></tr>
               <tr><td>Distinct count</td><td>{ groupResult.getDistinctCount() }</td></tr>
               <tr><td>Unique count</td><td>{ groupResult.getUniqueCount() }</td></tr>
               <tr><td>Null count</td><td>{ groupResult.getNullCount() }</td></tr>
             </table>
           </div>;
  }

  def getCount(groupResult: ValueDistributionGroupResult, vc: ValueCount, context: HtmlRenderingContext): scala.xml.Node = {
    val annotatedRowsResult = groupResult.getAnnotatedRows(vc.getValue());

    if (annotatedRowsResult == null) {
      return <span>{ vc.getCount() }</span>;
    } else {
      val elementId = context.createElementId();

      val bodyElement = new DrillToDetailsBodyElement(elementId, rendererFactory, annotatedRowsResult);
      frag.addBodyElement(bodyElement);

      val invocation = bodyElement.toJavaScriptInvocation()

      return <a class="drillToDetailsLink" href="#" onclick={ invocation }>{ vc.getCount() }</a>
    }
  }
}