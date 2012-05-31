package org.eobjects.analyzer.beans
import org.eobjects.analyzer.result.html.HtmlRenderer
import org.eobjects.analyzer.result.html.SimpleHtmlFragment
import org.eobjects.analyzer.result.renderer.RendererFactory
import org.eobjects.analyzer.result.renderer.CrosstabHtmlRenderer
import org.eobjects.analyzer.beans.api.Provided
import javax.inject.Inject
import org.eobjects.analyzer.result.html.HtmlFragment
import org.eobjects.analyzer.result.html.CompositeBodyElement
import org.eobjects.analyzer.result.html.BodyElement
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat
import org.eobjects.analyzer.result.html.HtmlRenderingContext

@RendererBean(classOf[HtmlRenderingFormat])
class BooleanAnalyzerResultHtmlRenderer(rf: RendererFactory) extends HtmlRenderer[BooleanAnalyzerResult] {

  @Inject
  @Provided
  var rendererFactory: RendererFactory = rf;

  def this() = this(null)

  override def handleFragment(frag: SimpleHtmlFragment, result: BooleanAnalyzerResult) = {
    // render the two crosstabs in this result
    val crosstabRenderer = new CrosstabHtmlRenderer(rendererFactory)

    val columnStatisticsCrosstab = result.getColumnStatisticsCrosstab()
    val columnStatisticsHtmlFragment = if (columnStatisticsCrosstab == null) null else crosstabRenderer.render(columnStatisticsCrosstab)

    val valueCombinationCrosstab = result.getValueCombinationCrosstab()
    val valueCombinationHtmlFragment = if (valueCombinationCrosstab == null) null else crosstabRenderer.render(valueCombinationCrosstab)

    // TODO: Should happen in a initialization method
    val context: HtmlRenderingContext = null;
    
    // add all head elements to the html fragment
    if (columnStatisticsHtmlFragment != null) {
        columnStatisticsHtmlFragment.initialize(context)
        columnStatisticsHtmlFragment.getHeadElements().foreach(frag.addHeadElement(_))
    }
    if (valueCombinationHtmlFragment != null) {
        valueCombinationHtmlFragment.initialize(context)
        valueCombinationHtmlFragment.getHeadElements().foreach(frag.addHeadElement(_))
    }

    // make a composite body element
    var bodyElements = Seq[BodyElement]();
    if (columnStatisticsHtmlFragment != null) {
        bodyElements = bodyElements ++ columnStatisticsHtmlFragment.getBodyElements()
    }
    if (valueCombinationHtmlFragment != null) {
        bodyElements = bodyElements ++ valueCombinationHtmlFragment.getBodyElements()
    }
    val composite = new CompositeBodyElement("booleanAnalyzerResult", bodyElements);
    frag.addBodyElement(composite);
  }
}