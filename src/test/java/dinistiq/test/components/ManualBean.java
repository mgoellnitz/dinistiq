/**
 *
 * Copyright 2016 Martin Goellnitz
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
package dinistiq.test.components;

import javax.annotation.PostConstruct;


/**
 * Bean implementation to test failures createBean() and initBean() methods.
 */
public class ManualBean {

    private String indicator = "not initialized";


    public String getIndicator() {
        return indicator;
    }


    /**
     * This is an invalid method for the PostConstruct annotation.
     *
     * @param test parameter to pass - which is not possible for dinistiq.
     */
    @PostConstruct
    public void postConstruct(int test) {
        // fail to call this
        indicator = "post contruct";
    } // ()

} // ManualBean
