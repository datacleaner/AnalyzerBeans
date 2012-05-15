package org.eobjects.analyzer.result.html

import org.eobjects.analyzer.beans.api.Renderer
import org.eobjects.analyzer.beans.api.RendererPrecedence
import org.eobjects.analyzer.result.renderer.Renderable
import scala.xml.Node
import scala.xml.Elem

trait HtmlRenderer[R <: Renderable] extends Renderer[R, HtmlFragment] {

  def getPrecedence(renderable: R): RendererPrecedence = {
    return RendererPrecedence.MEDIUM;
  }

  def render(result: R): HtmlFragment = {
    val frag = new SimpleHtmlFragment();
    handleFragment(frag, result);
    return frag;
  }
  
  def handleFragment(frag: SimpleHtmlFragment, result: R): Unit
}