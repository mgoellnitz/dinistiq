![dinistiq](https://raw.github.com/mgoellnitz/dinistiq/master/doc/dinistiq.png)

Minimalistic Dependency Injection
=================================

[![Build Status](https://api.travis-ci.org/mgoellnitz/dinistiq.svg?branch=master)](https://travis-ci.org/mgoellnitz/dinistiq)
[![Coverage Status](https://coveralls.io/repos/mgoellnitz/dinistiq/badge.svg)](https://coveralls.io/r/mgoellnitz/dinistiq)
[![Dependency Status](https://www.versioneye.com/user/projects/54ff710b4a10649b1b000053/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ff710b4a10649b1b000053)

A small footprint approach to dependency injection with a framework or container 
implemented in Java.

Or: What I got wrong about DI

Minimalistic library to use dependency injection for the wire-up of software components. 
It thus mostly deals with singletons - some of them implementing interfaces - which should 
be injected as dependencies into one another.

As the only other option besides the single scope managed by dinistiq it allows for the 
creation of fresh instances with all dependencies filled in from the scope of all beans
collected.

Fire up the wire up
-------------------

Dinistiq scans a given portion of the classpath for classes annotated with JSR330 Annotations. 
It does not introduce any custom annotations.

The missing bits can be configured by a set of properties files, describing

- additional components that should be instanciated
- additional values that should be injected into the instanciated components but cannot be derived from the autoscanned parts

Convention over Configuration
-----------------------------

First of all the most important thing to use dinistiq is to annotate you dependencies with 
JSR @Inject so that dinistiq can find out which components are needed.

```Java
public class TestComponentB {

    @Inject
    public TestInterface test;

} // TestComponentB
```

In the next step dinistiq resolves those components from the auto-scanned portion 
of the classpath where it instanciates all classes annotated with @Singleton. 
Optionally these components may be named with @Named (with an optional name as the 
value parameter). Without a name given as a parameter, components are always named
after their class name without the package name and a decapitalized first letter.

```Java
@Singleton
public class TestComponent implements TestInterface {

} // TestComponent
```

Thus in this example the instanciated bean of class TestComponent will be available 
with the name testComponent. The term "name" is used in this document since it is 
used as the parameter name in the JSR330 annotations. Since names must be unique 
within the scope they are in this case in fact identifiers througout the whole process.

If you are dealing with components of the same type, not only the beans may be named 
but also the injection point might indicate to require a bean with a certain name.

```Java
public class ConfigStuff {

    @Inject
    @Named
    public String filename;

    @Inject
    @Named("prefix")
    public String somePrefix;

} // ConfigStuff
```

In this case, filename is searched as a String component with the name "filename", 
while somePrefix has a specific named annotation with value "prefix".

This complete set-up is done without any configuration for dinistiq itself but only 
for the components to be used.

Optional Configuration with properties files
--------------------------------------------

If this is not enough, you can explicitly add some beans to be instanciated in properties files.

```
unannotatedComponent=dinistiq.test.components.UnannotatedComponent
```

Those files must simply be put in the folder dinistiq/ anywhere on your classpath. 
This example will instanciate the class dinistiq.test.components.UnannotatedComponent 
and store this bean with the name unannotatedComponent in the set of available beans.

The properties files are scanned in alphabetical order, so you can override the class 
for e.g. unannotatedComponent in a latter properties file, so classes given for bean 
names used in mybeans.properties can be overridden in override-mybeans.properties.

For any of the instanciated beans you can provide more values to explicitly inject - again 
by the use of properties files.

After instanciation of the bean a properties file with the bean's name as its base 
filename is searched - first in the dinistiq/defaults/ and then in the dinistiq/beans/ 
folders on the classpath. Thus you can deliver your components with a reasonable 
defaults and necessary overrides for the specific application.

file dinistiq/beans/example.properties
```
activateCaching=true
```

This will call the property setter setActivateCaching() on the bean named example. 
The grammar of the properties files describing the explicit injection supports set
and list type collections, boolean values, numeric value, strings, and references
to other beans.

file dinistiq/beans/example.properties
```
# numerics
intValue=42
longValue=123456789
floatValue=3.14159
doubleValue=2.7
# references
testInterface=${testComponent}
# strings and references in compound strings
replacement=a string
replacementTest=here comes ${replacement}
```

The bean named example is either a result of the automatic discovery of a class named Example

```Java
@Named
@Singleton
public class Example {

} // Example
```

a name declaration from the scanned class

```Java
@Named("example")
@Singleton
public class ExampleComponent  {

} // ExampleComponent
```

or taken from the naming in a configuration properties file

file dinistiq/demo.properties
```
example=some.package.ExampleComponent
```

At the top level, the types of the beans cannot be inferred, as the example

```
unannotatedComponent=dinistiq.test.components.UnannotatedComponent
```

showed. If you need some typical configuration types at this level - like e.g. Strings, 
Booleans, Lists, and Maps some extensions of this mere class based syntax had to be 
introduced. Any simple type found in the java.lang package can be intanciated with a 
value bound to it since these values are immutable and there are thus no modifiable 
fields or setters in these classes.

So

```
booleanValue=java.lang.Boolean("false")
stringValue=java.lang.String("string value")
```

are some examples for this. While

```
mapTest=java.util.Map
```

creates and empty map instance. Like any other beans the contents of this map can 
be modified by a properties file. In this case the contents of the properties file's
key / value pairs will be used as content for the whole map.

Lists of strings can be created by

```
listTest=java.util.List(first,second)
```

How to use
----------

Extend your project with the dependency to the rather small dinistiq library file. 
Dinistiq releases are available from JCenter. The group id and artifact id are both 
'dinistiq'.

Thus for projects built with gradle you will need to add to your repositories sections 
of the build file the line

```
jcenter()
```

if it's not there already and the dependency to the artifact in the dependencies section.

```
compile "dinistiq:dinistiq:0.5-SNAPSHOT"
```

Projects built with Apache Maven need the following steps:

module pom.xml
```xml
...
<dependencies>
  ...
  <dependency>
    <groupId>dinistiq</groupId>
    <artifactId>dinistiq</artifactId>
  </dependency>
  ...
</dependencies>
```

base  pom.xml
```xml
...
<dependencyManagement>
  ...
  <dependency>
    <groupId>dinistiq</groupId>
    <artifactId>dinistiq</artifactId>
    <versions>0.5-SNAPSHOT</version>
  </dependency>
...
</dependencyManagement>

...

<repositories>
  <repository>
    <id>jcenter</id>
    <name>JCenter</name>
    <url>http://jcenter.bintray.com/</url>
  </repository>
</repositories>
...
```

Dinistiq uses slf4j for logging and (still) log4j as an instance for testing.

Snapshot artifacts are available from the OJO repository:

```
https://oss.jfrog.org/
```

Apart from optional configuration files to be placed somehere on your classpath, 
you simply have to tell dinistiq which portion of the classpath to scan for annotations.

```Java
public class Test  {

    public static main(String[] args) {
        Set<String> packages = new HashSet<String>();
        packages.add(Test.class.getPackage().getName());
        Dinistiq d = new Dinistiq(packages);
    } // main()

} // Test
```

Make this portion of the classpath as small as ever possible or point to some invented 
and thus empty package, if you want to avoid scanning.

After this step you can ask dinistiq for instances of the components it created and 
injected.

```Java
public class Test  {

    public static main(String[] args) {
        Set<String> packages = new HashSet<String>();
        packages.add(Test.class.getPackage().getName());
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages);
        } catch (Exception e) {
            //
        } // try/catch
        TestInterface ti = d.findTypedBean(TestInterface.class);
        TestInterface test = d.findBean(TestInterface.class, "test");
        Set<TestInterface> tis = d.findTypedBeans(TestInterface.class);
    } // main()

} // Test
```

Web embedding
-------------

Dinistiq comes with a very lean web integration. An ordered list of beans implementing
the servlet interface will be added to the web application context using injection
and serving web requests.

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
         version="2.5">
  
  <display-name>dinistiq.web</display-name>

  <listener>
    <listener-class>dinistiq.web.DinistiqContextLoaderListener</listener-class>
  </listener>
  <context-param>
    <param-name>dinistiq.packages</param-name>
    <param-value>com.example.components,org.example.components</param-value>
  </context-param>
  
  <welcome-file-list>
    <welcome-file>index.html</welcome-file>
  </welcome-file-list>

</web-app>
```

The context loader listener tries to find the other servlets from the dinistiq context 
by asking for registrable servlets. 

```Java
/**
 * Servlets which should handle requests fulfilling a certain regular expression for their uris.
 */
public interface RegisterableServlet extends Servlet, Comparable<RegisterableServlet> {

    /**
     * Returns a set of url patterns this servlet should be registered for.
     */
    Set<String> getUrlPatterns();

    /**
     * Indicator if the implementing instance should be considered earlier or later 
     * in the servlet selection process.
     */
    int getOrder();

} // RegisterableServlet
```

So a servlet has to tell which url patterns its request should meet to be able to 
handle them. Additionally it tells an order number to sort all available servlets 
to provide a certain precedency rule for them.

Custom Class Resolver
---------------------

It is perfectly possible that you will find our class resolving pretty dumb. So we 
provide the option to pass over a class resolver instance to dinistiq instead of the 
set of package names.

```Java
public class Test  {

    public static main(String[] args) {
        Set<String> packages = new HashSet<String>();
        packages.add(Test.class.getPackage().getName());
        packages.add(Dinistiq.class.getPackage().getName());
        Dinistiq d = null;
        ClassResolver classResolver = new BetterClassResolver(packages);
        try {
            d = new Dinistiq(classResolver);
        } catch (Exception e) {
            //
        } // try/catch
    } // main()

} // Test
```

Be sure to add the package dinistiq in these cases as shown above. Otherwise for obvious 
reasons the properties files from the dinistiq path cannot be found as resources to 
be taken into consideration.

If you want to use custom class resolvers with the web integration you need to implement
a class resolver taking the set of package names as the single parameter to the constructor
and put the name of this implementing class in the context loader listener configuration
for dinistiq.

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
         version="2.5">
  
  <display-name>dinistiq.web</display-name>

  <listener>
    <listener-class>dinistiq.web.DinistiqContextLoaderListener</listener-class>
  </listener>
  <context-param>
    <param-name>dinistiq.packages</param-name>
    <param-value>com.example.components,org.example.components</param-value>
  </context-param>
  <context-param>
    <param-name>dinistiq.class.resolver</param-name>
    <param-value>org.example.dinistiq.BetterClassResolver</param-value>
  </context-param>
  
  <welcome-file-list>
    <welcome-file>index.html</welcome-file>
  </welcome-file-list>

</web-app>
```

Within the web application all beans from the dinistiq scope are available in the
application scope (servlet context) as attributes.

External Components
-------------------

If your software needs to use some components which cannot be instanciated or obtained 
using all of the means presented here, you can pass over a named set of instances as 
a base set of beans for dinistiq to add the scanned and configured beans to.

We use this to e.g. put the servlet context in the set of beans for web integration 
(see below).

```Java
public class DinistiqContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent contextEnvironment) {
        ServletContext context = contextEnvironment.getServletContext();
        Set<String> packages = new HashSet<String>();
        ...
        try {
            Map<String, Object> externalBeans = new HashMap<String, Object>();
            externalBeans.put("servletContext", context);
            Dinistiq dinistiq = new Dinistiq(packages, externalBeans);
            context.setAttribute(DINISTIQ_INSTANCE, dinistiq);
        } catch (Exception ex) {
            LOG.error("init()", ex);
        } // try/catch
    } // contextInitialized()

} // DinistiqContextLoaderListener
```

Besides the one managed Scope
-----------------------------

Dinistiq defines just one scope of beans you can grab beans from. If you need fresh 
instances of beans where the members of this scope should be injected on creation and 
optional post construct methods should be called just following the same rules as 
the beans from the dinistiq scope, you will find a createBeans method besides all 
the options to find existing beans in the scope.

```Java
My myNewInstance = dinistiq.createBean(My.class, null);
```

If this is still no option, you can - like with external beans - provide instances 
externally and let dinistiq still handle their injections and post construct methods.

```Java
My myNewInstance = new My();
dinistiq.initBean(myNewInstance, null);
```

Building
--------

The code for dinistiq is supposed to be written in Java 7 and prepared for building 
with gradle. Gradle versions 1.12 up to 2.4 are tested to be working.

Comparison
----------

The developers of [silk] (http://www.silkdi.com/help/comparison.html) present an 
interesting comparison of some DI implementations done in Java and we want to add 
some  values for dinistiq to this list:

|Library|dinistiq|
|:------|-------:|
|Version|0.4|
|Archive size|<25kB|
|Further dependencies|<=5|
|API||
|Methods in injector/context|3|
|Concept||
|Container Model|flat instances|
|Configuration style|annotation,properties|
|Wiring style|automatic scan|
|Types||
|Generics support|limitted|
|Generic type safety|-|
|Wildcard generics|-|
|Primitive types handling|-|
|Bind to all (generic) supertypes|yes|
|Type link|-|
|Injection||
|Annotation guidance|only JSR330|
|Constructor injection|yes|
|Field injection|yes|
|Setter injection|yes|
|Factory methods|no|
|Static injection|no|
|Method interception|no|
|Providers|(limited)|
|Optional injection|no|
|Mixed injection|?|
|Post construction hook|yes|
|Modularity||
|Arrays|no|
|Collections|partly|
|Multibinds|yes|
|Sequence of declarations|undefined|
|Scopes||
|Default scope|Singleton|
|Custom scopes|-|
|Available scopes|Singleton|
|Error behaviour||
|Dependency cycles|illegal|
|Detection of a cyclic dependencies error|runtime|

The closest competitor of dinistiq seems to be TinyDI - https://code.google.com/p/tinydi/. 
It recognises JSR330 Annotation but seems to lack the option of config files like the 
simple properties file mechanism of dinistiq. Additionally - unlike Spring and dinistiq - 
it depends on public setters for the injections. Private members with the @Inject annotation
are not enough. Also it is fairly unmaintained for some years now.
