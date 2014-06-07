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
import dinistiq.test.components.InitialBean;
import dinistiq.test.components.NumericInjection;
import dinistiq.test.components.StaticInjection;
import dinistiq.test.components.TestComponentB;
import dinistiq.test.components.TestInterface;
import dinistiq.test.components.UnannotatedComponent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;


public class SomeTest {

    @Test
    public void testFindImplementedInterface() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages);
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
        Assert.assertNotNull("DI container could not be initialized", d);
        Assert.assertNotNull("Cannot find instanciated component for interface", d.findTypedBean(TestInterface.class));
    } // testFindImplementedInterface()


    @Test
    public void testFindExplicitlyInstanciatedComponent() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        try {
            Dinistiq d = new Dinistiq(packages);
            Assert.assertNotNull("DI container could not be initialized", d);
            TestInterface ti = d.findTypedBean(TestInterface.class);
            TestInterface test = d.findBean(TestInterface.class, "test");
            Set<TestInterface> tis = d.findTypedBeans(TestInterface.class);
            Assert.assertNotNull("Cannot find instance of un-annotated component mentioned in config file ", d.findTypedBean(UnannotatedComponent.class));
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
    } // testFindExplicitlyInstanciatedComponent()


    @Test
    public void testNamedInjection() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        try {
            Dinistiq d = new Dinistiq(packages);
            Assert.assertNotNull("DI container could not be initialized", d);
            TestComponentB testComponentB = d.findTypedBean(TestComponentB.class);
            Assert.assertNotNull("need TestComponentB instance", testComponentB);
            Assert.assertEquals("Cannot find string values as expection ", "stringValue", testComponentB.getHallo());
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
    } // testNamedInjection()


    @Test
    public void testReferenceValue() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        try {
            Dinistiq d = new Dinistiq(packages);
            Assert.assertNotNull("DI container could not be initialized", d);
            // In this case in order to let the reference injection work, the bean MUST be named to find the correct properties file
            UnannotatedComponent unannotatedComponent = d.findBean(UnannotatedComponent.class, "unannotatedComponent");
            Assert.assertNotNull("Cannot find instance of un-annotated component mentioned in config file ", unannotatedComponent);
            Assert.assertNotNull("Cannot find auto-injected value ", unannotatedComponent.getAutoInjected());
            Assert.assertNotNull("Cannot find value injected as a reference ", unannotatedComponent.getTestInterface());
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
    } // testReferenceValue()


    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testMapBeans() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages);
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
        Assert.assertNotNull("DI container could not be initialized", d);
        Set<Map> maps = d.findTypedBeans(Map.class);
        Assert.assertTrue("no maps at all found", maps.size()>0);
        System.err.println("Maps: "+maps);
        Map<Object, Object> map = d.findBean(Map.class, "mapTest");
        Assert.assertNotNull("test map not found", map);
        Assert.assertEquals("default value not correct", "defaultValueA", map.get("keyA"));
        Assert.assertEquals("specialized value not correct", "overriddenValueB", map.get("keyB"));
    } // testMapBeans()


    @Test
    public void testStringValue() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages);
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
        Assert.assertNotNull("DI container could not be initialized", d);
        String stringValue = d.findBean(String.class, "stringTest");
        Assert.assertNotNull("not string with name 'stringTest' found", stringValue);
        Assert.assertEquals("unexpected string value", "stringValue", stringValue);
    } // testStringValue()


    @Test
    public void testInitialBeans() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        Map<String, Object> initialBeans = new HashMap<String, Object>();
        InitialBean initialBean = new InitialBean();
        initialBeans.put("initialBean", initialBean);
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages, initialBeans);
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
        Assert.assertNotNull("DI container could not be initialized", d);
        InitialBean ib = d.findTypedBean(InitialBean.class);
        Assert.assertNotNull("Bean from externally provided initial map of beans not found", ib);
    } // testInitialBeans()


    @Test
    public void testBooleans() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        Map<String, Object> initialBeans = new HashMap<String, Object>();
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

        UnannotatedComponent unannotatedComponent = d.findTypedBean(UnannotatedComponent.class);
        Assert.assertNotNull("Cannot find instance of un-annotated component mentioned in config file ", unannotatedComponent);
        Assert.assertTrue("Boolean value could not be referenced in other bean", unannotatedComponent.isBasetypeBooleanValue());
    } // testBooleans()


    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testStringReplacement() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages);
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
        Assert.assertNotNull("DI container could not be initialized", d);
        Map<Object, Object> map = d.findBean(Map.class, "mapTest");
        Assert.assertNotNull("test map not found", map);
        Assert.assertEquals("pattern not replaced as expected", "here comes a string value (a string value)", map.get("replacementTest"));
    } // testStringReplacement()


    @Test
    public void testStaticInjection() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages);
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
        Assert.assertNotNull("DI container could not be initialized", d);
        Assert.assertNotNull("Static field not injected", StaticInjection.getTestInterface());
    } // testStaticInjection()


    @Test
    public void testNumericInjection() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages);
        } catch (Exception e) {
            Assert.fail(e.toString());
        } // try/catch
        Assert.assertNotNull("DI container could not be initialized", d);
        NumericInjection ni = d.findTypedBean(NumericInjection.class);
        final String msg = "Failure in injection of numeric value";
        Assert.assertEquals(msg, 42, ni.getIntValue());
        Assert.assertEquals(msg, 123456789, ni.getLongValue());
        Assert.assertEquals(msg, 3.14159, ni.getFloatValue(), ni.getFloatValue()-3.14159);
        Assert.assertEquals(msg, 2.7, ni.getDoubleValue(), ni.getDoubleValue()-2.7);
    } // testNumericInjection()

} // SomeTest
