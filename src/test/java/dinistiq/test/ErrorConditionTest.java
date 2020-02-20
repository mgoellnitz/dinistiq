/**
 *
 * Copyright 2016-2020 Martin Goellnitz
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
import dinistiq.test.components.InjectionFailure;
import dinistiq.test.components.ManualBean;
import dinistiq.test.components.TestInterface;
import dinistiq.web.test.MockServletContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import org.atinject.tck.auto.Car;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * This is the collection of tests which should trigger problems and thus
 * produce well defined error conditions of the software.
 */
public class ErrorConditionTest {

    private Dinistiq d;


    /**
     * Prepare a map of beans used as initial beans on dinistiq instanciation.
     *
     * Used to be interoperable with one set of properties files and the other test scenrios.
     *
     * @return map of named beans.
     */
    public static Map<String, Object> prepareInitialBeans() {
        Map<String, Object> initialBeans = new HashMap<>();
        initialBeans.put("initialBean", new InitialBean());
        initialBeans.put("servetContext", new MockServletContext(null));
        return initialBeans;
    } // prepareInitialBeans()


    /**
     * Set up this test execution class with a special dinisitq setup to trigger error conditions.
     */
    public ErrorConditionTest() {
        Set<String> packages = new HashSet<>();
        packages.add(TestInterface.class.getPackage().getName());
        packages.add(Car.class.getPackage().getName());
        try {
            d = new Dinistiq(packages, prepareInitialBeans());
        } catch (Exception e) {
            Assert.fail(e.getClass().getSimpleName()+": "+e.getMessage());
            Assert.assertNotNull(d, "DI container could not be initialized");
        } // try/catch
    } // ErrorConditionTest()


    /**
     * Test if no component gets found accidentally.
     */
    @Test
    public void testDontFind() {
        String negativeTest = d.findBean(String.class, "unannotatedComponent");
        Assert.assertNull(negativeTest, "Type conversion should not have been possible");
        Set<ErrorConditionTest> negative = d.findBeans(ErrorConditionTest.class);
        Assert.assertNotNull(negative, "Cannot obtain instance set");
        Assert.assertEquals(negative.size(), 0, "Found unexpected instances");
        ErrorConditionTest notAvailable = d.findBean(ErrorConditionTest.class);
        Assert.assertNull(notAvailable, "Cannot obtain instance set");
    } // testDontFind()


    /**
     * Test if no component gets injected accidentally.
     */
    @Test
    public void testInjectionFailures() {
        ManualBean bean = new ManualBean();
        d.initBean(bean, null);
        Assert.assertEquals(bean.getIndicator(), "not initialized", "Unexpected value from post construct method found.");
        InjectionFailure failure = d.createBean(InjectionFailure.class, null);
        Assert.assertNotNull(failure, "Was not able to create instance.");
        Assert.assertEquals(failure.getIndicator(), "not initialized", "Unexpected value from impossible injection.");
    } // testInjectionFailures()


    /**
     * Test if init failed intentionally in broken setup.
     */
    @Test
    public void testFailures() {
        Singleton failure = d.createBean(Singleton.class, null);
        Assert.assertNull(failure, "Should not have been able to create instance.");
    } // testFailures()

} // ErrorConditionTest
