package org.eobjects.analyzer.result.html
import scala.xml.XML

/**
 * A body element which wraps several other body elements in a div
 */
class CompositeBodyElement(cssClassName: String, children: Seq[BodyElement]) extends BodyElement {

  override def toHtml(context: HtmlRenderingContext) = <div class={ cssClassName }>{
    children.map(elem => {
      XML.loadString(elem.toHtml(context))
    })
  }</div>.toString
}