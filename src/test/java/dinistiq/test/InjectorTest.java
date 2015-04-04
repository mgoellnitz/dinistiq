/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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
import org.testng.Assert;
import org.testng.annotations.Test;


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
            Assert.assertNotNull(d, "DI container could not be initialized");
        } // try/catch
    } // InjectorTest()


    @Test
    public void testFindImplementedInterface() {
        Assert.assertNotNull(d.findBean(TestInterface.class), "Cannot find instanciated component for interface");
    } // testFindImplementedInterface()


    @Test
    public void testFindExplicitlyInstanciatedComponent() {
        TestInterface ti = d.findBean(TestInterface.class);
        Assert.assertNotNull(ti, "Cannot find test interface instance just by type ");
        TestInterface test = d.findBean(TestInterface.class, "test");
        Assert.assertNull(test, "Should not be able to find test interface instance by type and name");
        Set<TestInterface> tis = d.findBeans(TestInterface.class);
        Assert.assertNotNull(tis, "Cannot find test interface set");
        Assert.assertNotNull(d.findBean(UnannotatedComponent.class), "Cannot find instance of un-annotated component mentioned in config file ");
    } // testFindExplicitlyInstanciatedComponent()


    @Test
    public void testFindNames() {
        Set<String> tis = d.findNames(TestInterface.class);
        Assert.assertNotNull(tis, "Cannot obtain name set");
        Assert.assertEquals(1, tis.size(), "Cannot find expected number of bean names");
        Assert.assertEquals("testComponent", tis.iterator().next(), "Cannot find expected bean name");
        Set<String> negative = d.findNames(InjectorTest.class);
        Assert.assertNotNull(negative, "Cannot obtain name set");
        Assert.assertEquals(0, negative.size(), "Cannot find expected number of bean names");
    } // testFindNames()


    @Test
    public void testDontFind() {
        String negativeTest = d.findBean(String.class, "unannotatedComponent");
        Assert.assertNull(negativeTest, "Type conversion should not have been possible");
        Set<InjectorTest> negative = d.findBeans(InjectorTest.class);
        Assert.assertNotNull(negative, "Cannot obtain instance set");
        Assert.assertEquals(0, negative.size(), "Found unexpected instances");
        InjectorTest notAvailable = d.findBean(InjectorTest.class);
        Assert.assertNull(notAvailable, "Cannot obtain instance set");
    } // testDontFind()


    @Test
    public void testNamedInjection() {
        TestComponentB testComponentB = d.findBean(TestComponentB.class);
        Assert.assertNotNull(testComponentB, "need TestComponentB instance");
        Assert.assertEquals("stringValue", testComponentB.getHallo(), "Cannot find string values as expection");
    } // testNamedInjection()


    @Test
    public void testReferenceValue() {
        UnannotatedComponent withoutName = d.findBean(UnannotatedComponent.class);
        Assert.assertNotNull(withoutName, "Cannot find instance of un-annotated component mentioned in config file ");
        // In this case in order to let the reference injection work, the bean MUST be named to find the correct properties file
        UnannotatedComponent unannotatedComponent = d.findBean(UnannotatedComponent.class, "unannotatedComponent");
        Assert.assertNotNull(unannotatedComponent, "Cannot find instance of un-annotated component mentioned in config file ");
        Assert.assertNotNull(unannotatedComponent.getAutoInjected(), "Cannot find auto-injected value ");
        Assert.assertNotNull(unannotatedComponent.getTestInterface(), "Cannot find value injected as a reference ");
        // Check that bean cannot be found with different name
        UnannotatedComponent notFound = d.findBean(UnannotatedComponent.class, "notFound");
        Assert.assertNull(notFound, "Bean should be not found by that name");
    } // testReferenceValue()


    @Test
    @SuppressWarnings("rawtypes")
    public void testMapBeans() {
        Set<Map> maps = d.findBeans(Map.class);
        Assert.assertTrue(maps.size()>0, "no maps at all found");
        Map map = d.findBean(Map.class, "mapTest");
        Assert.assertNotNull(map, "test map not found");
        Assert.assertEquals("defaultValueA", map.get("keyA"), "default value not correct");
        Assert.assertEquals("overriddenValueB", map.get("keyB"), "specialized value not correct");
    } // testMapBeans()


    @Test
    @SuppressWarnings("rawtypes")
    public void testLiteralCollections() {
        Set<List> lists = d.findBeans(List.class);
        Assert.assertTrue(lists.size()>0, "no lists at all found");
        List list = d.findBean(List.class, "listTest");
        Assert.assertNotNull(list, "test list not found");
        Assert.assertEquals("first", list.get(0), "second value not correct");
        Assert.assertEquals("second", list.get(1), "second value not correct");

        Set<Set> sets = d.findBeans(Set.class);
        Assert.assertTrue(sets.size()>0, "no sets at all found");
        Set set = d.findBean(Set.class, "setTest");
        Assert.assertNotNull(set, "test set not found");
        Assert.assertEquals(2, set.size(), "set should contain two elements");
        Assert.assertEquals("second", set.iterator().next(), "first value not correct");

        CollectionReferences cr = d.findBean(CollectionReferences.class);
        Assert.assertNotNull(cr, "no collection references object found");
        Assert.assertEquals(2, cr.getStringSet().size(), "referenced set should contain two elements");
        Assert.assertEquals("second", cr.getStringSet().iterator().next(), "first value not correct in referenced set");
    } // testLiteralCollections()


    @Test
    public void testStringValue() {
        String stringValue = d.findBean(String.class, "stringTest");
        Assert.assertNotNull(stringValue, "not string with name 'stringTest' found");
        Assert.assertEquals("stringValue", stringValue, "unexpected string value");
    } // testStringValue()


    @Test
    public void testInitialBeans() {
        InitialBean ib = d.findBean(InitialBean.class);
        Assert.assertNotNull(ib, "Bean from externally provided initial map of beans not found");
        TestInterface t = ib.getTest();
        Assert.assertNotNull(t, "Initial bean is missing injected dependency");
        InitialBeanDependentComponent ibd = d.findBean(InitialBeanDependentComponent.class);
        Assert.assertNotNull(ibd, "Bean from depending on externally provided bean not found");
    } // testInitialBeans()


    @Test
    public void testNamedInjections() {
        NamedInjection ni = d.findBean(NamedInjection.class);
        Assert.assertNotNull(ni, "Named injection test component not found");
        Assert.assertEquals("stringValue", ni.getStringTest(), "Named inection with default name failed");
        Assert.assertEquals("a string value", ni.getStringValue(), "Named inection with passed name failed");
        Assert.assertEquals("This is a direct value", ni.getDirectValue(), "Named inection with default name failed");
        Assert.assertEquals("a string value", ni.getNamedValue(), "Named inection with passed name failed");
        Assert.assertEquals("a string value", ni.getSomeValue(), "Named inection with passed name failed");
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
        Assert.assertNotNull(di, "DI container could not be initialized");
        Assert.assertTrue(di.findBean(Boolean.class, "booleanStringValueTrue"), "Boolean string based value cannot be set to true");
        Assert.assertFalse(di.findBean(Boolean.class, "booleanStringValueFalse"), "Boolean string based value cannot be set to false");
        Assert.assertTrue(di.findBean(Boolean.class, "booleanBasetypeValueTrue"), "Boolean base type value cannot be set to true");
        Assert.assertFalse(di.findBean(Boolean.class, "booleanBasetypeValueFalse"), "Boolean base type value cannot be set to false");
        Assert.assertTrue(di.findBean(Boolean.class, "booleanTypedValueTrue"), "Boolean typed value cannot be set to true");
        Assert.assertFalse(di.findBean(Boolean.class, "booleanTypedValueFalse"), "Boolean typed value cannot be set to false");

        UnannotatedComponent unannotatedComponent = di.findBean(UnannotatedComponent.class);
        Assert.assertNotNull(unannotatedComponent, "Cannot find instance of un-annotated component mentioned in config file ");
        Assert.assertTrue(unannotatedComponent.isBasetypeBooleanValue(), "Boolean value could not be referenced in other bean");
    } // testBooleans()


    @Test
    @SuppressWarnings("unchecked")
    public void testStringReplacement() {
        Map<Object, Object> map = d.findBean(Map.class, "mapTest");
        Assert.assertNotNull(map, "test map not found");
        // test if system properties can be used
        Assert.assertNotNull(map.get("osName"), "no os name found");
        Assert.assertNotEquals("__UNKNOWN__", map.get("osName"), "os name must be given");
        Assert.assertEquals("here comes a string value (a string value)", map.get("replacementTest"), "pattern not replaced as expected");
    } // testStringReplacement()


    @Test
    public void testStaticInjection() {
        Assert.assertNotNull(TestComponentB.getTestInterface(), "Static field not injected");
    } // testStaticInjection()


    @Test
    public void testNumericInjection() {
        NumericInjection numerics = d.findBean(NumericInjection.class);
        Assert.assertEquals(42, numerics.getIntValue(), "Failure in injection of int value");
        Assert.assertEquals(123456789, numerics.getLongValue(), "Failure in injection of long value");
        Assert.assertEquals(3.14159, numerics.getFloatValue(), numerics.getFloatValue()-3.14159, "Failure in injection of float value");
        Assert.assertEquals(2.7, numerics.getDoubleValue(), numerics.getDoubleValue()-2.7, "Failure in injection of double value");
        Assert.assertEquals(true, numerics.isBooleanValue(), "Failure in injection of boolean value");
    } // testNumericInjection()


    @Test
    public void testCollectionInjection() {
        TestComponentB cb = d.findBean(TestComponentB.class);
        UnannotatedComponent uac = d.findBean(UnannotatedComponent.class);
        Assert.assertNotNull(cb.getAllInstances(), "No collection of instances available");
        Assert.assertEquals(1, cb.getAllInstances().size(), "Wrong number of instaces in collection");
        Assert.assertNotNull(uac.getManuallyInjectedCollection(), "No collection of instances available");
        Assert.assertEquals(1, uac.getManuallyInjectedCollection().size(), "Wrong number of instaces in collection");
    } // testConstructorInjection()


    @Test
    public void testAnnotationLookup() {
        Collection<Object> beans = d.findAnnotatedBeans(Singleton.class);
        Assert.assertEquals(10, beans.size(), "Unexpected number of annotated beans in scope");
    } // testAnnotationLookup()


    @Test
    public void testConstructorInjection() {
        ConstructorInjection constructorInjection = d.findBean(ConstructorInjection.class);
        Assert.assertEquals("a string value", constructorInjection.getString(), "Failure in injection of numeric value");
    } // testConstructorInjection()


    @Test
    public void testInstanceCreation() {
        MultiInstanceComponent instance = d.createBean(MultiInstanceComponent.class, null);
        TestComponent testComponent = d.findBean(TestComponent.class);
        Assert.assertNotNull(instance.getTestComponent(), "Component didn't get injected");
        Assert.assertEquals(testComponent, instance.getTestComponent(), "Non expected instance injected");
        Assert.assertEquals("default", instance.getName(), "Non expected name of fresh instance");
        MultiInstanceComponent secondInstance = d.createBean(MultiInstanceComponent.class, "fresh");
        Assert.assertNotNull(secondInstance.getTestComponent(), "Component didn't get injected");
        Assert.assertEquals(testComponent, secondInstance.getTestComponent(), "Non expected instance injected");
        Assert.assertEquals("overridden", secondInstance.getName(), "Non expected name of fresh instance");
    } // testInstanceCreation()


    @Test
    public void testInstanceInit() {
        MultiInstanceComponent instance = new MultiInstanceComponent();
        d.initBean(instance, null);
        Assert.assertEquals("default", instance.getName(), "Non expected name of fresh instance");
        Assert.assertNotNull(instance.getTestComponent(), "Component didn't get injected");
        TestComponent testComponent = d.findBean(TestComponent.class);
        Assert.assertEquals(testComponent, instance.getTestComponent(), "Non expected instance injected");
    } // testInstanceInit()


    @Test
    public void testUsingTck() {
        Car car = d.findBean(Car.class);
        Assert.assertNotNull(car, "Tck's car should have been instanciated");
        Tck.testsFor(car, true, true);
    } // testUsingTck()

} // InjectorTest
