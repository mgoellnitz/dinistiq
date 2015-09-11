/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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
package dinistiq.web;

import dinistiq.ClassResolver;
import dinistiq.Dinistiq;
import dinistiq.SimpleClassResolver;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Context loader listener to initialize the context with a dinistiq instance.
 *
 * A dinistiq.packages init param will be necessary to find application specific components.
 *
 * The instance is stored in a servlet context attribute for later use together with all the other bean definitions
 * from the dinistiq scope.
 */
public class DinistiqContextLoaderListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(DinistiqContextLoaderListener.class);

    /**
     * Attribute name for the context/application scope to store the dinistiq instance.
     */
    public static final String DINISTIQ_INSTANCE = "DINISTIQ_INSTANCE";

    /**
     * Init parameter name for the parameter holding the list of packages to scan for annotations.
     */
    public static final String DINISTIQ_PACKAGES = "dinistiq.packages";

    /**
     * Init parameter name for the parameter holding class resolver name.
     */
    public static final String DINISTIQ_CLASSRESOLVER = "dinistiq.class.resolver";


    /**
     * Helper method to keep areas with suppressed warnings small.
     *
     * @param <T>
     * @param className
     * @return simply returns Class.forName(classname)
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private <T extends Object> Class<T> loadClass(String className) throws ClassNotFoundException {
        return (Class<T>) Class.forName(className);
    } // loadClass()


    /**
     * Web related dinistiq initialization with parameters taken from the web.xml.
     *
     * Looks up relevant packages for scanning and a custom class resolver's implementation class name. Exposes any
     * bean from the dinistiq scope to the application scope (servlet context) of the web layer including an instance
     * of dinistiq itself.
     *
     * @param contextEvent event instance to be used while handling the initialization
     */
    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        // just to check what our log instance looks like
        LOG.warn("contextInitialized() log: {}", LOG.getClass().getName());
        ServletContext context = contextEvent.getServletContext();
        Set<String> packages = new HashSet<>();
        String packagNameString = context.getInitParameter(DINISTIQ_PACKAGES);
        if (StringUtils.isNotBlank(packagNameString)) {
            for (String packageName : packagNameString.split(",")) {
                packageName = packageName.trim();
                packages.add(packageName);
            } // for
        } // if
        String classResolverName = context.getInitParameter(DINISTIQ_CLASSRESOLVER);
        ClassResolver classResolver = null;
        if (StringUtils.isNotBlank(classResolverName)) {
            try {
                Class<?> forName = Class.forName(classResolverName);
                Object[] args = new Object[1];
                args[0] = packages;
                classResolver = (ClassResolver) forName.getConstructors()[0].newInstance(args);
            } catch (Exception e) {
                LOG.error("contextInitialized() cannot obtain custom class resolver", e);
            } // try/catch
        } // if
        LOG.info("contextInitialized() classResolver: {} :{}", classResolver, classResolverName);
        classResolver = (classResolver==null) ? new SimpleClassResolver(packages) : classResolver;
        try {
            Map<String, Object> externalBeans = new HashMap<>();
            externalBeans.put("servletContext", context);
            Dinistiq dinistiq = new Dinistiq(classResolver, externalBeans);
            context.setAttribute(DINISTIQ_INSTANCE, dinistiq);
            LOG.debug("contextInitialized() registering all beans as attribute in servlet context");
            for (String name : dinistiq.getAllBeanNames()) {
                context.setAttribute(name, dinistiq.findBean(Object.class, name));
            } // for
            LOG.debug("contextInitialized() checking injections for servlets");
            for (ServletRegistration registration : context.getServletRegistrations().values()) {
                String className = registration.getClassName();
                LOG.debug("contextInitialized() class name {}", className);
                Class<HttpServlet> servletClass = loadClass(className);
                LOG.debug("contextInitialized() class  {}", servletClass);
                for (HttpServlet servlet : dinistiq.findBeans(servletClass)) {
                    LOG.debug("contextInitialized() servlet instance {}", servlet);
                    if (servlet.getServletName().equals(registration.getName())) {
                        LOG.debug("contextInitialized() injecting into servlet instance {}", servlet);
                        dinistiq.initBean(servlet, servlet.getServletName());
                    } // if
                } // for
            } // for
        } catch (Exception ex) {
            LOG.error("init()", ex);
        } // try/catch
    } // contextInitialized()


    /**
     * Action on context destruction.
     * Empty for now.
     *
     * @param sce servlet context event fire to destroy the context
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to be cleaned up on context destruction
    } // contextDestroyed()

} // DinistiqContextLoaderListener
