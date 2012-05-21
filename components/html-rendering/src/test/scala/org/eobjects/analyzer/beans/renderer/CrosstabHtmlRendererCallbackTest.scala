package org.eobjects.analyzer.result.renderer
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import org.eobjects.analyzer.result.Crosstab
import org.junit.Assert
import java.util.Arrays

class CrosstabHtmlRendererCallbackTest extends AssertionsForJUnit {

  @Test
   def testOneDimension() = {
       val c = new Crosstab[Integer](classOf[Integer], "Region");
        c.where("Region", "EU").put(1, true);
        c.where("Region", "USA").put(2, true);
        c.where("Region", "Asia").put(3, true);

        val crosstabRenderer = new CrosstabRenderer(c);
        val result1 = crosstabRenderer.render(new HtmlCrosstabRendererCallback(null)).getBodyElements().get(0).toHtml();
        Assert.assertEquals("<table class='crosstabTable'><tr><td>EU</td><td>USA</td><td>Asia</td></tr>"
                + "<tr><td>1</td><td>2</td><td>3</td></tr></table>", result1.replaceAll("\"", "'"));

        crosstabRenderer.makeVertical(c.getDimension(0));
val         result2 = crosstabRenderer.render(new HtmlCrosstabRendererCallback(null)).getBodyElements().get(0).toHtml();
        Assert.assertEquals("<table class='crosstabTable'><tr><td>EU</td><td>1</td></tr>" + "<tr><td>USA</td><td>2</td></tr>"
                + "<tr><td>Asia</td><td>3</td></tr></table>", result2.replaceAll("\"", "'"));
    }

    @Test
    def testMultipleDimensions() = {
        // creates a crosstab of some metric (simply iterated for simplicity)
        // based on person characteristica, examplified with Region (EU and
        // USA), Age-group (children, teenagers and adult)
        // and Gender (male and female)

        val c = new Crosstab[Integer](classOf[Integer], "Region", "Age-group", "Gender", "Native");
         val genderValues = Array[String]("Male", "Female");
        val regionValues = Array[String]("EU", "USA");
        val ageGroupValues = Array[String]("Child", "Teenager", "Adult");
        val nativeValues = Array[String]("Yes", "No, immigrant", "No, second-generation");

        var i = 0;
        for (gender <- genderValues) {
            for (region <- regionValues) {
                for (ageGroup <- ageGroupValues) {
                    for (nativeValue <- nativeValues) {
                        c.where("Region", region).where("Age-group", ageGroup).where("Gender", gender)
                                .where("Native", nativeValue).put(i, true);
                        i = i+1;
                    }
                }
            }
        }

        val dimensionNames: Array[String] = c.getDimensionNames();
        Assert.assertEquals("Array(Region, Age-group, Gender, Native)", dimensionNames.deepToString());

        val crosstabRenderer = new CrosstabRenderer(c);

        // auto-assigned axises
        Assert.assertEquals(
                "<table class='crosstabTable'><tr><td></td><td></td><td class='crosstabHorizontalHeader' colspan='3'>EU</td><td class='crosstabHorizontalHeader' colspan='3'>USA</td></tr>"
                        + "<tr><td></td><td></td><td>Child</td><td>Teenager</td><td>Adult</td><td>Child</td><td>Teenager</td><td>Adult</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='3'>Male</td><td>Yes</td><td>0</td><td>3</td><td>6</td><td>9</td><td>12</td><td>15</td></tr>"
                        + "<tr><td>No, immigrant</td><td>1</td><td>4</td><td>7</td><td>10</td><td>13</td><td>16</td></tr>"
                        + "<tr><td>No, second-generation</td><td>2</td><td>5</td><td>8</td><td>11</td><td>14</td><td>17</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='3'>Female</td><td>Yes</td><td>18</td><td>21</td><td>24</td><td>27</td><td>30</td><td>33</td></tr>"
                        + "<tr><td>No, immigrant</td><td>19</td><td>22</td><td>25</td><td>28</td><td>31</td><td>34</td></tr>"
                        + "<tr><td>No, second-generation</td><td>20</td><td>23</td><td>26</td><td>29</td><td>32</td><td>35</td></tr></table>",
                crosstabRenderer.render(new HtmlCrosstabRendererCallback(null)).getBodyElements().get(0).toHtml()
                        .replaceAll("\"", "'"));

        // try all vertical
        crosstabRenderer.makeVertical(c.getDimension(0));
        crosstabRenderer.makeVertical(c.getDimension(1));
        crosstabRenderer.makeVertical(c.getDimension(2));
        crosstabRenderer.makeVertical(c.getDimension(3));
       Assert. assertEquals(
                "<table class='crosstabTable'><tr><td class='crosstabVerticalHeader' rowspan='18'>Male</td><td class='crosstabVerticalHeader' rowspan='6'>Yes</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td>Child</td><td>0</td></tr>"
                        + "<tr><td>Teenager</td><td>3</td></tr>"
                        + "<tr><td>Adult</td><td>6</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td>Child</td><td>9</td></tr>"
                        + "<tr><td>Teenager</td><td>12</td></tr>"
                        + "<tr><td>Adult</td><td>15</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='6'>No, immigrant</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td>Child</td><td>1</td></tr>"
                        + "<tr><td>Teenager</td><td>4</td></tr>"
                        + "<tr><td>Adult</td><td>7</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td>Child</td><td>10</td></tr>"
                        + "<tr><td>Teenager</td><td>13</td></tr>"
                        + "<tr><td>Adult</td><td>16</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='6'>No, second-generation</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td>Child</td><td>2</td></tr>"
                        + "<tr><td>Teenager</td><td>5</td></tr>"
                        + "<tr><td>Adult</td><td>8</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td>Child</td><td>11</td></tr>"
                        + "<tr><td>Teenager</td><td>14</td></tr>"
                        + "<tr><td>Adult</td><td>17</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='18'>Female</td><td class='crosstabVerticalHeader' rowspan='6'>Yes</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td>Child</td><td>18</td></tr>"
                        + "<tr><td>Teenager</td><td>21</td></tr>"
                        + "<tr><td>Adult</td><td>24</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td>Child</td><td>27</td></tr>"
                        + "<tr><td>Teenager</td><td>30</td></tr>"
                        + "<tr><td>Adult</td><td>33</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='6'>No, immigrant</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td>Child</td><td>19</td></tr>"
                        + "<tr><td>Teenager</td><td>22</td></tr>"
                        + "<tr><td>Adult</td><td>25</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td>Child</td><td>28</td></tr>"
                        + "<tr><td>Teenager</td><td>31</td></tr>"
                        + "<tr><td>Adult</td><td>34</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='6'>No, second-generation</td><td class='crosstabVerticalHeader' rowspan='3'>EU</td><td>Child</td><td>20</td></tr>"
                        + "<tr><td>Teenager</td><td>23</td></tr>"
                        + "<tr><td>Adult</td><td>26</td></tr>"
                        + "<tr><td class='crosstabVerticalHeader' rowspan='3'>USA</td><td>Child</td><td>29</td></tr>"
                        + "<tr><td>Teenager</td><td>32</td></tr>" + "<tr><td>Adult</td><td>35</td></tr></table>",
                crosstabRenderer.render(new HtmlCrosstabRendererCallback(null)).getBodyElements().get(0).toHtml()
                        .replaceAll("\"", "'"));
    }
}