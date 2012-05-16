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

/**
 * The default HTML renderer for any AnalyzerResult.
 */
@RendererBean(classOf[HtmlRenderingFormat])
class DefaultAnalyzerResultHtmlRenderer(desc: AnalyzerBeanDescriptor[_], job: AnalyzerJob) extends HtmlRenderer[AnalyzerResult] {

  @Inject
  var descriptor = desc;

  @Inject
  var analyzerJob = job;

  def this() = this(null, null)

  override def getPrecedence(renderable: AnalyzerResult): RendererPrecedence = {
    return RendererPrecedence.LOWEST;
  }

  def handleFragment(frag: SimpleHtmlFragment, result: AnalyzerResult) = {
    val primaryInputProperties = descriptor.getConfiguredPropertiesForInput(false)
    val columns = primaryInputProperties.flatMap(property => getInputColumns(property));

    val html = <div class="analyzerResultMetrics">{
      descriptor.getResultMetrics().map(m => {
        if (!m.isParameterizedByString()) {
          if (m.isParameterizedByInputColumn()) {
            columns.map(col => {
              <div class="metric">
                <span class="metricName">{ m.getName() } ({ col.getName() })</span>
                <span class="metricValue">{ m.getValue(result, new MetricParameters(col)) }</span>
              </div>
            });
          } else {
            <div class="metric">
              <span class="metricName">{ m.getName() }</span>
              <span class="metricValue">{ m.getValue(result, null) }</span>
            </div>
          }
        }
      })
    }</div>

    frag.addBodyElement(html.toString())
  }

  def getInputColumns(property: ConfiguredPropertyDescriptor): Seq[InputColumn[_]] = {
    val value = analyzerJob.getConfiguration().getProperty(property);
    value match {
      case value: InputColumn[_] => Seq(value);
      case value: Array[InputColumn[_]] => value.toSeq
      case _ => Nil
    }
  }
}