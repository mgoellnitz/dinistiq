/**
 *
 * Copyright 2014-2017 Martin Goellnitz
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
package dinistiq.web.test; // NO PMD - ignore bunch of public methods warning since this has to be implemented

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


/**
 * Mock implementation of a servlet context specialized for dinistiq testing.
 */
@SuppressWarnings("deprecation")
public class MockServletContext implements ServletContext {

    private final Map<String, Object> attributes = new HashMap<>();

    private boolean emptyInit;

    private String resolverClassName;


    /**
     * Create a new mock servlet context with no real servlet attached for test purposes.
     *
     * @param emptyInit no parameters given in init section of servlet configuration
     * @param resolverClassName resolver class name to pass to dinistiq
     * @param dinistiq dinistiq instance to store in context attributes
     */
    public MockServletContext(boolean emptyInit, String resolverClassName, Dinistiq dinistiq) {
        this.emptyInit = emptyInit;
        this.resolverClassName = resolverClassName;
        attributes.put(DinistiqContextLoaderListener.DINISTIQ_INSTANCE, dinistiq);
    }


    /**
     * Create a new mock servlet context with no real servlet attached for test purposes.
     *
     * @param dinistiq dinistiq instance to store in context attributes
     */
    public MockServletContext(Dinistiq dinistiq) {
        this(false, null, dinistiq);
    }


    /**
     * @see ServletContext#getContextPath()
     */
    @Override
    public String getContextPath() {
        return "/";
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getContext(java.lang.String)
     */
    @Override
    public ServletContext getContext(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getMajorVersion()
     */
    @Override
    public int getMajorVersion() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getMinorVersion()
     */
    @Override
    public int getMinorVersion() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getMimeType(java.lang.String)
     */
    @Override
    public String getMimeType(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getResourcePaths(java.lang.String)
     */
    @Override
    public Set<String> getResourcePaths(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getResource(java.lang.String)
     */
    @Override
    public URL getResource(String string) throws MalformedURLException {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getRequestDispatcher(java.lang.String)
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getNamedDispatcher(java.lang.String)
     */
    @Override
    public RequestDispatcher getNamedDispatcher(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getServlet(java.lang.String)
     */
    @Override
    public Servlet getServlet(String string) throws ServletException {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getServlets()
     */
    @Override
    public Enumeration<Servlet> getServlets() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getServletNames()
     */
    @Override
    public Enumeration<String> getServletNames() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#log(java.lang.String)
     */
    @Override
    public void log(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#log(java.lang.Exception, java.lang.String)
     */
    @Override
    public void log(Exception excptn, String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#log(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void log(String string, Throwable thrwbl) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Get real path mock while pretending our base path is /x.
     *
     * @param path path to find real path for
     * @return path prepended by /x
     */
    @Override
    public String getRealPath(String path) {
        // Result must not be an empty string
        return "/x"+path;
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getServerInfo()
     */
    @Override
    public String getServerInfo() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Get init parameters for given name.
     *
     * Only understands classresolver and package parameters for dinisitiq.
     *
     * @param string name of the parameter
     * @return default value or value given through constructor depending on "emptyInit" constructor parameter.
     */
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


    /**
     * Not implemented.
     *
     * @see ServletContext#getInitParameterNames()
     */
    @Override
    public Enumeration<String> getInitParameterNames() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Get attributes from mock implementation.
     *
     * @see ServletContext#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getAttributeNames()
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Set attribute in mock implementation.
     *
     * @see ServletContext#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }


    /**
     * Remove attribute from mock implementation.
     *
     * @see ServletContext#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getServletContextName()
     */
    @Override
    public String getServletContextName() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getEffectiveMajorVersion()
     */
    @Override
    public int getEffectiveMajorVersion() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getEffectiveMinorVersion()
     */
    @Override
    public int getEffectiveMinorVersion() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#setInitParameter(java.lang.String, java.lang.String)
     */
    @Override
    public boolean setInitParameter(String string, String string1) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#addServlet(java.lang.String, java.lang.String)
     */
    @Override
    public ServletRegistration.Dynamic addServlet(String string, String string1) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Mock a servlet registration.
     *
     * @see ServletContext#addServlet(java.lang.String, javax.servlet.Servlet)
     * @return mock
     */
    @Override
    public ServletRegistration.Dynamic addServlet(String string, Servlet srvlt) {
        return Mockito.mock(ServletRegistration.Dynamic.class);
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#addServlet(java.lang.String, java.lang.Class)
     */
    @Override
    public ServletRegistration.Dynamic addServlet(String string, Class<? extends Servlet> type) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#createServlet(java.lang.Class)
     */
    @Override
    public <T extends Servlet> T createServlet(Class<T> type) throws ServletException {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getServletRegistration(java.lang.String)
     */
    @Override
    public ServletRegistration getServletRegistration(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getServletRegistrations()
     */
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#addFilter(java.lang.String, java.lang.String)
     */
    @Override
    public FilterRegistration.Dynamic addFilter(String string, String string1) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#addFilter(java.lang.String, javax.servlet.Filter)
     */
    @Override
    public FilterRegistration.Dynamic addFilter(String string, Filter filter) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#addFilter(java.lang.String, java.lang.Class)
     */
    @Override
    public FilterRegistration.Dynamic addFilter(String string, Class<? extends Filter> type) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#createFilter(java.lang.Class)
     */
    @Override
    public <T extends Filter> T createFilter(Class<T> type) throws ServletException {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getFilterRegistration(java.lang.String)
     */
    @Override
    public FilterRegistration getFilterRegistration(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getFilterRegistrations()
     */
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getSessionCookieConfig()
     */
    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#setSessionTrackingModes(java.util.Set)
     */
    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> set) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getDefaultSessionTrackingModes()
     */
    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getEffectiveSessionTrackingModes()
     */
    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#addListener(java.lang.String)
     */
    @Override
    public void addListener(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#addListener(java.util.EventListener)
     */
    @Override
    public <T extends EventListener> void addListener(T t) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#addListener(java.lang.Class)
     */
    @Override
    public void addListener(Class<? extends EventListener> type) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#createListener(java.lang.Class)
     */
    @Override
    public <T extends EventListener> T createListener(Class<T> type) throws ServletException {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getJspConfigDescriptor()
     */
    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getClassLoader()
     */
    @Override
    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#declareRoles(java.lang.String...)
     */
    @Override
    public void declareRoles(String... strings) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getVirtualServerName()
     */
    @Override
    public String getVirtualServerName() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#addJspFile(java.lang.String, java.lang.String)
     */
    @Override
    public ServletRegistration.Dynamic addJspFile(String string, String string1) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getSessionTimeout()
     */
    @Override
    public int getSessionTimeout() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#setSessionTimeout(int)
     */
    @Override
    public void setSessionTimeout(int i) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getRequestCharacterEncoding()
     */
    @Override
    public String getRequestCharacterEncoding() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#setRequestCharacterEncoding(java.lang.String)
     */
    @Override
    public void setRequestCharacterEncoding(String string) {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#getResponseCharacterEncoding()
     */
    @Override
    public String getResponseCharacterEncoding() {
        throw new UnsupportedOperationException("NYI");
    }


    /**
     * Not implemented.
     *
     * @see ServletContext#setResponseCharacterEncoding(java.lang.String)
     */
    @Override
    public void setResponseCharacterEncoding(String string) {
        throw new UnsupportedOperationException("NYI");
    }

} // MockServletContext
