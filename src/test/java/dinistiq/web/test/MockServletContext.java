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
import dinistiq.SimpleClassResolver;
import dinistiq.web.DinistiqContextLoaderListener;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;


@SuppressWarnings("deprecation")
public class MockServletContext implements ServletContext {

    private final Map<String, Object> attributes = new HashMap<>();


    public MockServletContext(Dinistiq dinistiq) {
        attributes.put(DinistiqContextLoaderListener.DINISTIQ_INSTANCE, dinistiq);
    }


    @Override
    public String getContextPath() {
        return "/";
    }


    @Override
    public ServletContext getContext(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public int getMajorVersion() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public int getMinorVersion() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public String getMimeType(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Set<?> getResourcePaths(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public URL getResource(String string) throws MalformedURLException {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public InputStream getResourceAsStream(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public RequestDispatcher getRequestDispatcher(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public RequestDispatcher getNamedDispatcher(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Servlet getServlet(String string) throws ServletException {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Enumeration<?> getServlets() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Enumeration<?> getServletNames() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void log(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void log(Exception excptn, String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void log(String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public String getRealPath(String string) {
        // Result must not be an empty string
        return "/x"+string;
    }


    @Override
    public String getServerInfo() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public String getInitParameter(String string) {
        if (DinistiqContextLoaderListener.DINISTIQ_PACKAGES.equals(string)) {
            return "dummy";
        } // if
        if (DinistiqContextLoaderListener.DINISTIQ_CLASSRESOLVER.equals(string)) {
            return SimpleClassResolver.class.getName();
        } // if
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Enumeration<?> getInitParameterNames() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }


    @Override
    public Enumeration<?> getAttributeNames() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }


    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }


    @Override
    public String getServletContextName() {
        throw new UnsupportedOperationException("NYI");
    }

} // MockServletContext
