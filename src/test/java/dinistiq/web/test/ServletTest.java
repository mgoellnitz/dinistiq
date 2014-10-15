/**
 *
 * Copyright 2014 Martin Goellnitz
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
import dinistiq.web.DinistiqServlet;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import org.junit.Assert;
import org.junit.Test;


/**
 * Minimal test for the dinistiq servlet.
 */
public class ServletTest {

    @Test
    public void testServlet() {
        Set<String> packages = new HashSet<>();
        packages.add(TestInterface.class.getPackage().getName());
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages, InjectorTest.prepareInitialBeans());
        } catch (Exception e) {
            Assert.assertNotNull("DI container could not be initialized", d);
            Assert.fail(e.getMessage());
        } // try/catch

        DinistiqServlet ds = new DinistiqServlet();
        final ServletConfig servletConfig = new MockServletConfig(d);
        try {
            ds.init(servletConfig);
        } catch (ServletException se) {
            Assert.fail("Exception while initializing servlet "+se.getMessage());
        } // try/catch
    } // testServlet()


    @Test
    public void testContextLoaderListener() {
        Set<String> packages = new HashSet<>();
        packages.add(TestInterface.class.getPackage().getName());
        Dinistiq d = null;
        try {
            d = new Dinistiq(packages, InjectorTest.prepareInitialBeans());
        } catch (Exception e) {
            Assert.assertNotNull("DI container could not be initialized", d);
            Assert.fail(e.getMessage());
        } // try/catch

        DinistiqContextLoaderListener dcll = new DinistiqContextLoaderListener();
        ServletContextEvent sce = new ServletContextEvent(new MockServletContext(d));
        dcll.contextInitialized(sce);
    } // testContextLoaderListener()

} // ServletTest
