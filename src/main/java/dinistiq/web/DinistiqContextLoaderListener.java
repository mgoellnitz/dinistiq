/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Context loader listener to initialize the context within a dinistiq instance.
 *
 * A dinistiq.packages init param will be necessary to find application specific components.
 *
 * The instance is store in a servlet context attribute for later use together with all the other
 * bean definitions from the dinistiq scope.
 */
public class DinistiqContextLoaderListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(DinistiqContextLoaderListener.class);

    public static final String DINISTIQ_INSTANCE = "DINISTIQ_INSTANCE";


    /**
     * Web related dinistiq initialization with parameters taken from the web.xml.
     *
     * Looks up relevant packages for scanning and a custom class resolvers implementation class name.
     * Exposes any bean from the dinistiq scope to the application scope (servlet context) of the web
     * layer including an instance of dinistiq itself.
     *
     * @param contextEnvironment
     */
    @Override
    public void contextInitialized(ServletContextEvent contextEnvironment) {
        // just to check what our log instance looks like
        if (LOG.isWarnEnabled()) {
            LOG.warn("contextInitialized() log: "+LOG.getClass().getName());
        } // if
        ServletContext context = contextEnvironment.getServletContext();
        Set<String> packages = new HashSet<String>();
        final String packagNameString = context.getInitParameter("dinistiq.packages");
        if (StringUtils.isNotBlank(packagNameString)) {
            for (String packageName : packagNameString.split(",")) {
                packageName = packageName.trim();
                packages.add(packageName);
            } // for
        } // if
        final String classResolverName = context.getInitParameter("dinistiq.class.resolver");
        ClassResolver classResolver = null;
        if (StringUtils.isNotBlank(classResolverName)) {
            try {
                final Class<?> forName = Class.forName(classResolverName);
                Object[] args = new Object[1];
                args[0] = packages;
                classResolver = (ClassResolver) forName.getConstructors()[0].newInstance(args);
            } catch (Exception e) {
                LOG.error("contextInitialized() cannot obtain custom class resolver", e);
            } // try/catch
        } // if
        if (LOG.isInfoEnabled()) {
            LOG.info("contextInitialized() classResolver: "+classResolver+" :"+classResolverName);
        } // if
        classResolver = (classResolver==null) ? new SimpleClassResolver(packages) : classResolver;
        try {
            Map<String, Object> externalBeans = new HashMap<String, Object>();
            externalBeans.put("servletContext", context);
            Dinistiq dinistiq = new Dinistiq(classResolver, externalBeans);
            context.setAttribute(DINISTIQ_INSTANCE, dinistiq);
            for (String name : dinistiq.getAllBeansNames()) {
                context.setAttribute(name, dinistiq.findBean(Object.class, name));
            } // for
        } catch (Exception ex) {
            LOG.error("init()", ex);
        } // try/catch
    } // contextInitialized()


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to be clean up on context destruction
    } // contextDestroyed()

} // DinistiqContextLoaderListener
