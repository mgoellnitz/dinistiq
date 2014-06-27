/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dinistiq {

    private static final Logger LOG = LoggerFactory.getLogger(Dinistiq.class);

    private static final String PRODUCT_BASE_PATH = "dinistiq";

    private static final String JAVALANG_PREFIX = "java.lang.";

    private static final Pattern REPLACEMENT_PATTERN = Pattern.compile("\\$\\{[a-zA-Z0-9_\\.]*\\}");

    private final Map<String, String> environment = new HashMap<String, String>(System.getenv());

    private List<Object> orderedBeans = new ArrayList<Object>();

    private Map<String, Object> beans = new HashMap<String, Object>();


    @SuppressWarnings("unchecked")
    public <T extends Object> Set<T> findTypedBeans(Class<T> type) {
        Set<T> result = new HashSet<T>();
        for (Object bean : beans.values()) {
            if (type.isAssignableFrom(bean.getClass())) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("findTypedBeans() adding to result result "+bean+" :"+type.getName());
                } // if
                result.add((T) bean);
            } // if
        } // for
        return result;
    } // findTypedBeans()


    @SuppressWarnings("unchecked")
    public <T extends Object> T findTypedBean(Class<T> type) {
        Set<T> allBeans = findTypedBeans(type);
        return allBeans.size()>0 ? allBeans.iterator().next() : null;
    } // findTypedBean()


    @SuppressWarnings("unchecked")
    public <T extends Object> T findBean(Class<? extends T> cls, String name) {
        T result = null;
        Object bean = beans.get(name);
        if (bean!=null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("findBean() "+name+" :"+bean.getClass().getName());
            } // if
            if (cls.isAssignableFrom(bean.getClass())) {
                result = (T) bean;
            } // if
        } // if
        return result;
    } // findBean()


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
                Set<? extends Object> resultCollection = findTypedBeans((Class<? extends Object>) collectionType);
                if (List.class.isAssignableFrom(cls)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getValue() transforming to list");
                    } // if
                    return new ArrayList<>(resultCollection);
                } // if
                return resultCollection;
            } // if
        } // if
        Object bean = findTypedBean(cls);
        if (bean==null) {
            throw new Exception("for "+customer+": bean of type "+cls.getName()+" not found.");
        } // if
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
            return bean;
        } // if
        throw new Exception("for "+customer+": "+name+" :"+cls.getName()+" not found.");
    } // getValue()


    /**
     * creates an instance of the given type and registeres it with the container.
     *
     * @param c type to create an instance of
     * @param name optional name - if null the name is taken from the @Named annotation or from the class name otherwise
     */
    private void createInstance(Class<? extends Object> c, String name) {
        if (LOG.isInfoEnabled()) {
            LOG.info("createInstance("+name+") c="+c);
        } // if
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
     * Get properties according to standard directory scheme from defaults and specialized properties for a given key.
     *
     * @param key key resembling the properties file name to look for in dinistiq/defaults and dinistiq/beans
     * resources folder
     * @return map collected from defaults and specialized values
     * @throws IOException
     */
    private Properties getProperties(String key) throws IOException {
        Properties beanProperties = new Properties();
        final String defaultsName = PRODUCT_BASE_PATH+"/defaults/"+key+".properties";
        final Enumeration<URL> resources = this.getClass().getClassLoader().getResources(defaultsName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProperties("+key+") searching defaults "+defaultsName+" "+resources);
        } // if
        while (resources.hasMoreElements()) {
            final URL resource = resources.nextElement();
            if (LOG.isDebugEnabled()) {
                LOG.debug("getProperties("+key+") loading defaults from "+defaultsName);
            } // if
            beanProperties.load(resource.openStream());
        } // while
        final String beanValuesName = PRODUCT_BASE_PATH+"/beans/"+key+".properties";
        InputStream beanStream = this.getClass().getClassLoader().getResourceAsStream(beanValuesName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProperties("+key+") searching bean values "+beanValuesName+" "+beanStream);
        } // if
        if (beanStream!=null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getProperties("+key+") loading bean values from "+beanValuesName);
            } // if
            beanProperties.load(beanStream);
        } // if
        return beanProperties;
    } // getProperties()


    /**
     * Tries to convert the given value as an object reference or string pattern replacement.
     *
     * @param propertyValue
     * @return referenced object or original string if unavailable
     */
    private Object getReferenceValue(String propertyValue) {
        Object result = propertyValue;
        if (propertyValue.startsWith("${")) {
            String referenceName = propertyValue.substring(2, propertyValue.length()-1);
            if (LOG.isDebugEnabled()) {
                LOG.debug("getReferenceValue("+propertyValue+") "+referenceName);
            } // if
            result = beans.containsKey(referenceName) ? beans.get(referenceName) : result;
        } // if
        if (result instanceof String) {
            String stringValue = (String) result;
            Pattern p = REPLACEMENT_PATTERN;
            Matcher m = p.matcher(stringValue);
            while (m.find()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getReferenceValue("+propertyValue+") string replacement in "+stringValue);
                } // if
                String name = m.group();
                name = name.substring(2, name.length()-1);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getReferenceValue("+propertyValue+") replacing "+name);
                } // if
                String replacement = beans.containsKey(name) ? ""+beans.get(name) : (environment.containsKey(name) ? environment.get(name) : "__UNKNOWN__");
                stringValue = stringValue.replace("${"+name+"}", replacement);
                m = p.matcher(stringValue);
            } // while
            result = stringValue;
        } // if
        return result;
    } // getReferenceValue()


    /**
     * store URL parts of a given named value with suffixed names in a given map of config values.
     *
     * @param name base name of the parts
     * @param value original value of the property
     * @param values map to store split values in
     */
    private void storeUrlParts(String name, String value, Map<String, Object> values) {
        // split
        int idx = value.indexOf("://");
        if ((idx>0)&&(value.length()>idx+5)) {
            // Might be a URL
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("storeUrlParts() splitting "+value+"("+name+")");
                } // if
                String protocol = value.substring(0, idx);
                if (StringUtils.isNotBlank(protocol)) {
                    values.put(name+".protocol", protocol);
                } // if
                idx += 3;
                String host = value.substring(idx);
                String uri = "";
                idx = host.indexOf('/');
                if (idx>0) {
                    uri = host.substring(idx+1);
                    host = host.substring(0, idx);
                } // if
                if (StringUtils.isNotBlank(uri)) {
                    values.put(name+".uri", uri);
                } // if
                String username = "";
                idx = host.indexOf('@');
                if (idx>0) {
                    username = host.substring(0, idx);
                    host = host.substring(idx+1);
                } // if
                idx = username.indexOf(':');
                if (idx>0) {
                    String[] userinfos = username.split(":");
                    if (userinfos.length>1) {
                        username = userinfos[0];
                        values.put(name+".password", userinfos[1]);
                    } // if
                } // if
                if (StringUtils.isNotBlank(username)) {
                    values.put(name+".username", username);
                } // if

                String port = "";
                idx = host.indexOf(':');
                if (idx>0) {
                    port = host.substring(idx+1);
                    host = host.substring(0, idx);
                } // if
                if (StringUtils.isNotBlank(host)) {
                    values.put(name+".host", host);
                } // if
                if (StringUtils.isNotBlank(port)) {
                    values.put(name+".port", port);
                } // if
            } catch (Exception e) {
                LOG.error("storeUrlParts() error reading "+value+" as a url", e);
            } // try/catch
        } // if
    } // storeUrlParts()


    /**
     * Create a dinistiq context from the given class resolver and optional external beans.
     * Add all the external named beans from thei given map for later lookup to the context as well
     * and be sure that your class resolver takes the resources in the dinistiq/ path of your
     * class path into cosideration.
     */
    public Dinistiq(ClassResolver classResolver, Map<String, Object> externalBeans) throws Exception {
        // Use all externally provided beans
        if (externalBeans!=null) {
            beans.putAll(externalBeans);
        } // if

        // Add environment to scope and split potential URL values
        for (String key : environment.keySet()) {
            storeUrlParts(key, environment.get(key), beans);
        } // for
        if (LOG.isDebugEnabled()) {
            LOG.debug("() initial beans "+beans);
        } // if

        // measure time for init process
        long start = System.currentTimeMillis();

        // Instanciate annotated beans
        final Set<Class<Object>> classes = classResolver.getAnnotated(Named.class);
        if (LOG.isInfoEnabled()) {
            LOG.info("() number of annotated beans "+classes.size());
        } // if
        for (Class<? extends Object> c : classes) {
            createInstance(c, null);
        } // for

        // Read bean list from properties files mapping names to names of the classes to be instanciated
        Properties beanlist = new Properties();
        SortedSet<String> propertiesFilenames = classResolver.getProperties(PRODUCT_BASE_PATH+"/");
        if (LOG.isDebugEnabled()) {
            LOG.debug("() checking "+propertiesFilenames.size()+" files for properties");
        } // if
        for (String propertyResource : propertiesFilenames) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("() check "+propertyResource);
            } // if
            // ignore subfolders!
            if (propertyResource.indexOf('/', PRODUCT_BASE_PATH.length()+1)<0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("() resource "+propertyResource);
                } // if
                beanlist.load(this.getClass().getClassLoader().getResourceAsStream(propertyResource));
            } // if
        } // for
        if (LOG.isInfoEnabled()) {
            LOG.info("() beanlist "+beanlist);
        } // if
        for (String key : beanlist.stringPropertyNames()) {
            String className = beanlist.getProperty(key);
            if ("java.util.Map".equals(className)) {
                Properties mapProperties = getProperties(key);
                Map<Object, Object> map = new HashMap<>();
                for (String name : mapProperties.stringPropertyNames()) {
                    map.put(name, getReferenceValue(mapProperties.getProperty(name)));
                }  // while
                if (LOG.isInfoEnabled()) {
                    LOG.info("() creating map '"+key+"' "+map);
                } // if
                beans.put(key, map);
            } else {
                // expect java.lang.Xyz("value")
                int idx = className.indexOf('(');
                if ((className.startsWith(JAVALANG_PREFIX))&&(idx>0)) {
                    String value = ""+getReferenceValue(className.substring(idx+2, className.length()-2));
                    className = className.substring(0, idx);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("() instanciating "+value+" :"+className);
                    } // if
                    Class<? extends Object> c = Class.forName(className.substring(0, idx));
                    Object instance = c.getConstructor(String.class).newInstance(value);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("() storing value "+key+" :"+instance.getClass().getName()+" - "+instance);
                    } // if
                    beans.put(key, instance);
                } else {
                    Class<? extends Object> c = Class.forName(className);
                    createInstance(c, key);
                } // if
            } // if
        } // for

        if (LOG.isDebugEnabled()) {
            LOG.debug("() beans "+beans.keySet());
        } // if

        // Fill in injections and note needed dependencies
        Map<String, Set<Object>> dependencies = new HashMap<String, Set<Object>>();
        for (String key : beans.keySet()) {
            Object bean = beans.get(key);

            // note dependencies
            Set<Object> beanDependencies = new HashSet<Object>();
            dependencies.put(key, beanDependencies);

            // Prepare values from properties files
            Properties beanProperties = getProperties(key);
            if (LOG.isDebugEnabled()) {
                LOG.debug("("+key+") bean properties"+beanProperties.keySet());
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
                        Named named = field.getAnnotation(Named.class);
                        String name = (named==null) ? null : named.value();
                        if (LOG.isInfoEnabled()) {
                            LOG.info("("+field.getName()+" :"+field.getGenericType()+") needs injection with name "+name);
                        } // if

                        Object b = getValue(key, field.getType(), field.getGenericType(), name);
                        final boolean accessible = field.isAccessible();
                        try {
                            field.setAccessible(true);
                            field.set(bean, b);
                            beanDependencies.add(b);
                            // TODO: If b is no singleton it should be removed from the list and potentially a new instance should be created
                        } catch (Exception e) {
                            LOG.error("() error setting field "+field.getName()+" :"+field.getType().getName()+" at '"+key+"' :"+beanClassName, e);
                        } finally {
                            field.setAccessible(accessible);
                        } // try/catch
                    } // if
                } // for
                beanClass = beanClass.getSuperclass();
            } // while

            // call injected setters
            for (Method m : bean.getClass().getMethods()) {
                if (m.getAnnotation(Inject.class)!=null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("("+key+") inject parameters on method "+m.getName());
                    } // if
                    Class<? extends Object>[] parameterTypes = m.getParameterTypes();
                    Type[] genericParameterTypes = m.getGenericParameterTypes();
                    Object[] parameters = new Object[parameterTypes.length];
                    final Annotation[][] parameterAnnotations = m.getParameterAnnotations();
                    for (int i = 0; i<parameterTypes.length; i++) {
                        Class<? extends Object> parameterType = parameterTypes[i];
                        String name = null;
                        for (Annotation a : parameterAnnotations[i]) {
                            if (a instanceof Named) {
                                name = ((Named) a).value();
                            } // if
                        } // for
                        parameters[i] = getValue(key, parameterType, genericParameterTypes[i], name);
                        beanDependencies.add(parameters[i]);
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
                        LOG.debug("("+key+") writable property found "+propertyName+" :"+parameterType+" "+genericType);
                    } // if
                    if (beanProperties.stringPropertyNames().contains(propertyName)) {
                        final String propertyValue = beanProperties.getProperty(propertyName);
                        boolean isBoolean = (parameterType==Boolean.class)||(m.getParameterTypes()[0]==Boolean.TYPE);
                        boolean isCollection = Collection.class.isAssignableFrom(parameterType);
                        Object[] parameters = new Object[1];
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("("+key+") trying to set value "+propertyName+" "+isBoolean+":"+isCollection+" "+propertyValue);
                        } // if
                        try {
                            parameters[0] = getReferenceValue(propertyValue);
                            if (isBoolean&&(parameters[0] instanceof String)) {
                                parameters[0] = "true".equals(propertyValue);
                            } // if
                            if ("long".equals(parameterType.getName())) {
                                parameters[0] = new Long(propertyValue);
                            } // if
                            if ("int".equals(parameterType.getName())) {
                                parameters[0] = new Integer(propertyValue);
                            } // if
                            if ("float".equals(parameterType.getName())) {
                                parameters[0] = new Float(propertyValue);
                            } // if
                            if ("double".equals(parameterType.getName())) {
                                parameters[0] = new Double(propertyValue);
                            } // if
                            if (isCollection) {
                                Set<Object> valueSet = new HashSet<Object>();
                                for (String value : propertyValue.split(",")) {
                                    valueSet.add(getReferenceValue(value));
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

        // sort beans according to dependencies
        if (LOG.isInfoEnabled()) {
            LOG.info("() sorting beans according to dependencies");
        } // if
        while (dependencies.size()>0) {
            if (LOG.isInfoEnabled()) {
                LOG.info("() "+dependencies.size()+" beans left");
            } // if
            Set<String> deletions = new HashSet<String>();
            for (String key : dependencies.keySet()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("() checking if "+key+" can be safely put into the ordered list");
                } // if
                boolean dependenciesMet = true;
                for (Object dep : dependencies.get(key)) {
                    boolean isMet = true;
                    if (dep instanceof Collection) {
                        @SuppressWarnings("unchecked")
                        Collection<Object> depCollection = (Collection<Object>) dep;
                        for (Object d : depCollection) {
                            isMet = isMet&&orderedBeans.contains(d);
                        } // for
                    } else {
                        isMet = orderedBeans.contains(dep);
                    } // if
                    if (LOG.isDebugEnabled()) {
                        if (!isMet) {
                            LOG.debug("() "+key+" is missing "+dep+" :"+dep.getClass().getName());
                        } // if
                    } // if
                    dependenciesMet = dependenciesMet&&isMet;
                } // for
                if (dependenciesMet) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("() adding "+key+" to the list "+orderedBeans);
                    } // if
                    orderedBeans.add(beans.get(key));
                    deletions.add(key);
                } // if
            } // for
            for (String key : deletions) {
                dependencies.remove(key);
            } // for
        } // while

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
    } // Dinistiq()


    /**
     * Create a dinistiq context from the given packages set and the config files placed in the dinistiq/
     * substructur of the resource path.
     * Add all the external named beans from thei given map for later lookup to the context as well.
     */
    public Dinistiq(Set<String> packages, Map<String, Object> externalBeans) throws Exception {
        this(new SimpleClassResolver(packages), externalBeans);
    } // Dinistiq()()


    /**
     * Create a dinistiq context from the given packages set and the config files placed in the dinistiq/
     * substructur of the resource path.
     */
    public Dinistiq(Set<String> packages) throws Exception {
        this(packages, null);
    } // Dinistiq()


} // Dinistiq
