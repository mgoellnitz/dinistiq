/**
 *
 * Copyright 2014-2020 Martin Goellnitz
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

import javax.inject.Named;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;


/**
 * Test vehicle bean class as a for different types of numeric injection.
 */
@Named
@Singleton
public class NumericInjection extends TestComponentB {

    @Getter
    @Setter
    private int intValue;

    @Getter
    @Setter
    private long longValue;

    @Getter
    @Setter
    private float floatValue;

    @Getter
    @Setter
    private double doubleValue;

    @Getter
    @Setter
    private boolean booleanValue;


    /**
     * Method with setter name patter but not parameter.
     * It should be ignored on injection.
     */
    public void setIntValue() {
        // This method should not be consired on injection
    }

} // NumericInjection
