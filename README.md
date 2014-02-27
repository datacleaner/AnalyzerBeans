AnalyzerBeans
=============

An extensible and high-performance data processing engine.

See the main website at http://analyzerbeans.eobjects.org for more information.


## Module structure

Modules are:

 * api - the API for AnalyzerBeans; contains interfaces and annotations to build processing components
 * core - the main processing engine implementation
 * testware - various utilities useful for testing
 * components - contains several submodules for concrete components that can be used in AnalyzerBeans. Some notable submodules are:
  * basic-transformers
  * basic-filters
  * basic-analyzers
  * html-rendering - framework for rendering analysis results as HTML fragments and pages
  * writers - components for inserting and updating data in target datastores
  * (...)
 * env - contains submodules for various environment configurations. Some notable submodules are:
  * cluster - framework for clustering AnalyzerBeans jobs
  * xml-config - reader and writers for jobs and configuration objects to and from XML files (conf.xml and .analysis.xml job files)
  * (...)
 * cli - a command-line interface which can be used to execute AnalyzerBeans jobs

## Continuous Integration

There's a public build of AnalyzerBeans that can be found on Travis CI:

https://travis-ci.org/datacleaner/AnalyzerBeans

## License

Licensed under the Lesser General Public License, see http://www.gnu.org/licenses/lgpl.txt
