package org.eobjects.analyzer.result.html

/**
 * Object responsible for locating the javascript URLs of Flot charts (used for most of the charts/graphs in HTML rendered analyzer results).
 */
object FlotChartLocator {

  val SYSTEM_PROPERTY_FLOT_HOME = "org.eobjects.analyzer.flot.home"
  val SYSTEM_PROPERTY_FLOT_HOME_DEPRECATED = "org.eobjects.analyzer.valuedist.flotLibraryLocation"
  val DEFAULT_FLOT_HOME = "http://cdnjs.cloudflare.com/ajax/libs/flot/0.7"

  /**
   * Gets the URL for the base flot library, typically named as "jquery.flot.min.js"
   */
  def getFlotBaseUrl: String = getFlotHome() + "/jquery.flot.min.js";

  /**
   * Gets the URL for the flot plugin for pie charts, typically named as "jquery.flot.pie.min.js"
   */
  def getFlotPieUrl: String = getFlotHome() + "/jquery.flot.pie.min.js";

  /**
   * Gets the home folder of all flot javascript files
   */
  def getFlotHome(): String = {
    return getSystemProperty(SYSTEM_PROPERTY_FLOT_HOME) match {
      case Some(str) => str;
      case None => getSystemProperty(SYSTEM_PROPERTY_FLOT_HOME_DEPRECATED) match {
        case Some(str) => str
        case None => DEFAULT_FLOT_HOME
      }
    }
  }

  /**
   * Sets the home folder of all flot javascript files
   */
  def setFlotHome(flotHome: String) {
    if (flotHome == null || flotHome.trim().isEmpty()) {
      System.clearProperty(SYSTEM_PROPERTY_FLOT_HOME)
      System.clearProperty(SYSTEM_PROPERTY_FLOT_HOME_DEPRECATED)
    } else {
      val propValue = if (flotHome.endsWith("/")) flotHome.substring(0, flotHome.length() - 1) else flotHome
      System.setProperty(SYSTEM_PROPERTY_FLOT_HOME, propValue)
    }
  }

  /**
   * Helper method to get a system property as an option
   */
  private def getSystemProperty(systemProp: String): Option[String] = {
    val propValue = System.getProperty(systemProp);
    if (propValue == null || propValue.trim().isEmpty()) {
      return None
    }
    return Some(propValue);
  }
}