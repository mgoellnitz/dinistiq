/**
 *
 * Copyright 2014-2020 Martin Goellnitz
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
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import lombok.Getter;


/**
 * This class will be automatically instanciated on test start and depends
 * on a bean from the set of initial beans. Additionally this bean is
 * part of the automatically prepared beans in a web app context.
 */
@Named
@Singleton
public class InitialBeanDependentComponent {

    @Inject
    @Getter
    private ServletContext servletContext;

} // InitialBeanDependentComponent
