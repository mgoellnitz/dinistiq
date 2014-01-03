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
import dinistiq.test.components.StaticInjection;
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
            //
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
            TestInterface ti = d.findTypedBean(TestInterface.class);
            TestInterface test = d.findBean(TestInterface.class, "test");
            Set<TestInterface> tis = d.findTypedBeans(TestInterface.class);
            Assert.assertNotNull("DI container could not be initialized", d);
            Assert.assertNotNull("Cannot find instance of un-annotated component mentioned in config file ", d.findTypedBean(UnannotatedComponent.class));
        } catch (Exception e) {
            //
        } // try/catch
    } // testFindExplicitlyInstanciatedComponent()


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
            //
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
            //
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
            //
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
            //
        } // try/catch
        Assert.assertNotNull("DI container could not be initialized", d);
        InitialBean ib = d.findTypedBean(InitialBean.class);
        Assert.assertNotNull("Bean from externally provided initial map of beans not found", ib);
    } // testInitialBeans()


    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testStringReplacement() {
        Set<String> packages = new HashSet<String>();
        packages.add(TestInterface.class.getPackage().getName());
        Map<String, Object> initialBeans = new HashMap<String, Object>();
        InitialBean initialBean = new InitialBean();
        initialBeans.put("initialBean", initialBean);
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages, initialBeans);
        } catch (Exception e) {
            //
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
            //
        } // try/catch
        Assert.assertNotNull("DI container could not be initialized", d);
        Assert.assertNotNull("Static field not injected", StaticInjection.getTestInterface());
    } // testStaticInjection()

} // SomeTest
