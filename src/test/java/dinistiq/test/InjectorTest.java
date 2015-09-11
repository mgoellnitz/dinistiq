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
        Assert.assertNull(d.findBean(Object.class, "nonsense"), "not fully specified instance should not be available");
    } // testFindExplicitlyInstanciatedComponent()


    @Test
    public void testFindNames() {
        Set<String> tis = d.findNames(TestInterface.class);
        Assert.assertNotNull(tis, "Cannot obtain name set");
        Assert.assertEquals(tis.size(), 1, "Cannot find expected number of bean names");
        Assert.assertEquals(tis.iterator().next(), "testComponent", "Cannot find expected bean name");
        Set<String> negative = d.findNames(InjectorTest.class);
        Assert.assertNotNull(negative, "Cannot obtain name set");
        Assert.assertEquals(negative.size(), 0, "Cannot find expected number of bean names");
    } // testFindNames()


    @Test
    public void testDontFind() {
        String negativeTest = d.findBean(String.class, "unannotatedComponent");
        Assert.assertNull(negativeTest, "Type conversion should not have been possible");
        Set<InjectorTest> negative = d.findBeans(InjectorTest.class);
        Assert.assertNotNull(negative, "Cannot obtain instance set");
        Assert.assertEquals(negative.size(), 0, "Found unexpected instances");
        InjectorTest notAvailable = d.findBean(InjectorTest.class);
        Assert.assertNull(notAvailable, "Cannot obtain instance set");
    } // testDontFind()


    @Test
    public void testNamedInjection() {
        TestComponentB testComponentB = d.findBean(TestComponentB.class);
        Assert.assertNotNull(testComponentB, "need TestComponentB instance");
        Assert.assertEquals(testComponentB.getHallo(), "stringValue", "Cannot find string values as expection");
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
        Assert.assertEquals(map.get("keyA"), "defaultValueA", "default value not correct");
        Assert.assertEquals(map.get("keyB"), "overriddenValueB", "specialized value not correct");
    } // testMapBeans()


    @Test
    @SuppressWarnings("rawtypes")
    public void testLiteralCollections() {
        Set<List> lists = d.findBeans(List.class);
        Assert.assertTrue(lists.size()>0, "no lists at all found");
        Assert.assertNull(d.findBean(List.class, "noList"), "not fully specified lis should not be found.");

        List list = d.findBean(List.class, "listTest");
        Assert.assertNotNull(list, "test list not found");
        Assert.assertEquals(list.get(0), "first", "first value not correct");
        Assert.assertEquals(list.get(1), "second", "second value not correct");

        Set<Set> sets = d.findBeans(Set.class);
        Assert.assertTrue(sets.size()>0, "no sets at all found");
        Set set = d.findBean(Set.class, "setTest");
        Assert.assertNotNull(set, "test set not found");
        Assert.assertEquals(set.size(), 2, "set should contain two elements");
        Assert.assertTrue(set.contains("second"), "missing value 'second'");

        CollectionReferences cr = d.findBean(CollectionReferences.class);
        Assert.assertNotNull(cr, "no collection references object found");
        Assert.assertEquals(cr.getStringSet().size(), 2, "referenced set should contain two elements");
        Assert.assertTrue(cr.getStringSet().contains("second"), "first value not correct in referenced set");
    } // testLiteralCollections()


    @Test
    public void testStringValue() {
        String stringValue = d.findBean(String.class, "stringTest");
        Assert.assertNotNull(stringValue, "not string with name 'stringTest' found");
        Assert.assertEquals(stringValue, "stringValue", "unexpected string value");
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
        Assert.assertEquals(ni.getStringTest(), "stringValue", "Named inection with default name failed");
        Assert.assertEquals(ni.getStringValue(), "a string value", "Named inection with passed name failed");
        Assert.assertEquals(ni.getDirectValue(), "This is a direct value", "Named inection with default name failed");
        Assert.assertEquals(ni.getNamedValue(), "a string value", "Named inection with passed name failed");
        Assert.assertEquals(ni.getSomeValue(), "a string value", "Named inection with passed name failed");
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
        Assert.assertNotEquals(map.get("osName"), "__UNKNOWN__", "os name must be given");
        Assert.assertEquals(map.get("unknownReference"), "__UNKNOWN__", "reference cannot be replaced");
        Assert.assertEquals(map.get("noReplacementTest"), "this is perfectly __UNKNOWN__", "this is perfectly ${noReference}");
        Assert.assertEquals(map.get("replacementTest"), "here comes a string value (a string value)", "pattern not replaced as expected");
    } // testStringReplacement()


    @Test
    public void testStaticInjection() {
        Assert.assertNotNull(TestComponentB.getTestInterface(), "Static field not injected");
    } // testStaticInjection()


    @Test
    public void testNumericInjection() {
        NumericInjection numerics = d.findBean(NumericInjection.class);
        Assert.assertEquals(numerics.getIntValue(), 42, "Failure in injection of int value");
        Assert.assertEquals(numerics.getLongValue(), 123456789, "Failure in injection of long value");
        Assert.assertEquals(numerics.getFloatValue(), 3.14159, numerics.getFloatValue()-3.14159, "Failure in injection of float value");
        Assert.assertEquals(numerics.getDoubleValue(), 2.7, numerics.getDoubleValue()-2.7, "Failure in injection of double value");
        Assert.assertEquals(numerics.isBooleanValue(), true, "Failure in injection of boolean value");
    } // testNumericInjection()


    @Test
    public void testCollectionInjection() {
        TestComponentB cb = d.findBean(TestComponentB.class);
        UnannotatedComponent uac = d.findBean(UnannotatedComponent.class);
        Assert.assertNotNull(cb.getAllInstances(), "No collection of instances available");
        Assert.assertEquals(cb.getAllInstances().size(), 1, "Wrong number of instaces in collection");
        Assert.assertNotNull(uac.getManuallyInjectedCollection(), "No collection of instances available");
        Assert.assertEquals(uac.getManuallyInjectedCollection().size(), 1, "Wrong number of instances in collection");
        Assert.assertNotNull(uac.getManuallyInjectedList(), "No list of strings available");
        Assert.assertEquals(uac.getManuallyInjectedList().size(), 3, "Wrong number of strings in list");
        Assert.assertEquals(uac.getManuallyInjectedList().get(1), "nice", "Unexpected second value in list");
    } // testConstructorInjection()


    @Test
    public void testAnnotationLookup() {
        Collection<Object> beans = d.findAnnotatedBeans(Singleton.class);
        Assert.assertEquals(beans.size(), 9, "Unexpected number of annotated beans in scope");
    } // testAnnotationLookup()


    @Test
    public void testConstructorInjection() {
        ConstructorInjection constructorInjection = d.findBean(ConstructorInjection.class);
        Assert.assertEquals(constructorInjection.getString(), "a string value", "Failure in injection of numeric value");
    } // testConstructorInjection()


    @Test
    public void testInstanceCreation() {
        MultiInstanceComponent instance = d.createBean(MultiInstanceComponent.class, null);
        TestComponent testComponent = d.findBean(TestComponent.class);
        Assert.assertNotNull(instance.getTestComponent(), "Component didn't get injected");
        Assert.assertEquals(instance.getTestComponent(), testComponent, "Non expected instance injected");
        Assert.assertEquals(instance.getName(), "default", "Non expected name of fresh instance");
        MultiInstanceComponent secondInstance = d.createBean(MultiInstanceComponent.class, "fresh");
        Assert.assertNotNull(secondInstance.getTestComponent(), "Component didn't get injected");
        Assert.assertEquals(secondInstance.getTestComponent(), testComponent, "Non expected instance injected");
        Assert.assertEquals(secondInstance.getName(), "overridden", "Non expected name of fresh instance");
    } // testInstanceCreation()


    @Test
    public void testInstanceInit() {
        MultiInstanceComponent instance = new MultiInstanceComponent();
        d.initBean(instance, null);
        Assert.assertEquals(instance.getName(), "default", "Non expected name of fresh instance");
        Assert.assertNotNull(instance.getTestComponent(), "Component didn't get injected");
        TestComponent testComponent = d.findBean(TestComponent.class);
        Assert.assertEquals(instance.getTestComponent(), testComponent, "Non expected instance injected");
    } // testInstanceInit()


    @Test
    public void testUsingTck() {
        Car car = d.findBean(Car.class);
        Assert.assertNotNull(car, "Tck's car should have been instanciated");
        Tck.testsFor(car, true, true);
    } // testUsingTck()

} // InjectorTest
