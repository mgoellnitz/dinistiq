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

import dinistiq.Dinistiq;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Front controller servlet for all dinistiq related calls.
 *
 * All servlets depending on injected components should implement the registrable servlet interface to be registered
 * with this one and be considered and called in the correct order according to their respective URL patterns.
 */
public class DinistiqServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(DinistiqServlet.class);

    /**
     * Maps URI pattern regular expressions to implementing servlets.
     */
    private final Map<Pattern, Servlet> servletMap = new HashMap<>();

    /**
     * Describes the order in which the uri pattern's regular expressions should be checked.
     */
    private final List<Pattern> patternOrder = new ArrayList<>();


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contextPath = req.getContextPath();
        contextPath = (contextPath.length()==1) ? "" : contextPath;
        String requestURI = req.getRequestURI();
        int slashIndex = requestURI.indexOf("/", contextPath.length()+1);
        String uri = slashIndex<0 ? "" : requestURI.substring(slashIndex);
        LOG.debug("service({}:{}) requestURI={} / {}", contextPath, slashIndex, requestURI, uri);
        for (Pattern uriPattern : patternOrder) {
            LOG.debug("service() {}", uriPattern);
            Matcher matcher = uriPattern.matcher(uri);
            if (matcher.matches()) {
                LOG.debug("service() match! {}", req.getParameterMap().size());
                servletMap.get(uriPattern).service(req, resp);
                return;
            } // if
        } // for
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "URI cannot be resolved to any view or action on any model.");
    } // service()


    @Override
    public void init(ServletConfig config) throws ServletException {
        LOG.warn("init() ({})", LOG.getClass().getName());
        try {
            Dinistiq dinistiq = (Dinistiq) (config.getServletContext().getAttribute(DinistiqContextLoaderListener.DINISTIQ_INSTANCE));
            Collection<RegisterableServlet> servlets = dinistiq.findBeans(RegisterableServlet.class);
            List<RegisterableServlet> orderedServlets = new ArrayList<>(servlets.size());
            for (RegisterableServlet servlet : servlets) {
                LOG.debug("init() {}", servlet);
                orderedServlets.add(servlet);
            } // for
            Collections.sort(orderedServlets);
            for (RegisterableServlet servlet : orderedServlets) {
                for (String uriRegex : servlet.getUriRegex()) {
                    LOG.debug("init() * {}", uriRegex);
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
