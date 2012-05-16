package org.eobjects.analyzer.beans.valuedist
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.data.MockInputColumn
import org.eobjects.analyzer.data.MockInputRow
import org.junit.Assert

class ValueDistributionResultHtmlRendererTest extends AssertionsForJUnit {

  @Test
  def testRenderMultipleGroups = {
    val col1 = new MockInputColumn[String]("email username", classOf[String]);
    val col2 = new MockInputColumn[String]("email domain", classOf[String]);

    val analyzer = new ValueDistributionAnalyzer(col1, col2, true, null, null);

    analyzer.run(new MockInputRow().put(col1, "kasper").put(col2, "eobjects.dk"), 4);
    analyzer.run(new MockInputRow().put(col1, "kasper.sorensen").put(col2, "eobjects.dk"), 2);
    analyzer.run(new MockInputRow().put(col1, "info").put(col2, "eobjects.dk"), 1);
    analyzer.run(new MockInputRow().put(col1, null).put(col2, "eobjects.dk"), 1);
    analyzer.run(new MockInputRow().put(col1, "kasper.sorensen").put(col2, "humaninference.com"), 1);
    analyzer.run(new MockInputRow().put(col1, "winfried.vanholland").put(col2, "humaninference.com"), 1);
    analyzer.run(new MockInputRow().put(col1, "kaspers").put(col2, "humaninference.com"), 1);

    val result = analyzer.getResult();

    val htmlFragment = new ValueDistributionResultHtmlRenderer().render(result);
    Assert.assertEquals("SimpleHtmlFragment[headElements=0,bodyElements=1]", htmlFragment.toString());

    Assert.assertEquals(1, htmlFragment.getBodyElements().size());
    Assert.assertEquals(0, htmlFragment.getHeadElements().size());

    val html = htmlFragment.getBodyElements().get(0).toHtml();
    Assert.assertEquals("""<div class="valueDistributionResultContainer">
                 <div class="valueDistributionGroupPanel">
             <h3>Group: eobjects.dk</h3>
             <table class="valueDistributionValueTable">
               <tr><td>kasper</td><td>4</td></tr><tr><td>kasper.sorensen</td><td>2</td></tr>
               
             </table>
             <table class="valueDistributionSummaryTable">
               <tr><td>Total count</td><td>8</td></tr>
               <tr><td>Distrinct count</td><td>4</td></tr>
               <tr><td>Unique count</td><td>1</td></tr>
               <tr><td>Null count</td><td>1</td></tr>
             </table>
           </div><div class="valueDistributionGroupPanel">
             <h3>Group: humaninference.com</h3>
             <table class="valueDistributionValueTable">
               
               
             </table>
             <table class="valueDistributionSummaryTable">
               <tr><td>Total count</td><td>3</td></tr>
               <tr><td>Distrinct count</td><td>3</td></tr>
               <tr><td>Unique count</td><td>3</td></tr>
               <tr><td>Null count</td><td>0</td></tr>
             </table>
           </div>
               </div>""".replaceAll("\r\n","\n"), html.replaceAll("\r\n","\n"));
  }

  @Test
  def testRenderSingleGroups = {
    val col1 = new MockInputColumn[String]("email username", classOf[String]);

    val analyzer = new ValueDistributionAnalyzer(col1, true, null, null);

    analyzer.run(new MockInputRow().put(col1, "kasper"), 6);
    analyzer.run(new MockInputRow().put(col1, "kasper.sorensen"), 3);
    analyzer.run(new MockInputRow().put(col1, "kasper"), 3);
    analyzer.run(new MockInputRow().put(col1, "info"), 1);

    val result = analyzer.getResult();

    val htmlFragment = new ValueDistributionResultHtmlRenderer().render(result);
    Assert.assertEquals("SimpleHtmlFragment[headElements=0,bodyElements=1]", htmlFragment.toString());

    Assert.assertEquals(1, htmlFragment.getBodyElements().size());
    Assert.assertEquals(0, htmlFragment.getHeadElements().size());

    val html = htmlFragment.getBodyElements().get(0).toHtml();
    Assert.assertEquals("""<div class="valueDistributionResultContainer">
                 <div class="valueDistributionGroupPanel">
             
             <table class="valueDistributionValueTable">
               <tr><td>kasper</td><td>9</td></tr><tr><td>kasper.sorensen</td><td>3</td></tr>
               
             </table>
             <table class="valueDistributionSummaryTable">
               <tr><td>Total count</td><td>13</td></tr>
               <tr><td>Distrinct count</td><td>3</td></tr>
               <tr><td>Unique count</td><td>1</td></tr>
               <tr><td>Null count</td><td>0</td></tr>
             </table>
           </div>
               </div>""".replaceAll("\r\n","\n"), html.replaceAll("\r\n","\n"));
  }
}