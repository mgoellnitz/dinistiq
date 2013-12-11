/**
 *
 * Copyright 2013 Martin Goellnitz
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

import dinistiq.Dinistiq;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DinistiqServlet extends HttpServlet {

    private static final Log LOG = LogFactory.getLog(DinistiqServlet.class);

    /**
     * Maps URI pattern regular expressions to implementing servlets.
     */
    private final Map<Pattern, Servlet> servletMap = new HashMap<Pattern, Servlet>();

    /**
     * describes the order in which the uri pattern regular expressions should be checked.
     */
    private final List<Pattern> patternOrder = new ArrayList<Pattern>();


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO: Will be removed soon.
        req.setAttribute("tangramAdminUser", "true");

        final String requestURI = req.getRequestURI();
        int idx = requestURI.indexOf("/", 1);
        idx++;
        idx = requestURI.indexOf("/", idx);
        String uri = requestURI.substring(idx);
        if (LOG.isDebugEnabled()) {
            LOG.debug("service() requestURI="+requestURI+" / "+uri);
        } // if
        for (Pattern uriPattern : patternOrder) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("service() "+uriPattern);
            } // if
            Matcher matcher = uriPattern.matcher(uri);
            if (matcher.matches()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("service() match! "+req.getParameterMap().size());
                } // if
                servletMap.get(uriPattern).service(req, resp);
                return;
            } // if
        } // for
        resp.getWriter().write("URI cannot be resolved to any view or action on any model");
    } // service()


    @Override
    public void init(ServletConfig config) throws ServletException {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Starting... ("+LOG.getClass().getName()+")");
        } // if
        Set<String> packages = new HashSet<String>();
        final Enumeration initParameterNames = config.getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String parameterName = ""+(initParameterNames.nextElement());
            if (LOG.isDebugEnabled()) {
                LOG.debug("init() parameter "+parameterName);
            } // if
            if (parameterName.equals("dinistiq.packages")) {
                final String packagNameString = config.getInitParameter(parameterName);
                String[] packageNames = packagNameString.split(",");
                for (String packageName : packageNames) {
                    packageName = packageName.trim();
                    packages.add(packageName);
                } // for
            } // if
            if (parameterName.startsWith("dinistiq.package.")) {
                packages.add(config.getInitParameter(parameterName));
            } // if
        } // for
        try {
            Dinistiq dinistiq = new Dinistiq(packages);
            Collection<RegisterableServlet> servlets = dinistiq.findTypedBeans(RegisterableServlet.class);
            List<RegisterableServlet> orderedServlets = new ArrayList<RegisterableServlet>(servlets.size());
            for (RegisterableServlet servlet : servlets) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("init() "+servlet);
                } // if
                orderedServlets.add(servlet);
            } // for
            Collections.sort(orderedServlets);
            for (RegisterableServlet servlet : orderedServlets) {
                Set<String> uriRegexes = servlet.getUriRegex();
                for (String uriRegex : uriRegexes) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("init() * "+uriRegex);
                    } // if
                    Pattern uriPattern = Pattern.compile(uriRegex.replace("/", "\\/"));
                    patternOrder.add(uriPattern);
                    servletMap.put(uriPattern, servlet);
                } // for
            } // for
        } catch (Exception ex) {
            LOG.error("init()", ex);
        } // try/catch
    } // init()

} // DinistiqServlet
