package org.eobjects.analyzer.beans.writers
import org.eobjects.analyzer.beans.api.Renderer
import org.eobjects.analyzer.result.html.HtmlFragment
import org.eobjects.analyzer.result.html.HtmlRenderer
import org.eobjects.analyzer.result.html.SimpleHtmlFragment
import scala.xml.PrettyPrinter
import javax.xml.transform.Transformer

class WriteDataResultHtmlRenderer extends HtmlRenderer[WriteDataResult] {

  def handleFragment(frag: SimpleHtmlFragment, r: WriteDataResult) = {
    val inserts = r.getWrittenRowCount()
    val updates = r.getUpdatesCount()
    val errors = r.getErrorRowCount()

    val html = <div>
                 { if (inserts > 0) { <p>Executed { inserts } inserts</p> } }
                 { if (updates > 0) { <p>Executed { updates } updates</p> } }
                 { if (errors > 0) { <p>{ errors } Errornous records</p> } }
               </div>;
               
    frag.addBodyElement(html.toString());
  }
}