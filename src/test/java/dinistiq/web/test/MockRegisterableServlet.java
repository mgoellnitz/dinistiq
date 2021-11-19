/*
 *
 * Copyright 2014-2021 Martin Goellnitz
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

import dinistiq.web.RegisterableServlet;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;


/**
 * Mock registerable servlet to trigger code threads in servlet setup.
 */
@Named
@Singleton
public class MockRegisterableServlet implements RegisterableServlet {

    /**
     * Get mock url patterns.
     */
    @Override
    public Set<String> getUrlPatterns() {
        Set<String> result = new HashSet<>();
        result.add("/testing");
        return result;
    }


    /**
     * Not implemented.
     *
     * @see RegisterableServlet#getOrder
     */
    @Override
    public int getOrder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * Not implemented.
     *
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig sc) throws ServletException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * Not implemented.
     *
     * @see javax.servlet.Servlet#getServletConfig()
     */
    @Override
    public ServletConfig getServletConfig() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * Empty mock implementation.
     *
     * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public void service(ServletRequest sr, ServletResponse sr1) throws ServletException, IOException {
        // Mocks don't provide any service.
    }


    /**
     * There is no real servlet information for mocks.
     * @return
     */
    @Override
    public String getServletInfo() {
        return "-";
    }


    /**
     * Destroy servlet. Which means do nothing in this context.
     */
    @Override
    public void destroy() {
        // Nothing to do on destruction.
    }


    /**
     * All mock registerable servlets are equal. No Exceptions.
     */
    @Override
    public int compareTo(RegisterableServlet o) {
        return 0;
    }


    /**
     * All mock registerable servlets are equal. No Exceptions.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof MockRegisterableServlet;
    }


    /**
     * All mock registerable servlets have the same hash code.
     *
     * @return 13
     */
    @Override
    public int hashCode() {
        return 13;
    }

} // MockRegisterableServlet
