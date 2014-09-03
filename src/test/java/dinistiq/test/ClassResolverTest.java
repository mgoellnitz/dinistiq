/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dinistiq.test;

import dinistiq.ClassResolver;
import dinistiq.SimpleClassResolver;
import dinistiq.test.components.TestComponentB;
import dinistiq.test.components.TestInterface;
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
        packages.add(TestInterface.class.getPackage().getName());
        ClassResolver resolver = new SimpleClassResolver(packages);
        Set<Class<TestComponentB>> subclasses = resolver.getSubclasses(TestComponentB.class);
        Assert.assertEquals("Cannot find implementing classes", 2, subclasses.size());
        Set<Class<TestComponentB>> annotatedSubclasses = resolver.getAnnotatedSubclasses(TestComponentB.class, Singleton.class);
        Assert.assertEquals("Cannot find annotated implementing classes", 2, annotatedSubclasses.size());
    } // testClassLoader()

} // ClassResolverTest
