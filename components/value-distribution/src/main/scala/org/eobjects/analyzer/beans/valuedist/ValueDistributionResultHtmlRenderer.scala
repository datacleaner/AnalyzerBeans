package org.eobjects.analyzer.beans.valuedist
import org.eobjects.analyzer.beans.api.Provided
import org.eobjects.analyzer.beans.api.RendererBean
import org.eobjects.analyzer.beans.api.Renderer
import org.eobjects.analyzer.beans.api.RendererPrecedence
import org.eobjects.analyzer.result.html.HtmlFragment
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat
import org.eobjects.analyzer.result.renderer.RendererFactory
import javax.inject.Inject

@RendererBean(classOf[HtmlRenderingFormat])
class ValueDistributionResultHtmlRenderer(rf: RendererFactory) extends Renderer[ValueDistributionResult, HtmlFragment] {

  @Inject
  @Provided
  var rendererFactory: RendererFactory = rf;

  def this() = this(null)

  override def getPrecedence(result: ValueDistributionResult) = RendererPrecedence.HIGH;

  override def render(result: ValueDistributionResult): HtmlFragment = new ValueDistributionHtmlFragment(result, rendererFactory);
}