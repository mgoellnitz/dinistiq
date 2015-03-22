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

import java.util.Set;
import javax.servlet.Servlet;


/**
 * Interface to be implemented by servlets to be registered with the web part of dinistiq.
 *
 * They must fulfill a certain regular expression for their uris, so that calls can be recognized as meant for
 * the implementing instance. Also they support an ordering to allow for URI pattern precedence and default
 * behaviour at a lower priority level.
 */
public interface RegisterableServlet extends Servlet, Comparable<RegisterableServlet> {

    /**
     * Returns a set of regular expression of which the calling URI must adhere one so that this servlet should handle it.
     *
     * @return set of regular expressions where matching URLs should be serviced by this servlet
     */
    Set<String> getUriRegex();

    /**
     * Indicator if the implementing instance should be considered earlier or later in the servlet selection process.
     *
     * @return integer indicator of order, the higher the later
     */
    int getOrder();

} // RegisterableServlet
