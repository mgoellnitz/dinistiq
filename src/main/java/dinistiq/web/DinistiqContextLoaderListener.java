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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Context loader listener to initialize the context within a dinistiq instance.
 *
 * A dinistiq.packages init param will be necessary to find application specific components.
 *
 * The instance is store in a servlet context attribute for later use.
 */
public class DinistiqContextLoaderListener implements ServletContextListener {

    private static final Log LOG = LogFactory.getLog(DinistiqContextLoaderListener.class);

    public static final String DINISTIQ_INSTANCE = "DINISTIQ_INSTANCE";


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
            String[] packageNames = packagNameString.split(",");
            for (String packageName : packageNames) {
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
                forName.getConstructors()[0].newInstance(args);
            } catch (Exception e) {
                LOG.error("contextInitialized() cannot obtain custom class resolver", e);
            } // try/catch
        } // if
        if (classResolver==null) {
            classResolver = new SimpleClassResolver(packages);
        } // if
        try {
            Map<String, Object> externalBeans = new HashMap<String, Object>();
            externalBeans.put("servletContext", context);
            Dinistiq dinistiq = new Dinistiq(classResolver, externalBeans);
            context.setAttribute(DINISTIQ_INSTANCE, dinistiq);
        } catch (Exception ex) {
            LOG.error("init()", ex);
        } // try/catch
    } // contextInitialized()


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (LOG.isInfoEnabled()) {
            LOG.info("contextDestroyed()");
        } // if
    } // contextDestroyed()

} // DinistiqContextLoaderListener