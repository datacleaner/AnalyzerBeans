package org.eobjects.analyzer.beans.valuedist
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.eobjects.analyzer.data.InputColumn
import org.eobjects.analyzer.data.MockInputColumn
import org.eobjects.analyzer.data.MockInputRow
import org.junit.Assert
import org.eobjects.analyzer.result.html.HtmlUtils
import org.eobjects.analyzer.result.html.GoogleChartHeadElement
import org.eobjects.analyzer.result.renderer.RendererFactory
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider
import org.eobjects.analyzer.result.html.DrillToDetailsHeadElement

class ValueDistributionResultHtmlRendererTest extends AssertionsForJUnit {

  @Test
  def testRenderMultipleGroups = {
    HtmlUtils.resetIds();

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

    val htmlFragment = new ValueDistributionResultHtmlRenderer(createRendererFactory()).render(result);
    Assert.assertEquals("SimpleHtmlFragment[headElements=5,bodyElements=3]", htmlFragment.toString());

    Assert.assertEquals(3, htmlFragment.getBodyElements().size());
    Assert.assertEquals(5, htmlFragment.getHeadElements().size());

    val html = htmlFragment.getBodyElements().get(2).toHtml();
    Assert.assertEquals("""<div class="valueDistributionResultContainer">
                 <div class="valueDistributionGroupPanel">
             <h3>Group: humaninference.com</h3>
             <div class="valueDistributionChart" id="analysisResultElement4">
               </div>
             
             <table class="valueDistributionSummaryTable">
               <tr><td>Total count</td><td>3</td></tr>
               <tr><td>Distinct count</td><td>3</td></tr>
               <tr><td>Unique count</td><td>3</td></tr>
               <tr><td>Null count</td><td>0</td></tr>
             </table>
           </div><div class="valueDistributionGroupPanel">
             <h3>Group: eobjects.dk</h3>
             <div class="valueDistributionChart" id="analysisResultElement1">
               </div>
             <table class="valueDistributionValueTable">
                   <tr><td>kasper</td><td><a class="drillToDetailsLink" onclick="analysisResult.callback1();return false;" href="#">4</a></td></tr><tr><td>kasper.sorensen</td><td><a class="drillToDetailsLink" onclick="analysisResult.callback2();return false;" href="#">2</a></td></tr>
                   
                 </table>
             <table class="valueDistributionSummaryTable">
               <tr><td>Total count</td><td>8</td></tr>
               <tr><td>Distinct count</td><td>4</td></tr>
               <tr><td>Unique count</td><td>1</td></tr>
               <tr><td>Null count</td><td>1</td></tr>
             </table>
           </div>
               </div>""".replaceAll("\r\n", "\n"), html.replaceAll("\r\n", "\n"));

    Assert.assertEquals(GoogleChartHeadElement, htmlFragment.getHeadElements().get(0))
    Assert.assertEquals("""<script type="text/javascript">
                   google.setOnLoadCallback(function() {
                     var elem = document.getElementById("analysisResultElement1");
                     var options = {};
                     
                     var data = google.visualization.arrayToDataTable([
                         ['Value', 'Count'],['kasper', 4],['kasper.sorensen', 2],['<null>', 1],['<unique>', 1]
                     ]);
                     
                     var chart = new google.visualization.PieChart(elem);
                     chart.draw(data, options);
                     
                   });
               </script>""".replaceAll("\r\n", "\n"), htmlFragment.getHeadElements().get(1).toHtml().replaceAll("\r\n", "\n"))

    Assert.assertEquals(classOf[DrillToDetailsHeadElement], htmlFragment.getHeadElements().get(2).getClass())
    Assert.assertEquals(classOf[DrillToDetailsHeadElement], htmlFragment.getHeadElements().get(3).getClass())
               
    Assert.assertEquals("""<script type="text/javascript">
                   google.setOnLoadCallback(function() {
                     var elem = document.getElementById("analysisResultElement4");
                     var options = {};
                     
                     var data = google.visualization.arrayToDataTable([
                         ['Value', 'Count'],['<unique>', 3]
                     ]);
                     
                     var chart = new google.visualization.PieChart(elem);
                     chart.draw(data, options);
                     
                   });
               </script>""".replaceAll("\r\n", "\n"), htmlFragment.getHeadElements().get(4).toHtml().replaceAll("\r\n", "\n"))
  }

  @Test
  def testRenderSingleGroups = {
    HtmlUtils.resetIds();

    val col1 = new MockInputColumn[String]("email username", classOf[String]);

    val analyzer = new ValueDistributionAnalyzer(col1, true, null, null);

    analyzer.run(new MockInputRow().put(col1, "kasper"), 6);
    analyzer.run(new MockInputRow().put(col1, "kasper.sorensen"), 3);
    analyzer.run(new MockInputRow().put(col1, "kasper"), 3);
    analyzer.run(new MockInputRow().put(col1, "info"), 1);

    val result = analyzer.getResult();

    val htmlFragment = new ValueDistributionResultHtmlRenderer(createRendererFactory()).render(result);
    Assert.assertEquals("SimpleHtmlFragment[headElements=4,bodyElements=3]", htmlFragment.toString());

    Assert.assertEquals(3, htmlFragment.getBodyElements().size());
    Assert.assertEquals(4, htmlFragment.getHeadElements().size());

    var html = htmlFragment.getBodyElements().get(0).toHtml();
    Assert.assertEquals("""<div id="analysisResultElement2" class="drillToDetailsPanel" style="display:none;">
<table class="annotatedRowsTable"><tr><th>email username</th></tr><tr><td>kasper</td></tr><tr><td>kasper</td></tr></table>
</div>""".replaceAll("\r\n", "\n"), html.replaceAll("\r\n", "\n"));

    html = htmlFragment.getBodyElements().get(1).toHtml();
    Assert.assertEquals("""<div id="analysisResultElement3" class="drillToDetailsPanel" style="display:none;">
<table class="annotatedRowsTable"><tr><th>email username</th></tr><tr><td>kasper.sorensen</td></tr></table>
</div>""".replaceAll("\r\n", "\n"), html.replaceAll("\r\n", "\n"));

    html = htmlFragment.getBodyElements().get(2).toHtml();
    Assert.assertEquals("""<div class="valueDistributionResultContainer">
                 <div class="valueDistributionGroupPanel">
             
             <div class="valueDistributionChart" id="analysisResultElement1">
               </div>
             <table class="valueDistributionValueTable">
                   <tr><td>kasper</td><td><a class="drillToDetailsLink" onclick="analysisResult.callback1();return false;" href="#">9</a></td></tr><tr><td>kasper.sorensen</td><td><a class="drillToDetailsLink" onclick="analysisResult.callback2();return false;" href="#">3</a></td></tr>
                   
                 </table>
             <table class="valueDistributionSummaryTable">
               <tr><td>Total count</td><td>13</td></tr>
               <tr><td>Distinct count</td><td>3</td></tr>
               <tr><td>Unique count</td><td>1</td></tr>
               <tr><td>Null count</td><td>0</td></tr>
             </table>
           </div>
               </div>""".replaceAll("\r\n", "\n"), html.replaceAll("\r\n", "\n"));

    Assert.assertEquals(GoogleChartHeadElement, htmlFragment.getHeadElements().get(0))
    Assert.assertEquals("""<script type="text/javascript">
                   google.setOnLoadCallback(function() {
                     var elem = document.getElementById("analysisResultElement1");
                     var options = {};
                     
                     var data = google.visualization.arrayToDataTable([
                         ['Value', 'Count'],['kasper', 9],['kasper.sorensen', 3],['<unique>', 1]
                     ]);
                     
                     var chart = new google.visualization.PieChart(elem);
                     chart.draw(data, options);
                     
                   });
               </script>""".replaceAll("\r\n", "\n"), htmlFragment.getHeadElements().get(1).toHtml().replaceAll("\r\n", "\n"))
  }

  def createRendererFactory(): RendererFactory = {
    val descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage("org.eobjects.analyzer.beans", true).scanPackage("org.eobjects.analyzer.result.renderer", false);
    val conf = new AnalyzerBeansConfigurationImpl().replace(descriptorProvider);
    return new RendererFactory(conf);
  }
}