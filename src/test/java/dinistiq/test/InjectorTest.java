/**
 *
 * Copyright 2013-2014 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package dinistiq.test;

import dinistiq.Dinistiq;
import dinistiq.test.components.CollectionReferences;
import dinistiq.test.components.ConstructorInjection;
import dinistiq.test.components.InitialBean;
import dinistiq.test.components.InitialBeanDependentComponent;
import dinistiq.test.components.MultiInstanceComponent;
import dinistiq.test.components.NamedInjection;
import dinistiq.test.components.NumericInjection;
import dinistiq.test.components.TestComponent;
import dinistiq.test.components.TestComponentB;
import dinistiq.test.components.TestInterface;
import dinistiq.test.components.UnannotatedComponent;
import dinistiq.web.test.MockServletContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.junit.Assert;
import org.junit.Test;


public class InjectorTest {

    private Dinistiq d;

    private final Set<String> packages;


    public static Map<String, Object> prepareInitialBeans() {
        Map<String, Object> initialBeans = new HashMap<>();
        initialBeans.put("initialBean", new InitialBean());
        initialBeans.put("servetContext", new MockServletContext(null));
        return initialBeans;
    } // prepareInitialBeans()


    public InjectorTest() {
        packages = new HashSet<>();
        packages.add(TestInterface.class.getPackage().getName());
        packages.add(Car.class.getPackage().getName());
        try {
            d = new Dinistiq(packages, prepareInitialBeans());
        } catch (Exception e) {
            Assert.fail(e.getClass().getSimpleName()+": "+e.getMessage());
            Assert.assertNotNull("DI container could not be initialized", d);
        } // try/catch
    } // InjectorTest()


    @Test
    public void testFindImplementedInterface() {
        Assert.assertNotNull("Cannot find instanciated component for interface", d.findBean(TestInterface.class));
    } // testFindImplementedInterface()


    @Test
    public void testFindExplicitlyInstanciatedComponent() {
        TestInterface ti = d.findBean(TestInterface.class);
        Assert.assertNotNull("Cannot find test interface instance just by type ", ti);
        TestInterface test = d.findBean(TestInterface.class, "test");
        Assert.assertNull("Should not be able to find test interface instance by type and name", test);
        Set<TestInterface> tis = d.findBeans(TestInterface.class);
        Assert.assertNotNull("Cannot find test interface set", tis);
        Assert.assertNotNull("Cannot find instance of un-annotated component mentioned in config file ", d.findBean(UnannotatedComponent.class));
    } // testFindExplicitlyInstanciatedComponent()


    @Test
    public void testFindNames() {
        Set<String> tis = d.findNames(TestInterface.class);
        Assert.assertNotNull("Cannot obtain name set", tis);
        Assert.assertEquals("Cannot find expected number of bean names", 1, tis.size());
        Assert.assertEquals("Cannot find expected bean name", "testComponent", tis.iterator().next());
        Set<String> negative = d.findNames(InjectorTest.class);
        Assert.assertNotNull("Cannot obtain name set", negative);
        Assert.assertEquals("Cannot find expected number of bean names", 0, negative.size());
    } // testFindNames()


    @Test
    public void testDontFind() {
        String negativeTest = d.findBean(String.class, "unannotatedComponent");
        Assert.assertNull("Type conversion should not have been possible", negativeTest);
        Set<InjectorTest> negative = d.findBeans(InjectorTest.class);
        Assert.assertNotNull("Cannot obtain instance set", negative);
        Assert.assertEquals("Found unexpected instances", 0, negative.size());
        InjectorTest notAvailable = d.findBean(InjectorTest.class);
        Assert.assertNull("Cannot obtain instance set", notAvailable);
    } // testDontFind()


    @Test
    public void testNamedInjection() {
        TestComponentB testComponentB = d.findBean(TestComponentB.class);
        Assert.assertNotNull("need TestComponentB instance", testComponentB);
        Assert.assertEquals("Cannot find string values as expection ", "stringValue", testComponentB.getHallo());
    } // testNamedInjection()


    @Test
    public void testReferenceValue() {
        UnannotatedComponent withoutName = d.findBean(UnannotatedComponent.class);
        Assert.assertNotNull("Cannot find instance of un-annotated component mentioned in config file ", withoutName);
        // In this case in order to let the reference injection work, the bean MUST be named to find the correct properties file
        UnannotatedComponent unannotatedComponent = d.findBean(UnannotatedComponent.class, "unannotatedComponent");
        Assert.assertNotNull("Cannot find instance of un-annotated component mentioned in config file ", unannotatedComponent);
        Assert.assertNotNull("Cannot find auto-injected value ", unannotatedComponent.getAutoInjected());
        Assert.assertNotNull("Cannot find value injected as a reference ", unannotatedComponent.getTestInterface());
        // Check that bean cannot be found with different name
        UnannotatedComponent notFound = d.findBean(UnannotatedComponent.class, "notFound");
        Assert.assertNull("Bean should be not found by that name", notFound);
    } // testReferenceValue()


    @Test
    @SuppressWarnings("rawtypes")
    public void testMapBeans() {
        Set<Map> maps = d.findBeans(Map.class);
        Assert.assertTrue("no maps at all found", maps.size()>0);
        Map map = d.findBean(Map.class, "mapTest");
        Assert.assertNotNull("test map not found", map);
        Assert.assertEquals("default value not correct", "defaultValueA", map.get("keyA"));
        Assert.assertEquals("specialized value not correct", "overriddenValueB", map.get("keyB"));
    } // testMapBeans()


    @Test
    @SuppressWarnings("rawtypes")
    public void testLiteralCollections() {
        Set<List> lists = d.findBeans(List.class);
        Assert.assertTrue("no lists at all found", lists.size()>0);
        List list = d.findBean(List.class, "listTest");
        Assert.assertNotNull("test list not found", list);
        Assert.assertEquals("first value not correct", "first", list.get(0));
        Assert.assertEquals("second value not correct", "second", list.get(1));
        
        Set<Set> sets = d.findBeans(Set.class);
        Assert.assertTrue("no sets at all found", sets.size()>0);
        Set set = d.findBean(Set.class, "setTest");
        Assert.assertNotNull("test set not found", set);
        Assert.assertEquals("set should contain two elements", 2, set.size());
        Assert.assertEquals("first value not correct", "second", set.iterator().next());
        
        CollectionReferences cr = d.findBean(CollectionReferences.class);
        Assert.assertNotNull("no collection references object found", cr);
        Assert.assertEquals("referenced set should contain two elements", 2, cr.getStringSet().size());
        Assert.assertEquals("first value not correct in referenced set", "second", cr.getStringSet().iterator().next());
    } // testLiteralCollections()


    @Test
    public void testStringValue() {
        String stringValue = d.findBean(String.class, "stringTest");
        Assert.assertNotNull("not string with name 'stringTest' found", stringValue);
        Assert.assertEquals("unexpected string value", "stringValue", stringValue);
    } // testStringValue()


    @Test
    public void testInitialBeans() {
        InitialBean ib = d.findBean(InitialBean.class);
        Assert.assertNotNull("Bean from externally provided initial map of beans not found", ib);
        TestInterface t = ib.getTest();
        Assert.assertNotNull("Initial bean is missing injected dependency", t);
        InitialBeanDependentComponent ibd = d.findBean(InitialBeanDependentComponent.class);
        Assert.assertNotNull("Bean from depending on externally provided bean not found", ibd);
    } // testInitialBeans()


    @Test
    public void testNamedInjections() {
        NamedInjection ni = d.findBean(NamedInjection.class);
        Assert.assertNotNull("Named injection test component not found", ni);
        Assert.assertEquals("Named inection with default name failed", "stringValue", ni.getStringTest());
        Assert.assertEquals("Named inection with passed name failed", "a string value", ni.getStringValue());
        Assert.assertEquals("Named inection with default name failed", "This is a direct value", ni.getDirectValue());
        Assert.assertEquals("Named inection with passed name failed", "a string value", ni.getNamedValue());
        Assert.assertEquals("Named inection with passed name failed", "a string value", ni.getSomeValue());
    } // testNamedInjections()


    @Test
    public void testBooleans() {
        Map<String, Object> initialBeans = prepareInitialBeans();
        initialBeans.put("booleanBasetypeValueTrue", true);
        initialBeans.put("booleanTypedValueTrue", Boolean.TRUE);
        initialBeans.put("booleanBasetypeValueFalse", false);
        initialBeans.put("booleanTypedValueFalse", Boolean.FALSE);
        Dinistiq di = null;
        try {
            di = new Dinistiq(packages, initialBeans);
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
        Assert.assertNotNull("DI container could not be initialized", di);
        Assert.assertTrue("Boolean string based value cannot be set to true", di.findBean(Boolean.class, "booleanStringValueTrue"));
        Assert.assertFalse("Boolean string based value cannot be set to false", di.findBean(Boolean.class, "booleanStringValueFalse"));
        Assert.assertTrue("Boolean base type value cannot be set to true", di.findBean(Boolean.class, "booleanBasetypeValueTrue"));
        Assert.assertFalse("Boolean base type value cannot be set to false", di.findBean(Boolean.class, "booleanBasetypeValueFalse"));
        Assert.assertTrue("Boolean typed value cannot be set to true", di.findBean(Boolean.class, "booleanTypedValueTrue"));
        Assert.assertFalse("Boolean typed value cannot be set to false", di.findBean(Boolean.class, "booleanTypedValueFalse"));

        UnannotatedComponent unannotatedComponent = di.findBean(UnannotatedComponent.class);
        Assert.assertNotNull("Cannot find instance of un-annotated component mentioned in config file ", unannotatedComponent);
        Assert.assertTrue("Boolean value could not be referenced in other bean", unannotatedComponent.isBasetypeBooleanValue());
    } // testBooleans()


    @Test
    @SuppressWarnings("unchecked")
    public void testStringReplacement() {
        Map<Object, Object> map = d.findBean(Map.class, "mapTest");
        Assert.assertNotNull("test map not found", map);
        // test if system properties can be used
        Assert.assertNotNull("no os name found", map.get("osName"));
        Assert.assertNotEquals("os name must be given", "__UNKNOWN__", map.get("osName"));
        Assert.assertEquals("pattern not replaced as expected", "here comes a string value (a string value)", map.get("replacementTest"));
    } // testStringReplacement()


    @Test
    public void testStaticInjection() {
        Assert.assertNotNull("Static field not injected", TestComponentB.getTestInterface());
    } // testStaticInjection()


    @Test
    public void testNumericInjection() {
        NumericInjection numerics = d.findBean(NumericInjection.class);
        Assert.assertEquals("Failure in injection of int value", 42, numerics.getIntValue());
        Assert.assertEquals("Failure in injection of long value", 123456789, numerics.getLongValue());
        Assert.assertEquals("Failure in injection of float value", 3.14159, numerics.getFloatValue(), numerics.getFloatValue()-3.14159);
        Assert.assertEquals("Failure in injection of double value", 2.7, numerics.getDoubleValue(), numerics.getDoubleValue()-2.7);
        Assert.assertEquals("Failure in injection of boolean value", true, numerics.isBooleanValue());
    } // testNumericInjection()


    @Test
    public void testCollectionInjection() {
        TestComponentB cb = d.findBean(TestComponentB.class);
        UnannotatedComponent uac = d.findBean(UnannotatedComponent.class);
        Assert.assertNotNull("No collection of instances available", cb.getAllInstances());
        Assert.assertEquals("Wrong number of instaces in collection", 1, cb.getAllInstances().size());
        Assert.assertNotNull("No collection of instances available", uac.getManuallyInjectedCollection());
        Assert.assertEquals("Wrong number of instaces in collection", 1, uac.getManuallyInjectedCollection().size());
    } // testConstructorInjection()


    @Test
    public void testAnnotationLookup() {
        Collection<Object> beans = d.findAnnotatedBeans(Singleton.class);
        Assert.assertEquals("Unexpected number of annotated beans in scope", 10, beans.size());
    } // testAnnotationLookup()


    @Test
    public void testConstructorInjection() {
        ConstructorInjection constructorInjection = d.findBean(ConstructorInjection.class);
        Assert.assertEquals("Failure in injection of numeric value", "a string value", constructorInjection.getString());
    } // testConstructorInjection()


    @Test
    public void testInstanceCreation() {
        MultiInstanceComponent instance = d.createBean(MultiInstanceComponent.class, null);
        TestComponent testComponent = d.findBean(TestComponent.class);
        Assert.assertNotNull("Component didn't get injected", instance.getTestComponent());
        Assert.assertEquals("Non expected instance injected", testComponent, instance.getTestComponent());
        Assert.assertEquals("Non expected name of fresh instance", "default", instance.getName());
        MultiInstanceComponent secondInstance = d.createBean(MultiInstanceComponent.class, "fresh");
        Assert.assertNotNull("Component didn't get injected", secondInstance.getTestComponent());
        Assert.assertEquals("Non expected instance injected", testComponent, secondInstance.getTestComponent());
        Assert.assertEquals("Non expected name of fresh instance", "overridden", secondInstance.getName());
    } // testInstanceCreation()


    @Test
    public void testInstanceInit() {
        MultiInstanceComponent instance = new MultiInstanceComponent();
        d.initBean(instance, null);
        Assert.assertEquals("Non expected name of fresh instance", "default", instance.getName());
        Assert.assertNotNull("Component didn't get injected", instance.getTestComponent());
        TestComponent testComponent = d.findBean(TestComponent.class);
        Assert.assertEquals("Non expected instance injected", testComponent, instance.getTestComponent());
    } // testInstanceInit()


    @Test
    public void testUsingTck() {
        Car car = d.findBean(Car.class);
        Assert.assertNotNull("Tck's car should have been instanciated", car);
        Tck.testsFor(car, true, true);
    } // testUsingTck()

} // InjectorTest
