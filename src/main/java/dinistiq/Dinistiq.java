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

import java.beans.Introspector;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Dinistiq {

    private static final Log LOG = LogFactory.getLog(Dinistiq.class);

    private static final String PRODUCT_BASE_PATH = "dinistiq";

    private ClassResolver classResolver = new SimpleClassResolver();

    private int orderedBeanIndex = -1;

    private List<Object> orderedBeans = new ArrayList<Object>();

    private Map<String, Object> beans = new HashMap<String, Object>();


    @SuppressWarnings("unchecked")
    public <T extends Object> Set<T> findTypedBeans(Class<T> type) {
        Set<T> result = new HashSet<T>();
        for (Object bean : beans.values()) {
            if (type.isAssignableFrom(bean.getClass())) {
                LOG.info("findTypedBeans() returning "+bean+" :"+type.getName());
                result.add((T) bean);
            } // if
        } // for
        return result;
    } // findTypedBeans()


    @SuppressWarnings("unchecked")
    public <T extends Object> T findTypedBean(Class<T> type) {
        for (Object bean : beans.values()) {
            if (type.isAssignableFrom(bean.getClass())) {
                LOG.info("findTypedBean() returning "+bean+" :"+type.getName());
                return (T) bean;
            } // if
        } // for
        return null;
    } // findTypedBean()


    @SuppressWarnings("unchecked")
    public <T extends Object> T findBean(Class<? extends T> cls, String name) {
        T result = null;
        Object bean = beans.get(name);
        if (bean!=null) {
            if (bean.getClass().isAssignableFrom(cls)) {
                result = (T) bean;
            } // if
        } // if
        return result;
    } // findBean()


    private void sortBeanIntoList(Object bean) {
        final int idx = orderedBeans.indexOf(bean);
        if ((idx>orderedBeanIndex)||(idx<0)) {
            if (idx>=0) {
                orderedBeans.remove(bean);
            }// if
            orderedBeans.add(orderedBeanIndex++, bean);
        } // if
    } // sortBeanIntoList()


    private Object getValue(String customer, Class<? extends Object> cls, Type type) throws Exception {
        if (Collection.class.isAssignableFrom(cls)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getValue() collection: "+type);
            } // if
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                final Type collectionType = pType.getActualTypeArguments()[0];
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getValue() inner type "+collectionType);
                } // if
                Class<? extends Object> ct = (Class<? extends Object>) collectionType;
                Set<? extends Object> resultCollection = findTypedBeans(ct);
                if (List.class.isAssignableFrom(cls)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getValue() transforming to list");
                    } // if
                    List<Object> resultList = new ArrayList<>(resultCollection.size());
                    for (Object o : resultCollection) {
                        resultList.add(o);
                    } // for
                    return resultList;
                } // if
                return resultCollection;
            } // if
        } // if
        Object bean = findTypedBean(cls);
        if (bean==null) {
            throw new Exception("for "+customer+": bean of type "+cls.getName()+" not found.");
        } // if
        sortBeanIntoList(bean);
        return bean;
    } // getValue()


    private Object getValue(String customer, Class<?> cls, Type type, String name) throws Exception {
        if (name==null) {
            return getValue(customer, cls, type);
        } // if
        Object bean = beans.get(name);
        if (bean==null) {
            throw new Exception("for "+customer+": no bean with name '"+name+"' found.");
        } // if
        if (cls.isAssignableFrom(bean.getClass())) {
            sortBeanIntoList(bean);
            return bean;
        }
        throw new Exception("for "+customer+": "+name+" :"+cls.getName()+" not found.");
    } // getValue()


    /**
     * creates an instance of the given type and registeres it with the container.
     *
     * @param c type to create an instance of
     * @param name optional name - if null the name is taken from the @Named annotation or from the class name otherwise
     */
    private void createInstance(Class<? extends Object> c, String name) {
        LOG.info("createInstance("+name+") c="+c);
        // Check constructor
        // Instanciate and save
        try {
            final Object bean = c.newInstance();
            String beanName = (name==null) ? c.getAnnotation(Named.class).value() : name;
            if (StringUtils.isBlank(beanName)) {
                beanName = Introspector.decapitalize(c.getSimpleName());
            } // if
            beans.put(beanName, bean);
        } catch (Exception e) {
            LOG.error("createInstance() error instanciating bean of type "+c.getName(), e);
        } // try/catch
    } // createInstance()


    /**
     * Dinistiq test run.
     *
     * We need to describe which class(es) should implement an interface in a configuration file.
     *
     * We need to describe which not auto-scanned beans should be instanciated. Maybe as an alternative to the above.
     *
     * We need to describe complex value structures as vales to be used for injection into certain beans.
     */
    public Dinistiq(Set<String> packages) throws Exception {
        // to have the properties files in the path which we intend to use for configuration
        packages.add(this.getClass().getPackage().getName());
        if (LOG.isDebugEnabled()) {
            LOG.debug("()");
        } // if
        long start = System.currentTimeMillis();
        for (String pack : packages) {
            classResolver.addPackage(pack);
        } // for
        final Set<Class<Object>> classes = classResolver.getAnnotated(Named.class);
        if (LOG.isInfoEnabled()) {
            LOG.info("() "+classes.size());
        } // if
        for (Class<? extends Object> c : classes) {
            createInstance(c, null);
        } // for

        Properties beanlist = new Properties();
        final Collection<String> propertiesFilenames = classResolver.getProperties(PRODUCT_BASE_PATH+"/");
        if (LOG.isDebugEnabled()) {
            LOG.debug("() checking "+propertiesFilenames.size()+" files for properties");
        } // if
        for (String propertyResource : propertiesFilenames) {
            // ignore subfolders!
            if (LOG.isDebugEnabled()) {
                LOG.debug("() check "+propertyResource);
            } // if
            if (propertyResource.indexOf('/', PRODUCT_BASE_PATH.length()+1)<0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("() resource "+propertyResource);
                } // if
                beanlist.load(this.getClass().getClassLoader().getResourceAsStream(propertyResource));
            }// if
        } // for
        if (LOG.isInfoEnabled()) {
            LOG.info("() beanlist "+beanlist);
        } // if
        for (String key : beanlist.stringPropertyNames()) {
            String className = beanlist.getProperty(key);
            Class<? extends Object> c = Class.forName(className);
            createInstance(c, key);
        } // for

        // Fill in injections
        for (String key : beans.keySet()) {
            Object bean = beans.get(key);
            orderedBeanIndex = orderedBeans.indexOf(bean);
            if (orderedBeanIndex<0) {
                orderedBeans.add(bean);
            } // if
            orderedBeanIndex = orderedBeans.indexOf(bean);
            if (LOG.isDebugEnabled()) {
                LOG.debug("() bean "+key+": "+orderedBeanIndex+" "+orderedBeans);
            } // if

            Properties beanProperties = new Properties();
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream(PRODUCT_BASE_PATH+"/defaults/"+key+".properties");
            if (stream!=null) {
                beanProperties.load(stream);
            } // if
            stream = this.getClass().getClassLoader().getResourceAsStream(PRODUCT_BASE_PATH+"/beans/"+key+".properties");
            if (stream!=null) {
                beanProperties.load(stream);
            } // if

            // fill injected fields
            Class<? extends Object> beanClass = bean.getClass();
            String beanClassName = beanClass.getName();
            while (beanClass!=Object.class) {
                for (Field field : beanClass.getDeclaredFields()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("("+key+") field "+field.getName());
                    } // if
                    if (field.getAnnotation(Inject.class)!=null) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("("+field.getName()+" :"+field.getGenericType()+") needs injection");
                        } // if

                        String name = null;
                        Named named = field.getAnnotation(Named.class);
                        if (named!=null) {
                            name = named.value();
                        } // if

                        Object b = getValue(key, field.getType(), field.getGenericType(), name);
                        try {
                            final boolean accessible = field.isAccessible();
                            field.setAccessible(true);
                            field.set(bean, b);
                            field.setAccessible(accessible);
                        } catch (Exception e) {
                            LOG.error("() error setting field "+field.getName()+" :"+field.getType().getName()+" at '"+key+"' :"+beanClassName, e);
                        } // try/catch
                    } // if
                } // for
                beanClass = beanClass.getSuperclass();
            } // while

            // call injected setters
            for (Method m : bean.getClass().getMethods()) {
                if (m.getAnnotation(Inject.class)!=null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("() inject parameters on method "+m.getName());
                    } // if
                    Class<? extends Object>[] parameterTypes = m.getParameterTypes();
                    Type[] genericParameterTypes = m.getGenericParameterTypes();
                    Object[] parameters = new Object[parameterTypes.length];
                    final Annotation[][] parameterAnnotations = m.getParameterAnnotations();
                    for (int i = 0; i<parameterTypes.length; i++) {
                        Class<? extends Object> parameterType = parameterTypes[i];
                        String name = null;
                        Annotation[] annotations = parameterAnnotations[i];
                        for (Annotation a : annotations) {
                            if (a instanceof Named) {
                                Named named = (Named) a;
                                name = named.value();
                            } // if
                        } // for
                        parameters[i] = getValue(key, parameterType, genericParameterTypes[i], name);
                    } // for
                    try {
                        m.invoke(bean, parameters);
                    } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
                        LOG.error("() error injecting for method "+m.getName()+" at '"+key+"' :"+beanClassName, ex);
                    } // try/catch
                } // if
                if (m.getName().startsWith("set")) {
                    String propertyName = Introspector.decapitalize(m.getName().substring(3));
                    final Class<?> parameterType = m.getParameterTypes()[0];
                    final Type genericType = m.getGenericParameterTypes()[0];
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("() writable property found "+propertyName+" :"+parameterType+" "+genericType);
                    } // if
                    if (beanProperties.stringPropertyNames().contains(propertyName)) {
                        final String propertyValue = beanProperties.getProperty(propertyName);
                        boolean isBoolean = (parameterType==Boolean.class)||(m.getParameterTypes()[0]==Boolean.TYPE);
                        boolean isCollection = Collection.class.isAssignableFrom(parameterType);
                        Object[] parameters = new Object[1];
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("() trying to set value "+propertyName+" "+isBoolean+":"+isCollection);
                        } // if
                        try {
                            parameters[0] = isBoolean ? "true".equals(propertyValue) : propertyValue;
                            if (isCollection) {
                                Set<String> valueSet = new HashSet<String>();
                                for (String value : propertyValue.split(",")) {
                                    valueSet.add(value);
                                } // for
                                parameters[0] = valueSet;
                            } // if
                            m.invoke(bean, parameters);
                        } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
                            LOG.error("() error setting property "+propertyName+" to '"+propertyValue+"' at "+key+" :"+beanClassName, ex);
                        } // try/catch
                    } // if
                } // if
            } // for
        } // for

        // Call Post Construct
        if (LOG.isInfoEnabled()) {
            LOG.info("() calling post construct on ordered beans "+orderedBeans);
        } // if
        for (Object bean : orderedBeans) {
            if (LOG.isInfoEnabled()) {
                LOG.info("() bean "+bean);
            } // if
            for (Method m : bean.getClass().getMethods()) {
                if (m.getAnnotation(PostConstruct.class)!=null) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("() post construct method on "+bean+": "+m.getName());
                    } // if
                    try {
                        m.invoke(bean, new Object[0]);
                    } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
                        LOG.error("() error calling post constructor "+m.getName()+" at "+bean+" :"+bean.getClass().getName(), ex);
                    } // try/catch
                } // if
            } // for
        } // for
        if (LOG.isInfoEnabled()) {
            LOG.info("() calling post construct for the rest of the beans");
        } // if
        for (String key : beans.keySet()) {
            Object bean = beans.get(key);
            if (!orderedBeans.contains(bean)) {
                for (Method m : bean.getClass().getMethods()) {
                    if (m.getAnnotation(PostConstruct.class)!=null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("() post construct method on "+key+": "+m.getName());
                        } // if
                        try {
                            m.invoke(bean, new Object[0]);
                        } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
                            LOG.error("() error calling post constructor "+m.getName()+" at '"+key+"' :"+bean.getClass().getName(), ex);
                        } // try/catch
                    } // if
                } // for
            } // if
        } // for
        if (LOG.isInfoEnabled()) {
            LOG.info("() setup completed after "+(System.currentTimeMillis()-start)+"ms");
        } // if
    } // ()

} // Dinistiq
