/*
 *
 * Copyright 2015 Martin Goellnitz
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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;


/**
 * Test vehicle to test set/collections automatically collected by the framework.
 */
@Named
@Singleton
public class CollectionReferences {

    private Set<String> stringSet;

    private List<String> stringList;

    @Inject
    private Collection<TestInterface> testInstances;


    public void setStringSet(Set<String> stringSet) {
        this.stringSet = stringSet;
    }


    public Set<String> getStringSet() {
        return stringSet;
    }


    public List<String> getStringList() {
        return stringList;
    }


    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }


    public Collection<TestInterface> getTestInstances() {
        return testInstances;
    }

} // CollectionReferences
