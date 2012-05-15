package org.eobjects.analyzer.beans.writers
import org.eobjects.analyzer.beans.api.Renderer
import org.eobjects.analyzer.result.html.HtmlFragment
import org.eobjects.analyzer.result.html.HtmlRenderer
import org.eobjects.analyzer.result.html.SimpleHtmlFragment

class WriteDataResultHtmlRenderer extends HtmlRenderer[WriteDataResult] {

  def handleFragment(frag: SimpleHtmlFragment, r: WriteDataResult) = {
    val inserts = r.getWrittenRowCount()
    val updates = r.getUpdatesCount()

    val html = <div><p>Executed { inserts } inserts</p><p>Executed { updates } updates</p></div>;
    frag.addBodyElement(html.toString());
  }
}