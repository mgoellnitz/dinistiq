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

import java.util.Set;
import javax.servlet.Servlet;


/**
 * Servlets which should handle requests fulfilling a certain regular expression for their uris.
 */
public interface RegisterableServlet extends Servlet, Comparable<RegisterableServlet> {

    /**
     * Returns a set of regular expression of which the calling URI must adhere one so that this servlet should handle it.
     */
    Set<String> getUriRegex();

    /**
     * returns an integer indicating if the implementing instance should be considered earlier or later in
     * the servlet selection process.
     */
    int getOrder();

} // RegisterableServlet
