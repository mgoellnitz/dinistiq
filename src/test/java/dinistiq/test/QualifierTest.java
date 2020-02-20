/**
 *
 * Copyright 2016-2020 Martin Goellnitz
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
import dinistiq.Dinistiq;
import dinistiq.SimpleClassResolver;
import dinistiq.test.components.InitialBean;
import dinistiq.test.components.QualifiedComponent;
import dinistiq.test.components.QualifiedInjection;
import dinistiq.test.components.TestInterface;
import dinistiq.web.test.MockServletContext;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Scope;
import org.atinject.tck.auto.Car;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test selection of injected beans based on qualifiers.
 */
public class QualifierTest {

    private Dinistiq d;


    /**
     * Prepare a map of beans used as initial beans on dinistiq instanciation.
     *
     * @return map of named beans.
     */
    public static Map<String, Object> prepareInitialBeans() {
        Map<String, Object> initialBeans = new HashMap<>();
        initialBeans.put("initialBean", new InitialBean());
        initialBeans.put("servetContext", new MockServletContext(null));
        return initialBeans;
    } // prepareInitialBeans()


    /**
     * Set up this test execution class with a special dinisitq setup for qualifiers.
     */
    public QualifierTest() {
        Set<String> packages = new HashSet<>();
        packages.add(TestInterface.class.getPackage().getName());
        packages.add(Car.class.getPackage().getName());
        try {
            d = new Dinistiq(packages, prepareInitialBeans());
        } catch (Exception e) {
            Assert.fail(e.getClass().getSimpleName()+": "+e.getMessage());
            Assert.assertNotNull(d, "DI container could not be initialized");
        } // try/catch
    } // QualifierTest()


    /**
     * Test if beans get handled depending on their respective given qualifiers.
     */
    @Test
    public void testQualifiedBeans() {
        Named n = new Named() {
            /**
             * Get named for named annotation.
             */
            @Override
            public String value() {
                return "test";
            }


            /**
             * Get annotation type for named annotation.
             */
            @Override
            public Class<? extends Annotation> annotationType() {
                return Named.class;
            }

        };
        Set<Annotation> qualifiers = new HashSet<>();
        qualifiers.add(n);
        Set<Object> qualifiedBeans = d.findQualifiedBeans(qualifiers);
        Assert.assertEquals(qualifiedBeans.size(), 8, "Unexpected number of quaified beans discovered in dinistiq scope.");
        boolean thrown = false;
        try {
            Scope s = new Scope() {
                /**
                 * Get annotation type.
                 */
                @Override
                public Class<? extends Annotation> annotationType() {
                    return Scope.class;
                }

            };
            qualifiers = new HashSet<>();
            qualifiers.add(s);
            d.findQualifiedBeans(qualifiers);
        } catch (Exception e) {
            if (e.getMessage().startsWith("Not a qualifier")) {
                thrown = true;
            } // if
        } // try/catch
        Assert.assertTrue(thrown, "Should not issue results when no qualifier is given.");
    } // testQualifiedBeans()


    /**
     * Test scope handling with the given single scope of dinistiq.
     */
    @Test
    public void testScopes() {
        Set<String> packs = new HashSet<>();
        packs.add(Scope.class.getPackage().getName());
        ClassResolver classResolver = new SimpleClassResolver(packs);
        Set<Class<Object>> scopes = classResolver.getAnnotatedItems(Scope.class);
        Assert.assertEquals(scopes.size(), 1, "Unexpected number of scopes discovered in class path.");
    } // testScopes()


    /**
     * Test injection of beans depending on a given qualifier.
     */
    @Test
    public void testQualifiedInjection() {
        QualifiedInjection qualifiedInjection = d.findBean(QualifiedInjection.class);
        Assert.assertNotNull(qualifiedInjection, "Qualified injection point object should be found.");
        TestInterface testInterface = qualifiedInjection.getTestInterface();
        Assert.assertNotNull(testInterface, "Test interface instance should have be injected.");
        Assert.assertEquals(testInterface.getClass(), QualifiedComponent.class, "Injected instance should be of type marked as qualified.");
        Assert.assertEquals(qualifiedInjection.getConstructorInjected().getClass(), QualifiedComponent.class, "Injected instance should be of type marked as qualified.");
    } // testQualifiedInjection()

} // QualifierTest
