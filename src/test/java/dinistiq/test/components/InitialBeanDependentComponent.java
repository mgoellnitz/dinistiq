/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dinistiq.test.components;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;


@Named
@Singleton
public class InitialBeanDependentComponent {

    @Inject
    private ServletContext servletContext;


    public ServletContext getServletContext() {
        return servletContext;
    }

} // InitialBeanDependentComponent
