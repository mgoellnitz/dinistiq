/**
 *
 * Copyright 2014 Martin Goellnitz
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


@Named
@Singleton
public class NumericInjection extends TestComponentB {

    private int intValue;

    private long longValue;

    private float floatValue;

    private double doubleValue;

    private boolean booleanValue;


    public int getIntValue() {
        return intValue;
    }


    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }


    public void setIntValue() {
        // This method should not be consired on injection
    }


    public long getLongValue() {
        return longValue;
    }


    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }


    public float getFloatValue() {
        return floatValue;
    }


    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }


    public double getDoubleValue() {
        return doubleValue;
    }


    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }


    public boolean isBooleanValue() {
        return booleanValue;
    }


    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

} // NumericInjection
