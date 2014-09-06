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
import dinistiq.test.components.ConstructorInjection;
import dinistiq.test.components.InitialBean;
import dinistiq.test.components.InitialBeanDependentComponent;
import dinistiq.test.components.NumericInjection;
import dinistiq.test.components.TestComponentB;
import dinistiq.test.components.TestInterface;
import dinistiq.test.components.UnannotatedComponent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import org.junit.Assert;
import org.junit.Test;


public class InjectorTest {

    private Dinistiq d;

    private final Set<String> packages;


    public static Map<String, Object> prepareInitialBeans() {
        Map<String, Object> initialBeans = new HashMap<String, Object>();
        InitialBean initialBean = new InitialBean();
        initialBeans.put("initialBean", initialBean);
        return initialBeans;
    } // prepareInitialBeans()


    public InjectorTest() {
        packages = new HashSet<>();
        packages.add(TestInterface.class.getPackage().getName());
        try {
            d = new Dinistiq(packages, prepareInitialBeans());
        } catch (Exception e) {
            Assert.assertNotNull("DI container could not be initialized", d);
            Assert.fail(e.getMessage());
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testMapBeans() {
        Set<Map> maps = d.findBeans(Map.class);
        Assert.assertTrue("no maps at all found", maps.size()>0);
        Map<Object, Object> map = d.findBean(Map.class, "mapTest");
        Assert.assertNotNull("test map not found", map);
        Assert.assertEquals("default value not correct", "defaultValueA", map.get("keyA"));
        Assert.assertEquals("specialized value not correct", "overriddenValueB", map.get("keyB"));
    } // testMapBeans()


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
    public void testBooleans() {
        Map<String, Object> initialBeans = prepareInitialBeans();
        initialBeans.put("booleanBasetypeValueTrue", true);
        initialBeans.put("booleanTypedValueTrue", Boolean.TRUE);
        initialBeans.put("booleanBasetypeValueFalse", false);
        initialBeans.put("booleanTypedValueFalse", Boolean.FALSE);
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages, initialBeans);
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
        Assert.assertNotNull("DI container could not be initialized", d);
        Assert.assertTrue("Boolean string based value cannot be set to true", d.findBean(Boolean.class, "booleanStringValueTrue"));
        Assert.assertFalse("Boolean string based value cannot be set to false", d.findBean(Boolean.class, "booleanStringValueFalse"));
        Assert.assertTrue("Boolean base type value cannot be set to true", d.findBean(Boolean.class, "booleanBasetypeValueTrue"));
        Assert.assertFalse("Boolean base type value cannot be set to false", d.findBean(Boolean.class, "booleanBasetypeValueFalse"));
        Assert.assertTrue("Boolean typed value cannot be set to true", d.findBean(Boolean.class, "booleanTypedValueTrue"));
        Assert.assertFalse("Boolean typed value cannot be set to false", d.findBean(Boolean.class, "booleanTypedValueFalse"));

        UnannotatedComponent unannotatedComponent = d.findBean(UnannotatedComponent.class);
        Assert.assertNotNull("Cannot find instance of un-annotated component mentioned in config file ", unannotatedComponent);
        Assert.assertTrue("Boolean value could not be referenced in other bean", unannotatedComponent.isBasetypeBooleanValue());
    } // testBooleans()


    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
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
        Assert.assertEquals("Unexpected number of annotated beans in scope", 5, beans.size());
    } // testAnnotationLookup()


    @Test
    public void testConstructorInjection() {
        ConstructorInjection constructorInjection = d.findBean(ConstructorInjection.class);
        Assert.assertEquals("Failure in injection of numeric value", "a string value", constructorInjection.getString());
    } // testConstructorInjection()

} // InjectorTest
