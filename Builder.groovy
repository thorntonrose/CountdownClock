import groovy.util.*
import groovy.xml.*

// properties ---------------------------------------------------------------------------------------------------------

startTime = System.currentTimeMillis()
ant = new AntBuilder()
ivy = groovy.xml.NamespaceBuilder.newInstance(ant, "antlib:org.apache.ivy.ant")
env = System.getenv()
props = System.properties
args.findAll { it.contains("=") }.collect { it.split("=") }.each { props[it[0]] = it[1] }

appOrg = "com.ericsson"
appName = "countdown"
appDisplayName = "Countdown"
appVersion = "1.0"
appPackage = "${appOrg}.${appName}"
appJar = "$appName-${appVersion}.jar"
appWar = "$appName-${appVersion}.war"
appZip = "$appName-${appVersion}.zip"

buildDir = "build"
classesDir = "$buildDir/classes"
distDir = "$buildDir/dist"
libDir = "$buildDir/lib"
resourcesDir = "resources"
runResourcesDir = "$resourcesDir/run"
warResourcesDir = "$resourcesDir/war"
sourceDir = "src"

testDir = "test"
testClassesDir = "$buildDir/test-classes"
testDbDir = "$buildDir/db"
testIncludes = props.testIncludes ?: "**/*Test.class"
testExcludes = props.testExcludes ?: "**/integrationtest/**"
testResourcesDir = "$resourcesDir/test"
testResultsDir = "$buildDir/test-results"

clientTestDir = "client-test"
clientTestClassesDir = "$buildDir/client-test-classes"
clientTestResourcesDir = "$resourcesDir/client-test"
clientTestResultsDir = "$buildDir/client-test-results"
clientTestIncludes = props.clientTestIncludes ?: "**/*Test.class"
clientTestExcludes = props.clientTestExcludes ?: ""

javacDebug = true
javacDebugLevel = "source,lines,vars"

ivyLocalRoot = "${props.'user.home'}/.ivy2/local"
ivySharedRoot = "http://10.116.2.180:8001/archiva/repository/internal"

ivyDependencies = [
   build: [
      "javaee:javaee-api:5"
   ],

   run: [
      "asm:asm:3.3.1",
      "com.sun.jersey:jersey-bundle:1.12",
      "com.sun.jersey:jersey-core:1.12",
      "commons-collections:commons-collections:3.2.1",
      "commons-beanutils:commons-beanutils:1.8.3",
      "commons-lang:commons-lang:2.4",
      "commons-logging:commons-logging:1.1.1",
      "log4j:log4j:1.2.14",
      "org.codehaus.groovy:groovy-all:2.2.1",
      "org.mortbay.jetty:jetty:6.1.24",
      "org.mortbay.jetty:jetty-util:6.1.24",
      "org.mortbay.jetty:servlet-api:2.5-20081211"
   ],

   test: [
      "junit:junit:4.8.2"
   ]
]

// actions ------------------------------------------------------------------------------------------------------------

clean = [
   help: "Delete build artifacts.",
   do: {
      println "clean..."
      ant.delete(quiet: true, dir: buildDir)
   }
]

build = [
   help: "Compile, jar, dist.",
   do: {
      println "build..."
      compile.do()
      jar.do()
      dist.do()
   }
]

compile = [
   help: "Compile app, tests.",
   do: {
      println "compile..."
      compileApp.do()
      compileTests.do()
   }
]

compileApp = [
   help: "Compile app code.",
   do: {
      println "compileApp..."
      ant.mkdir(dir: classesDir)
      deps.do()

      ant.groovyc(srcdir: sourceDir, destdir: classesDir, classpathref: "compile.path") {
         javac(debug: javacDebug, debugLevel: javacDebugLevel)
      }
   }
]

compileTests = [
   help: "Compile test code.",
   do: {
      println "compileTests..."
      deps.do()
      ant.mkdir(dir: testClassesDir)
      ant.mkdir(dir: clientTestClassesDir)

      // unit tests
      ant.groovyc(srcdir: testDir, destdir: testClassesDir, classpathref: "test.path") {
         javac(debug: javacDebug, debugLevel: javacDebugLevel)
      }

      // client tests
      ant.groovyc(srcdir: clientTestDir, destdir: clientTestClassesDir, classpathref: "client-test.path") {
         javac(debug: javacDebug, debugLevel: javacDebugLevel)
      }
   }
]

jar = [
   help: "Create app jar.",
   do: {
      println "jar..."

      ant.jar(destfile: "$buildDir/$appJar") {
         fileset(dir: classesDir, includes: "**/*")
         fileset(dir: "$resourcesDir/jar", includes: "**/*", errorOnMissingDir: false)
      }
   }
]

dist = [
   help: "Create app distribution.",
   do: {
      println "dist..."
      ant.delete(quiet: true, includeEmptyDirs: true) { fileset(dir: distDir, includes: "**/*") }
      assert !(new File("$distDir/db").exists())
      ant.mkdir(dir: distDir)

      // libs
      ant.copy(todir: "$distDir/lib") {
         fileset(dir: buildDir, includes: "$appJar")
         fileset(dir: "$libDir/run", includes: "*")
      }

      // config files and scripts
      ant.copy(todir: distDir) {
         fileset(dir: runResourcesDir, includes: "*")

         filterset {
            filter(token: "APP_NAME", value: appName)
            filter(token: "APP_VERSION", value: appVersion)
            filter(token: "APP_JAR", value: appJar)
            filter(token: "APP_WAR", value: appWar)
            filter(token: "APP_PACKAGE", value: appPackage)
         }
      }

      // war
      distWar.do()

      // zip
      ant.zip(destfile: "$buildDir/$appZip") {
         fileset(dir: distDir, includes: "**")
      }
   }
]

distWar = [
   help: "Create webapp (WAR) distribution.",
   do: {
      println "distWar..."

      ant.copy(todir: "$distDir/war") {
         fileset(dir: warResourcesDir, includes: "**/*")

         filterset {
            filter(token: "APP_DISPLAY_NAME", value: appDisplayName)
            filter(token: "APP_NAME", value: appName)
            filter(token: "APP_VERSION", value: appVersion)
         }
      }
   }
]

//---------------------------------------------------------------------------------------------------------------------

test = [
   help: "Run tests.",
   do: {
      println "test..."
      deps.do()

      ant.delete(quiet: true, includeEmptyDirs: true) {
         fileset(dir: ".", includes: "junit*.properties,$testResultsDir/**,$testDbDir/**")
         fileset(dir: buildDir, includes: "test.log")
      }

      Thread.sleep(500)
      assert !(new File(testDbDir).exists())
      ant.mkdir(dir: testResultsDir)

      ant.junit(printsummary: true, showoutput: true, fork: true, errorproperty: "testErrors",
            failureproperty: "testFailures") {
         sysproperty(key: "env", value: "test")
         sysproperty(key: "buildDir", value: buildDir)
         sysproperty(key: "testDbDir", value: testDbDir)
         classpath(refid: "test.path")
         formatter(type: "xml")

         batchtest(todir: testResultsDir) {
            fileset(dir: testClassesDir, includes: testIncludes, excludes: testExcludes)
         }
      }

      testErrors = ant.project.properties.testErrors
      testFailures = ant.project.properties.testFailures

      testReport.do()
   }
]

testReport = [
   help: "Generate test report.",
   do: {
      println "testReport..."

      ant.junitreport(todir: testResultsDir) {
         fileset(dir: testResultsDir, includes: "*.xml")
         report(todir: testResultsDir)
      }
   }
]

clientTest = [
   help: "Run client tests. (Note: Jetty must be running.)",
   do: {
      println "clientTest..."

      deps.do()
      ant.delete(quiet: true, file: "$buildDir/client-test.log")
      ant.mkdir(dir: clientTestResultsDir)

      ant.junit(printSummary: true, showOutput: true, fork: true, errorProperty: "testErrors",
            failureproperty: "testFailures") {
         sysproperty(key: "env", value: "clientTest")
         classpath(refid: "client-test.path")
         formatter(type: "xml")

         batchtest(todir: clientTestResultsDir) {
            fileset(dir: clientTestClassesDir, includes: clientTestIncludes, excludes: clientTestExcludes)
         }
      }

      testErrors = ant.project.properties.testErrors
      testFailures = ant.project.properties.testFailures

      clientTestReport.do()
   }
]

clientTestReport = [
   help: "Generate client test report.",
   do: {
      println "clientTestReport..."

      ant.junitreport(todir: clientTestResultsDir) {
         fileset(dir: clientTestResultsDir, includes: "*.xml")
         report(todir: clientTestResultsDir) { param(name: "TITLE", expression: "Client Test Results") }
      }
   }
]

//---------------------------------------------------------------------------------------------------------------------

deps = [
   help: "Resolve dependencies and initialize classpaths.",
   do: {
      println "deps..."
      if (ant.project.references."libs.path") { return }
      ant.mkdir(dir: libDir)

      // configure

      def ivySettingsWriter = new StringWriter()

      new MarkupBuilder(ivySettingsWriter).ivysettings {
         settings(defaultResolver: "default")

         resolvers {
             filesystem(name: "local") {
                 ivy(pattern: "$ivyLocalRoot/[organisation]/[module]/[revision]/ivy.xml")
                 artifact(pattern: "$ivyLocalRoot/[organisation]/[module]/[revision]/[artifact].[ext]")
             }

             url(name: "shared", m2compatible: true, checksums: "") {
                 ivy(pattern: "$ivySharedRoot/[organisation]/[module]/[revision]/ivy.xml")
                 artifact(pattern: "$ivySharedRoot/[organisation]/[module]/[revision]/[artifact]-[revision].[ext]")
             }

             chain(name: "default", returnFirst: true) {
                 resolver(ref: "local")
                 resolver(ref: "shared")
             }
         }
      }

      new File("$buildDir/ivysettings.xml").write(ivySettingsWriter.toString())
      ivy.configure(file: "$buildDir/ivysettings.xml")

      // resolve

      def ivyModuleWriter = new StringWriter()

      new MarkupBuilder(ivyModuleWriter)."ivy-module"(version: "1.0") {
         info(organisation: appOrg, module: appName, revision: appVersion)
         configurations(defaultconfmapping: "*") { ivyDependencies.keySet().each { conf(name: it) } }

         dependencies {
            ivyDependencies.each { conf, list ->
               list.each {
                  def (org, name, rev) = it.split(":")
                  dependency(org: org, name: name, rev: rev, conf: conf)
               }
            }
         }
      }

      new File("$buildDir/ivy.xml").write(ivyModuleWriter.toString())
      ivy.resolve(file: "$buildDir/ivy.xml", transitive: false)

      // retrieve
      ivy.retrieve(sync: true, pattern: "$libDir/[conf]/[artifact]-[revision].[ext]")

      // artifact properties
      ivy.artifactproperty(name: "[organisation]:[module]", value: "$libDir/[conf]/[artifact]-[revision].[ext]")

      // classpaths

      ant.path(id: "libs.path") {
         new File(libDir).eachDir { d ->
            fileset(dir: d, includes: "*.jar")
         }
      }

      ant.path(id: "compile.path") {
         pathelement(location: classesDir)
         path(refid: "libs.path")
      }

      ant.path(id: "test.path") {
         pathelement(location: testResourcesDir)
         pathelement(location: testClassesDir)
         pathelement(location: runResourcesDir)
         path(refid: "compile.path")
      }

      ant.path(id: "client-test.path") {
         pathelement(location: clientTestResourcesDir)
         pathelement(location: clientTestClassesDir)
         pathelement(location: testClassesDir)
         pathelement(location: runResourcesDir)
         path(refid: "compile.path")
      }

      // taskdefs
      ant.taskdef(name: "groovyc", classname: "org.codehaus.groovy.ant.Groovyc")
   }
]

// main ---------------------------------------------------------------------------------------------------------------

println "$appOrg, $appName, $appVersion"
def actions = args.findAll { ! it.contains("=") }

if (actions) {
   actions.each { binding."$it".do() }

   if (ant.project.getProperty("testFailures") || ant.project.getProperty("testErrors")) {
      println "WARNING: Test failures or errors."
   }

   duration = (System.currentTimeMillis() - startTime) / 1000
   min = (int) (duration / 60)
   sec = duration - (min * 60)

   println "Completed: $min min, $sec sec"
} else {
   println(
      "Usage:\n" +
      "   groovy ${this.class.simpleName} [<action>]... [<property>=<value>]...\n" +
      "\n" +
      "Actions:\n" +
      binding.variables.findAll { k, v -> (v instanceof Map) && v.help }.sort().collect { k, v ->
         "   $k = ${v.help}\n" }.join("") +
      "\n" +
      "Examples:\n" +
      "   groovy ${this.class.simpleName} compile\n" +
      "   groovy ${this.class.simpleName} compile test testIncludes=**/FooTest.class"
   )
}
