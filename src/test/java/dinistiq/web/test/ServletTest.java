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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


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
        try {
            DinistiqServlet ds = new DinistiqServlet();
            ServletConfig servletConfig = Mockito.mock(ServletConfig.class);
            Mockito.when(servletConfig.getServletContext()).thenReturn(new MockServletContext(d));

            ds.init(servletConfig);
            HttpServletRequest request1 = Mockito.mock(HttpServletRequest.class);
            Mockito.when(request1.getContextPath()).thenReturn("/context");
            Mockito.when(request1.getRequestURI()).thenReturn("/context/servlet/testing");
            HttpServletResponse response1 = Mockito.mock(HttpServletResponse.class);
            ds.service(request1, response1);

            HttpServletRequest request2 = Mockito.mock(HttpServletRequest.class);
            Mockito.when(request2.getContextPath()).thenReturn("/");
            Mockito.when(request2.getRequestURI()).thenReturn("/servlet/nothing");
            HttpServletResponse response2 = Mockito.mock(HttpServletResponse.class);
            ds.service(request2, response2);

            HttpServletRequest request3 = Mockito.mock(HttpServletRequest.class);
            Mockito.when(request3.getContextPath()).thenReturn("/");
            Mockito.when(request3.getRequestURI()).thenReturn("/");
            HttpServletResponse response3 = Mockito.mock(HttpServletResponse.class);
            ds.service(request3, response3);
        } catch (IOException|ServletException e) {
            Assert.fail("Exception while testing servlet "+e.getMessage());
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
        dcll.contextDestroyed(sce);
    } // testContextLoaderListener()

} // ServletTest
