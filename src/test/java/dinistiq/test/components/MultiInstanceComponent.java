/*
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

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Component to test the instance creation feature.
 */
@Named("strangename")
public class MultiInstanceComponent {

    @Inject
    private TestComponent testComponent;

    private String name;


    public TestComponent getTestComponent() {
        return testComponent;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

} // MultiInstanceComponent
