package org.eobjects.analyzer.result.html
import scala.xml.XML

/**
 * A body element which wraps several other body elements in a div
 */
class CompositeBodyElement(cssClassName: String, children: Seq[BodyElement]) extends BodyElement {

  def toHtml = <div class={ cssClassName }>{
    children.map(elem => {
      XML.loadString(elem.toHtml())
    })
  }</div>.toString
}