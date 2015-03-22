/*
 *
 * Copyright 2014-2015 Martin Goellnitz
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
import javax.inject.Singleton;


/**
 * Component bean to test named injections.
 */
@Singleton
@Named
public class NamedInjection {

    @Inject
    @Named
    private String stringTest;

    @Inject
    @Named("b")
    private String stringValue;

    @Inject
    @Named
    private String directValue;

    @Inject
    @Named("another")
    private String namedValue;

    private String someValue;


    public String getStringTest() {
        return stringTest;
    }


    public String getStringValue() {
        return stringValue;
    }


    public String getDirectValue() {
        return directValue;
    }


    public String getNamedValue() {
        return namedValue;
    }


    public String getSomeValue() {
        return someValue;
    }


    @Inject
    public void setSomeValue(@Named("another") String someValue) {
        this.someValue = someValue;
    }

} // NamedInjection
