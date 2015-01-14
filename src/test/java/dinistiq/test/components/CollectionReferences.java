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

import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;


/**
 *
 */
@Named
@Singleton
public class CollectionReferences {

    private Set<String> stringSet;


    public void setStringSet(Set<String> stringSet) {
        this.stringSet = stringSet;
    }


    public Set<String> getStringSet() {
        return stringSet;
    }

} // CollectionReferences
