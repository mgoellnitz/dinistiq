/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Named
@Singleton
public class TestComponent implements TestInterface {

    private static final Logger LOG = LoggerFactory.getLogger(TestComponent.class);


    /**
     * Post construct implementation doing nothing but logging.
     */
    @PostConstruct
    public void afterPropertiesSet() {
        LOG.info("afterPropertiesSet() YEAH!");
    } // afterPropertiesSet()


    /**
     * setMeUp method - not a setter.
     *
     * @throws Exception
     */
    public void setMeUp() throws Exception {
        // It's just the name that counts
    } // setMeUp()

} // TestComponent
