/*
 *
 * Copyright 2014-2016 Martin Goellnitz
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
import lombok.Getter;


/**
 * Component bean to test named injections.
 */
@Singleton
@Named
public class NamedInjection {

    @Inject
    @Named
    @Getter
    private String stringTest;

    @Inject
    @Named("b")
    @Getter
    private String stringValue;

    @Inject
    @Named
    @Getter
    private String directValue;

    @Inject
    @Named("another")
    @Getter
    private String namedValue;

    private String someValue;


    public String getSomeValue() {
        return someValue;
    }


    @Inject
    public void setSomeValue(@Named("another") String someValue) {
        this.someValue = someValue;
    }

} // NamedInjection
