/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dinistiq.web.test;

import dinistiq.Dinistiq;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;


/**
 *
 */
public class MockServletConfig implements ServletConfig {

    private final Dinistiq dinistiq;


    public MockServletConfig(Dinistiq dinistiq) {
        this.dinistiq = dinistiq;
    }


    @Override
    public String getServletName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public ServletContext getServletContext() {
        return new MockServletContext(dinistiq);
    }


    @Override
    public String getInitParameter(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public Enumeration<Object> getInitParameterNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

} // MockServletConfig()
