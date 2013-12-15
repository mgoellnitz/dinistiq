![dinistiq](https://raw.github.com/mgoellnitz/dinistiq/master/doc/dinistiq.png)

Minimalistic Dependency Injection
=================================

[![Build Status](https://buildhive.cloudbees.com/job/mgoellnitz/job/dinistiq/badge/icon)](https://buildhive.cloudbees.com/job/mgoellnitz/job/dinistiq/)

Or: What I got wrong about DI

Minimalistic component to use dependency injection for the wire-up of software components. It thus mostly deals with singletons - some of the implementing interfaces - which should be injected as dependencies into one another.

Fire up the wire up
-------------------

Dinistiq scans a given portion of the classpath for classes annotated with JSR330 Annotations. It does not introduce any custom annotations.

The missing bits can be configured by a set of property files, describing

- additional components that should be instancianted
- additional values that should be injected into the instancianted components but cannot be taken from the autoscanned

Convention over Configuration
-----------------------------

First of all the most important thing to use dinistiq is, to annotate you dependencies with JSR @Inject so that dinistiq can find out which components are needed.

```Java
public class TestComponentB {

    @Inject
    public TestInterface test;

} // TestComponentB
```

It then finds those components from the auto-scanned portion of the classpath where it instanciates all classes annotated with @Named (and an optional name as the value parameter).

```Java
@Named
public class TestComponent implements TestInterface {

} // TestComponent
```

If this is not enough, you can explicitly add some beans to be instaciated in properties files

```
unannotatedComponent=dinistiq.test.components.UnannotatedComponent
```

Those files must simply be put in the folder dinistiq/ anywhere on your classpath.

For any of the instanciated beans you can provide more values to explicitly inject - again by the use of properties files.

After instanciation of the bean a properties file with the bean name as its base file name is searched first in the dinistiq/defaults/ and then in the dinistiq/beans/ folders on the classpath. Thus you can deliver your components with a reasonable default and necessary overrides for the specific application.

file dinistiq/beans/example.properties
```
activateCaching=true
```

This will call the property setter set setActivateCaching() on the bean named example. The bean named example comes either from automatic scan of a class named Example

```Java
@Named
public class Example {

} // Example
```

a name declaration from the scanned class

```Java
@Named("example")
public class ExampleComponent  {

} // ExampleComponent
```

or from the naming through a configuration properties file

file dinistiq/demo.properties
```
example=some.package.ExampleComponent
```

This complete set up is done without any configuration for dinistiq itself.

How to use
----------

Apart from optional configuration files to be placed somehere on your classpath, you simply have to tell dinistiq which portion of the classpath to scan for annotations.

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
    } // main()

} // Test
```

Make this portion of the classpath as small as ever possible or point to some invented and thus empty package, if you want to avoid scanning.

After this step you can ask dinistiq for instances of the components it created and injected.

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

dinistiq comes with a very lean web integration with a front controller servlet with which other servlets implemented as components can be registered.

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
         version="2.5">
  
  <display-name>dinistiq.web</display-name>

  <servlet>
    <servlet-name>dinistiq</servlet-name>
    <servlet-class>dinistiq.web.DinistiqServlet</servlet-class>
    <init-param>
      <param-name>dinistiq.packages</param-name>
      <param-value>com.example.components,org.example.components</param-value>
    </init-param>
  </servlet>
  <servlet-mapping>
    <servlet-name>dinistiq</servlet-name>
    <url-pattern>/d/*</url-pattern>
  </servlet-mapping>
  
  <welcome-file-list>
    <welcome-file>index.html</welcome-file>
  </welcome-file-list>

</web-app>
```

This front controller servlet tries to find the other servlets from the dinistiq context by asking for registrable servlets. 

```Java
/**
 * Servlets which should handle requests fulfilling a certain regular expression for their uris.
 */
public interface RegisterableServlet extends Servlet, Comparable<RegisterableServlet> {

    /**
     * Returns a set of regular expression of which the calling URI must adhere one so that this servlet should handle it.
     */
    Set<String> getUriRegex();

    /**
     * returns an integer indicating if the implementing instance should be considered earlier or later in
     * the servlet selection process.
     */
    int getOrder();

} // RegisterableServlet
```

So a servlet has to tell which regular expressions its request should meet to be able to handle them. Additionally it tells an order number to sort all available servlets to provide a certain precedency rule for them.

Note: Since / is such a common character in URLs and regular exressions need to escape exactly this character, you must pass the / unescaped as it gets auto-escaped by dinistiq.

Comparison
----------

The developers of [silk] (http://www.silkdi.com/help/comparison.html) present an interesting comparison of some DI implementations done in Java and we want to add some values for dinistiq to this list:

|Library|dinistiq|
|:------|-------:|
|Version|0.1|
|Archive size|<20kB|
|Further dependencies|<5|
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
|Constructor injection|no|
|Field injection|yes|
|Setting injection|yes|
|Factory methods|no|
|Static injection|no|
|Method interception|no|
|Providers|no|
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
