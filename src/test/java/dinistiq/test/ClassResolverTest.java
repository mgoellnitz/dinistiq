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
package dinistiq.test;

import dinistiq.ClassResolver;
import dinistiq.SimpleClassResolver;
import dinistiq.test.components.TestComponentB;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;
import org.junit.Assert;
import org.junit.Test;


public class ClassResolverTest {

    @Test
    public void testClassLoader() {
        Set<String> packages = new HashSet<String>();
        packages.add(SimpleClassResolver.class.getPackage().getName());
        packages.add("javax");
        ClassResolver resolver = new SimpleClassResolver(packages);
        Set<Class<TestComponentB>> subclasses = resolver.getSubclasses(TestComponentB.class);
        Assert.assertEquals("Cannot find implementing classes", 2, subclasses.size());
        Set<Class<TestComponentB>> annotatedSubclasses = resolver.getAnnotatedSubclasses(TestComponentB.class, Singleton.class);
        Assert.assertEquals("Cannot find annotated implementing classes", 2, annotatedSubclasses.size());
    } // testClassLoader()

} // ClassResolverTest
