/**
 *
 * Copyright 2013-2020 Martin Goellnitz
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
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;


/**
 * Test vehicle class to check that without annotations no bean of this class
 * gets created automatially. Still annotations can be used inside for manual
 * creation.
 */
public class UnannotatedComponent {

    @Inject
    @Getter
    private TestComponentB b;

    private TestInterface testInterface;

    private TestInterface handWritten;

    @Getter
    @Setter
    private boolean basetypeBooleanValue;

    @Getter
    @Setter
    private Collection<Object> manuallyInjectedCollection;

    @Getter
    @Setter
    private List<String> manuallyInjectedList;


    /**
     * Get value from injection point below.
     */
    public TestInterface getAutoInjected() {
        return testInterface;
    } // getAutoInjected()


    /**
     * Injection point which is not a setter by naming.
     */
    @Inject
    public void autoInjected(TestInterface testInterface) {
        this.testInterface = testInterface;
    } // autoInjected()


    /**
     * Getter for property with different name than getter.
     */
    public TestInterface getTestInterface() {
        return handWritten;
    } // getTestInterface()


    /**
     * Setter for property with different name than getter.
     */
    public void setTestInterface(TestInterface testInterface) {
        this.handWritten = testInterface;
    } // autoInjected()

} // UnannotatedComponent
