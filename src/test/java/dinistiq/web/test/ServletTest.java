/**
 *
 * Copyright 2014-2015 Martin Goellnitz
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
package dinistiq.web.test;

import dinistiq.Dinistiq;
import dinistiq.test.InjectorTest;
import dinistiq.test.components.TestInterface;
import dinistiq.web.DinistiqContextLoaderListener;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContextEvent;
import org.atinject.tck.auto.Car;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Minimal test for the dinistiq servlet.
 */
public class ServletTest {

    @Test
    public void testContextLoaderListener() {
        Set<String> packages = new HashSet<>();
        packages.add(TestInterface.class.getPackage().getName());
        packages.add(Car.class.getPackage().getName());
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages, InjectorTest.prepareInitialBeans());
        } catch (Exception e) {
            Assert.assertNotNull(d, "DI container could not be initialized");
            Assert.fail(e.getMessage());
        } // try/catch

        // Test with some init values for the servlet
        DinistiqContextLoaderListener dcll = new DinistiqContextLoaderListener();
        ServletContextEvent sce = new ServletContextEvent(new MockServletContext(d));
        dcll.contextInitialized(sce);
        dcll.contextDestroyed(sce);

        // Test with no init values for the servlet to trigger default paths
        dcll = new DinistiqContextLoaderListener();
        MockServlet servlet = new MockServlet();
        sce = new ServletContextEvent(new MockServletContext(true, null, d, servlet));
        dcll.contextInitialized(sce);
        Assert.assertEquals(servlet.getValueInServlet(), "stringValue", "Injection into servlet failed.");
        dcll.contextDestroyed(sce);

//        // Test with partially wrong init values for the servlet to trigger exception handling
//        dcll = new DinistiqContextLoaderListener();
//        sce = new ServletContextEvent(new MockServletContext(true, "X", d, servlet));
//        dcll.contextInitialized(sce);
//        dcll.contextDestroyed(sce);
    } // testContextLoaderListener()

} // ServletTest
