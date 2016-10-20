![dinistiq](https://raw.github.com/mgoellnitz/dinistiq/master/doc/dinistiq.png)

# Minimalistic Dependency Injection

[![Latest Release](https://img.shields.io/github/release/mgoellnitz/dinistiq.svg)](https://github.com/mgoellnitz/dinistiq/releases/latest)
[![Build Status](https://api.travis-ci.org/mgoellnitz/dinistiq.svg?branch=master)](https://travis-ci.org/mgoellnitz/dinistiq)
[![Coverage Status](https://coveralls.io/repos/github/mgoellnitz/dinistiq/badge.svg?branch=master)](https://coveralls.io/github/mgoellnitz/dinistiq?branch=master)
[![Coverage Status](http://codecov.io/github/mgoellnitz/dinistiq/coverage.svg?branch=master)](https://codecov.io/gh/mgoellnitz/dinistiq)
[![Dependency Status](https://www.versioneye.com/user/projects/54ff710b4a10649b1b000053/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ff710b4a10649b1b000053)

A small footprint approach to dependency injection with a framework or container
implemented in Java.

Or: What I got wrong about DI

Minimalistic library to use dependency injection for the wire-up of software 
components. It thus mostly deals with singletons - some of them implementing 
interfaces - which should be injected as dependencies into one another taking 
into account their  name, qualifier.

As the only other option besides the single scope managed by dinistiq it allows 
for the creation of fresh instances with all dependencies filled in from the 
scope of all beans collected.

## Fire up the wire up

Dinistiq scans a given portion of the classpath for classes annotated with 
JSR330 Annotations. It does not introduce any custom annotations.

The missing bits can be configured by a set of properties files, describing

- additional components that should be instanciated
- additional values that should be injected into the instanciated components but cannot be derived from the autoscanned parts

## Convention over Configuration

First of all the most important thing to use dinistiq is to annotate your 
dependencies with JSR @Inject so that dinistiq can find out which components are 
needed.

```Java
public class TestComponentB {

    @Inject
    public TestInterface test;

} // TestComponentB
```

In the next step dinistiq resolves those components from the auto-scanned 
portion of the classpath where it instanciates all classes annotated with 
@Singleton. Optionally these components may be named with @Named (with an 
optional name as the value parameter). Without a name given as a parameter, 
components are always named after their class name without the package name and 
a decapitalized first letter.

```Java
@Singleton
public class TestComponent implements TestInterface {

} // TestComponent
```

Thus in this example the instanciated bean of class TestComponent will be 
available with the name testComponent. The term "name" is used in this document 
since it is used as the parameter name in the JSR330 annotations. Since names 
must be unique within the scope they are in this case in fact identifiers 
througout the whole process.

If you are dealing with components of the same type, not only the beans may be 
named but also the injection point might indicate to require a bean with a 
certain name.

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

This complete set-up is done without any configuration for dinistiq itself but 
only for the components to be used.

## Configuration through annotations

JSR330 specifies the concept of qualifiers to select which implementation of
an interface is to be chosen. Unfortunately this means, that the injection
point defines which implementation is chosen in the Java code. So we recommend
not to use this in your code but add configuration files (see below) to control
the selection of implementing classes outside the code.

You may defined annotations describing qualifiers

```Java
@Qualifier
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestQualifier {
} // TestQualifier
```

and these qualifiers may be used to define injection criteria at the injection
points.

```Java
public class QualifiedInjection {

    @Inject
    @TestQualifier
    private TestInterface testInterface;

} // QualifiedInjection
```

Only implementation annotated with the given qualifier are take into account
when performing the injection.

```Java
@Singleton
@TestQualifier
public class QualifiedComponent implements TestInterface {

} // QualifiedComponent
```


## Optional Configuration with properties files

If this is not enough, you can explicitly add some beans to be instanciated in 
properties files.

```
unannotatedComponent=dinistiq.test.components.UnannotatedComponent
```

Those files must simply be put in the folder dinistiq/ anywhere on your classpath.
This example will instanciate the class dinistiq.test.components.UnannotatedComponent
and store this bean with the name unannotatedComponent in the set of available 
beans.

The properties files are scanned in alphabetical order, so you can override the 
class for e.g. unannotatedComponent in a latter properties file, so classes given 
for bean names used in mybeans.properties can be overridden in override-mybeans.properties.

For any of the instanciated beans you can provide more values to explicitly 
inject - again by the use of properties files.

After instanciation of the bean a properties file with the bean's name as its 
base filename is searched - first in the dinistiq/defaults/ and then in the 
dinistiq/beans/ folders on the classpath. Thus you can deliver your components 
with a reasonable defaults and necessary overrides for the specific application.

file dinistiq/beans/example.properties
```
activateCaching=true
```

This will call the property setter setActivateCaching() on the bean named 
example. The grammar of the properties files describing the explicit injection 
supports set and list type collections, boolean values, numeric value, strings, 
and references to other beans.

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

The bean named example is either a result of the automatic discovery of a class 
named Example

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

showed. If you need some typical configuration types at this level - like e.g. 
Strings, Booleans, Lists, and Maps some extensions of this mere class based 
syntax had to be introduced. Any simple type found in the java.lang package can 
be intanciated with a value bound to it since these values are immutable and 
there are thus no modifiable fields or setters in these classes.

So

```
booleanValue=java.lang.Boolean("false")
stringValue=java.lang.String("string value")
integerValue=java.lang.Integer(42)
```

are some examples for this. While

```
mapTest=java.util.Map
```

creates and empty map instance. Like any other beans the contents of this map can
be modified by a properties file. In this case the contents of the properties 
file's key / value pairs will be used as content for the whole map.

Lists of strings can be created by

```
listTest=java.util.List(first,second)
```

## How to use

Extend your project with the dependency to the rather small dinistiq library 
file. Dinistiq releases are available from JCenter. The group id and artifact id 
are both 'dinistiq'.

Thus for projects built with gradle you will need to add to your repositories 
sections of the build file the line

```
jcenter()
```

if it's not there already and the dependency to the artifact in the dependencies 
section.

```
compile "dinistiq:dinistiq:0.6"
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
    <versions>0.6</version>
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

Dinistiq uses slf4j for logging and logback as an instance for testing.

Snapshot artifacts are available from the OJO repository:

```
https://oss.jfrog.org/oss-snapshot-local/
```

Apart from optional configuration files to be placed somehere on your classpath,
you simply have to tell dinistiq which portion of the classpath to scan for 
annotations.

```Java
public class Test  {

    public static main(String[] args) {
        Set<String> packages = new HashSet<String>();
        packages.add(Test.class.getPackage().getName());
        Dinistiq d = new Dinistiq(packages);
    } // main()

} // Test
```

Make this portion of the classpath as small as ever possible or point to some 
invented and thus empty package, if you want to avoid scanning.

After this step you can ask dinistiq for instances of the components it created 
and injected.

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

## Web embedding

Dinistiq comes with a very lean web integration. An ordered list of beans 
implementing the servlet interface will be registered directly with the servlet 
container.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">

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

The context loader listener tries to find the servlets from the dinistiq context 
by asking for RegisterableServlet instances.

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

## Custom Class Resolver

It is perfectly possible that you will find our class resolving pretty dumb. So 
we provide the option to pass over a class resolver instance to dinistiq instead 
of the set of package names.

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

Be sure to add the package dinistiq in these cases as shown above. Otherwise for 
obvious reasons the properties files from the dinistiq path cannot be found as 
resources to be taken into consideration.

If you want to use custom class resolvers with the web integration you need to 
implement a class resolver taking the set of package names as the single parameter 
to the constructor and put the name of this implementing class in the context 
loader listener configuration for dinistiq.

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">

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

## External Components

If your software needs to use some components which cannot be instanciated or 
obtained using all of the means presented here, you can pass over a named set of 
instances as a base set of beans for dinistiq to add the scanned and configured 
beans to.

We use this to e.g. put the servlet context in the set of beans for web 
integration (see below).

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

## Besides the one managed Scope

Dinistiq defines just one scope of beans you can grab beans from. If you need 
fresh instances of beans where the members of this scope should be injected on 
creation and optional post construct methods should be called just following the 
same rules as the beans from the dinistiq scope, you will find a createBeans 
method besides all the options to find existing beans in the scope.

```Java
My myNewInstance = dinistiq.createBean(My.class, null);
```

If this is still no option, you can - like with external beans - provide instances
externally and let dinistiq still handle their injections and post construct 
methods.

```Java
My myNewInstance = new My();
dinistiq.initBean(myNewInstance, null);
```

## Building

While dinistiq 0.4 happily works with Java 8, only dinistiq 0.5 and up can be 
compiled and tested with Java 8.

The code for dinistiq is prepared for building with Gradle. Gradle 2 versions up 
to 2.14.1 are tested to be working, while dinistiq starting from version 0.7
at least needs Gradle 2.12.

Up to dinistiq 0.5 the code is supposed to be written in Java 7 with a subsequent
switch to Java 8.

|dinistiq Version|Works with |Compiles with|GAE support|
|:--------------:|:---------:|:-----------:|:---------:|
|0.4|Java 7 / 8|Java 7|+|
|0.5|Java 7 / 8|Java 7 / 8|-|
|0.6|Java 8|Java 8|-|
|0.7|Java 8|Java 8|-|

## Comparison

The developer of [SilkDI](https://github.com/jbee/silk/blob/6a739b44973de964013d320c174a333e2f70665c/help/comparison.md) presents an
interesting comparison of some DI implementations done in Java and we want to add
some  values for dinistiq to this list:

|Library|dinistiq|
|:------|-------:|
|Version|0.7|
|Archive size|24kB|
|Further dependencies|3|
|API||
|Methods in injector/context|10|
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
|Static injection|yes|
|Method interception|no|
|Providers|(limited)|
|Optional injection|yes|
|Mixed injection|?|
|Post construction hook|yes|
|Modularity||
|Arrays|no|
|Collections|partly|
|Multibinds|yes|
|Sequence of declarations|undefined|
|Scopes|limited|
|Default scope|Singleton|
|Custom scopes|-|
|Available scopes|Singleton|
|Error behaviour||
|Dependency cycles|illegal|
|Detection of a cyclic dependencies error|runtime|

The closest competitor of dinistiq seems to be TinyDI - https://code.google.com/p/tinydi/.
It recognises JSR330 Annotation but seems to lack the option of config files like 
the simple properties file mechanism of dinistiq. Additionally - unlike Spring 
and dinistiq - it depends on public setters for the injections. Private members 
with the @Inject annotation are not enough. Also it is fairly unmaintained for 
some years now.

Another option I ran into is [Feather](https://github.com/zsoltherpai/feather)
described in [this article](http://codejargon.blogspot.no/2015/09/feather-ultra-lightweight-dependency.html).
It lacks too many injection options to be usefull for the injection scenarios 
presented here like injecting after instanciation with a @PostConstruct method 
to complete initialization.

## History and Why

I rather apologise to introduce another Dependency Injection Container for the 
Java world - dinistiq - a very minimalistic approach to the topic. It turned out 
to be easier to implement another one, than to use others listed here. Limited 
in features, easy to use, and still more configurable than other options I could 
think of. After some months of use, I now can invite other users to take a look 
at it and try it in their own projects.

Also this text gives you a "why" on the use of the JSR 330 annotations for Dependency 
Injection. It simply makes your code even more reusable in case your development 
or deployment environment changes.

Since tangram is much more about glueing together proven existing software 
components and frameworks than writing code, I felt the need to check if the 
existing code base was really fully dependent on the Spring Framework.

Despite the fact that spring more or less in many ways does what I need, it 
sometimes feels a bit bloated and does too much magic I don't understand in 
detail (which I still had to learn when debugging things). So I tried to isolate 
the spring code during the tangram 0.9 work and present at least a second 
solution for all the things I did with spring so far.

For tangram spring does three things

- Dependency Injection to plug the whole application together
- support a decent view layer with JSP and Apache Velocity views
- A concise way to map http requests to code - controller classes or methods

So I took a look at other view frameworks like Vaadin, GWT, Apache Wicket, Play, 
Struts, JSF/JEE, Stripes. Right at the moment I think Vaading, GWT, Wicket, and 
Play are no really good fit for tangram, Struts in my eyes is a fading technology, 
and only JSF/JEE is an obvious option. With Java Server Faces I only had 
unsatisfying project experiences and the rest of JEE goes for plain Servlet. So 
tangram had to be provided with a plain servlet way of doing the view layer.

Since the modularity of tangram was achieved by the Spring way of plugging 
components together with Dependency Injection, the first thing to do was, to mark 
the generic components in a spring independent way and to look at the other 
options for the Dependency Injection part. Only then it would be possible to 
replace the spring view layer with a servlet view layer during the startup and 
wire-up of the application.

So the list of relevant DI frameworks gets shortened to those supporting the 
generic Dependency Injection annotations from JSR330 which are intended for JEE 
and can e.g. also be used with Google Guice and the Spring Framework alike.

From the reading Google Guice seemed to be a good alternative for the proof of 
concept phase, but it took me that much work to get something to run with it 
(not everything can be plugged together programmatically in my case), that I 
came out faster with my own Dependency Injection Container. Rather minimalistic 
and only suited for the setup of components.
result
Its advantage over Guice is that it's smaller and easier configurable with 
properties files. Weeks later I discovered TinyDI as another option. While this 
container seems to be a lot cleverer about the search of annotated classes it 
seems to lack the needed option of extending the configuration aspects from the 
annotations with properties files - defaults and overridden values and references.
