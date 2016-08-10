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
package dinistiq.test;

import dinistiq.Dinistiq;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test the custom string utils methods introduced to remove the commons lang(3) dependency.
 */
public class StringUtilsTest {

    @Test
    public void testStringUtils() {
        Assert.assertTrue(Dinistiq.isEmpty(null), "Null is an empty string.");
        Assert.assertTrue(Dinistiq.isEmpty(""), "The empty string is an empty string.");
        Assert.assertFalse(Dinistiq.isEmpty(" "), "One space may not be an empty string.");
        Assert.assertTrue(Dinistiq.isNotBlank("a b d"), "A random string is not a blank string.");
        Assert.assertFalse(Dinistiq.isNotBlank(" "), "One space is a blank string.");
        Assert.assertFalse(Dinistiq.isNotBlank("\t "), "Tabs are whitespaces for a blank string.");
        Assert.assertFalse(Dinistiq.isNotBlank(""), "The empty string is a blank string.");
        Assert.assertFalse(Dinistiq.isNotBlank(null), "Null is a blank string.");
    } // testStringUtils()

} // StringUtilsTest
