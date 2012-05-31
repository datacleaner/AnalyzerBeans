package org.eobjects.analyzer.beans
import org.eobjects.analyzer.result.html.HtmlRenderer
import org.eobjects.analyzer.result.AnalyzerResult
import org.eobjects.analyzer.result.html.SimpleHtmlFragment
import org.eobjects.analyzer.beans.api.RendererPrecedence
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat
import javax.inject.Inject
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor
import scala.collection.JavaConversions._
import org.eobjects.analyzer.job.AnalyzerJob
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor
import org.eobjects.analyzer.descriptors.MetricParameters
import org.eobjects.analyzer.beans.api.Provided

/**
 * The default HTML renderer for any AnalyzerResult.
 */
@RendererBean(classOf[HtmlRenderingFormat])
class DefaultAnalyzerResultHtmlRenderer() extends HtmlRenderer[AnalyzerResult] {

  override def getPrecedence(renderable: AnalyzerResult): RendererPrecedence = {
    return RendererPrecedence.LOWEST;
  }

  def handleFragment(frag: SimpleHtmlFragment, result: AnalyzerResult) = {
    frag.addBodyElement(new MetricListBodyElement(result));
  }
}