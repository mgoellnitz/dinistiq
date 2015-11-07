/*
 *
 * Copyright 2015 Martin Goellnitz
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

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Non singleton scoped bean.
 *
 * Inject a (singleton) string from properties, a discovered singleton, an a dependent bean.
 * The latter one needing fresh instances on every creation of this beans.
 */
@Named
public class ScopedBean {

    @Inject
    @Named("another")
    private String namedValue;

    @Inject
    private TestInterface testInterface;


    public String getNamedValue() {
        return namedValue;
    }


    public TestInterface getTestInterface() {
        return testInterface;
    }

} // ScopedBean
