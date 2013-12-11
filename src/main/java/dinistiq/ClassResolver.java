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
import java.util.Collection;
import java.util.Set;


/**
 * Instances resolv classes according to a set of package, type and annotations.
 */
public interface ClassResolver {

    void addPackage(String packageName);


    <T extends Object> Set<Class<T>> getSubclasses(Class<T> type);


    <T extends Object> Set<Class<T>> getAnnotated(Class<? extends Annotation> annotation);


    <T extends Object> Set<Class<T>> getAnnotatedSubclasses(Class<T> c, Class<? extends Annotation> annotation);


    Collection<String> getProperties(String path);

} // ClassResolver
