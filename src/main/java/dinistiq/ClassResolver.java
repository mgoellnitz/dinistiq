/**
 *
 * Copyright 2013 Martin Goellnitz
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
package dinistiq;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.SortedSet;


/**
 * Instances resolve classes from a given set of packages according to type and annotations.
 */
public interface ClassResolver {

    /**
     * Get all available subclasses of a given class which are not abstract.
     *
     * @param <T> generic type variable for the result set
     * @param type type to search subclasses for
     * @return Set of classes implementing the given type
     */
    <T extends Object> Set<Class<T>> getSubclasses(Class<T> type);


    /**
     * Get all classes annotated with a given annotation which are not abstract and no interface.
     *
     * @param <T> generic type variable for the result set
     * @param annotation annotation instance resulting classes should adhere
     * @return Set of classes with the given annotation
     */
    <T extends Object> Set<Class<T>> getAnnotated(Class<? extends Annotation> annotation);


    /**
     * Get all available subclasses of a given type annotated with a given annotation which are no interface.
     *
     * @param <T> generic type variable for the result set
     * @param c type to search subclasses for
     * @param annotation annotation instance resulting classes should adhere
     * @return Set of class instances fulfilling the condition
     */
    <T extends Object> Set<Class<T>> getAnnotatedSubclasses(Class<T> c, Class<? extends Annotation> annotation);


    /**
     * Get the filenames of properties files in a given path in alphabetical order.
     *
     * @param path file or resource path to scan.
     * @return Sorted set of property files paths
     */
    SortedSet<String> getProperties(String path);

} // ClassResolver
