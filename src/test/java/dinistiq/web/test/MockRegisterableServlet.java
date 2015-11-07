/*
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

import dinistiq.web.RegisterableServlet;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * Mock registerable servlet to trigger code threads in servlet setup.
 */
@Named
@Singleton
public class MockRegisterableServlet implements RegisterableServlet {

    @Override
    public Set<String> getUrlPatterns() {
        Set<String> result = new HashSet<>();
        result.add("/testing");
        return result;
    }


    @Override
    public int getOrder() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void init(ServletConfig sc) throws ServletException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public ServletConfig getServletConfig() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void service(ServletRequest sr, ServletResponse sr1) throws ServletException, IOException {
        // Mocks don't provide any service.
    }


    @Override
    public String getServletInfo() {
        return "-";
    }


    @Override
    public void destroy() {
        // Nothing to do on destruction.
    }


    @Override
    public int compareTo(RegisterableServlet o) {
        return 0;
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof MockRegisterableServlet;
    }


    @Override
    public int hashCode() {
        return 13;
    }

} // MockRegisterableServlet
