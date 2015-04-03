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
import dinistiq.SimpleClassResolver;
import dinistiq.web.DinistiqContextLoaderListener;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import org.mockito.Mockito;


@SuppressWarnings("deprecation")
public class MockServletContext implements ServletContext {

    private final Map<String, Object> attributes = new HashMap<>();

    private boolean emptyInit;

    private String resolverClassName;


    public MockServletContext(boolean emptyInit, String resolverClassName, Dinistiq dinistiq) {
        this.emptyInit = emptyInit;
        this.resolverClassName = resolverClassName;
        attributes.put(DinistiqContextLoaderListener.DINISTIQ_INSTANCE, dinistiq);
    }


    public MockServletContext(Dinistiq dinistiq) {
        this(false, null, dinistiq);
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
    public Set<String> getResourcePaths(String string) {
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
    public Enumeration<Servlet> getServlets() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Enumeration<String> getServletNames() {
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
            return emptyInit ? null : "dummy";
        } // if
        if (DinistiqContextLoaderListener.DINISTIQ_CLASSRESOLVER.equals(string)) {
            return emptyInit ? resolverClassName : SimpleClassResolver.class.getName();
        } // if
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Enumeration<String> getInitParameterNames() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }


    @Override
    public Enumeration<String> getAttributeNames() {
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


    @Override
    public int getEffectiveMajorVersion() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public int getEffectiveMinorVersion() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public boolean setInitParameter(String string, String string1) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String string, String string1) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String string, Servlet srvlt) {
        return Mockito.mock(ServletRegistration.Dynamic.class);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String string, Class<? extends Servlet> type) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public <T extends Servlet> T createServlet(Class<T> type) throws ServletException {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public ServletRegistration getServletRegistration(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String string, String string1) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String string, Filter filter) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String string, Class<? extends Filter> type) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public <T extends Filter> T createFilter(Class<T> type) throws ServletException {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public FilterRegistration getFilterRegistration(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> set) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void addListener(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public <T extends EventListener> void addListener(T t) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void addListener(Class<? extends EventListener> type) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public <T extends EventListener> T createListener(Class<T> type) throws ServletException {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void declareRoles(String... strings) {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public String getVirtualServerName() {
        throw new UnsupportedOperationException("NYI");
    }

} // MockServletContext
