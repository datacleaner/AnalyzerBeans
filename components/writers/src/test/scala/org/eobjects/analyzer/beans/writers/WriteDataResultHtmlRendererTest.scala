package org.eobjects.analyzer.beans.writers
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.junit.Assert

class WriteDataResultHtmlRendererTest extends AssertionsForJUnit {

  @Test
  def testRendering() = {
    val result = new WriteDataResultImpl(2, 3, "datastore", "schema", "table");
    val renderer = new WriteDataResultHtmlRenderer();
    val htmlFragment = renderer.render(result);

    assert(0 == htmlFragment.getHeadElements().size());
    assert(1 == htmlFragment.getBodyElements().size(), { "Found " + htmlFragment });

    Assert.assertEquals("""<div>
                 <p>Executed 2 inserts</p>
                 <p>Executed 3 updates</p>
                 
               </div>""".replaceAll("\r\n","\n"), htmlFragment.getBodyElements().get(0).toHtml().replaceAll("\r\n","\n"));
  }
}