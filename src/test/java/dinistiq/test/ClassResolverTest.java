/**
 *
 * Copyright 2014-2017 Martin Goellnitz
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

import dinistiq.ClassResolver;
import dinistiq.SimpleClassResolver;
import dinistiq.test.components.TestInterface;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test usage of custom class loader.
 *
 * Still using the default implementation.
 */
public class ClassResolverTest {

    /**
     * Test class loading with a given class loader.
     */
    @Test
    public void testClassLoader() {
        Set<String> packages = new HashSet<>();
        packages.add(TestInterface.class.getPackage().getName());
        packages.add("javax");
        ClassResolver resolver = new SimpleClassResolver(packages);
        Set<Class<TestInterface>> subclasses = resolver.getSubclasses(TestInterface.class);
        Assert.assertEquals(subclasses.size(), 2, "Cannot find expected number of implementing classes");
        Set<Class<TestInterface>> annotatedSubclasses = resolver.getAnnotatedSubclasses(TestInterface.class, Singleton.class);
        Assert.assertEquals(annotatedSubclasses.size(), 3, "Cannot find expected number of implementing classes annotated as singleton");
    } // testClassLoader()

} // ClassResolverTest
