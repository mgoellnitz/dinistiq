/**
 *
 * Copyright 2013-2016 Martin Goellnitz
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The dinistiq main class.
 *
 * By instanciating this class the bean creation and injection process will be started. Subsequent calls to the
 * API methods support retrieval of the singleton beans according to type, annotation, or type and annotation.
 */
public class Dinistiq {

    private static final Logger LOG = LoggerFactory.getLogger(Dinistiq.class);

    private static final String PRODUCT_BASE_PATH = "dinistiq";

    private static final String JAVALANG_PREFIX = "java.lang.";

    private static final String MAP_TYPE = "java.util.Map";

    private static final String LIST_TYPE = "java.util.List";

    private static final String SET_TYPE = "java.util.Set";

    private static final Pattern REPLACEMENT_PATTERN = Pattern.compile("\\$\\{[a-zA-Z0-9_\\.]*\\}");

    private final Map<String, String> environment = new HashMap<>(System.getenv());

    private final List<Object> orderedBeans = new ArrayList<>();

    private final Map<String, Object> beans = new HashMap<>();


    /**
     * Small helper method to keep areas with suppressed warnings small.
     *
     * @param <T>
     * @param bean
     */
    @SuppressWarnings("unchecked")
    private <T extends Object> T convert(Object bean) {
        return (T) bean;
    } // convert()


    /**
     * Find all beans of a given type.
     *
     * @param <T> type to check resulting beans for
     * @param type instance of that type
     * @return Set of beans - may be empty but not null
     */
    public <T extends Object> Set<T> findBeans(Class<T> type) {
        Set<T> result = new HashSet<>();
        for (Object bean : beans.values()) {
            if (type.isAssignableFrom(bean.getClass())) {
                LOG.info("findBeans() adding to result {}:{}", bean, type.getName());
                T b = convert(bean);
                result.add(b);
            } // if
        } // for
        return result;
    } // findBeans()


    /**
     * Find all names of beans of a given type.
     *
     * @param <T> type to check resulting beans for
     * @param type instance of that type
     * @return Set of bean names - may be empty but not null
     */
    public <T extends Object> Set<String> findNames(Class<T> type) {
        Set<String> result = new HashSet<>();
        for (String name : beans.keySet()) {
            Object bean = beans.get(name);
            if (type.isAssignableFrom(bean.getClass())) {
                LOG.info("findNames() adding to result {} :{}", bean, type.getName());
                result.add(name);
            } // if
        } // for
        return result;
    } // findNames()


    /**
     * Find all beans with a given annotation.
     *
     * @param <A> type to check resulting beans for
     * @param type instance of that type
     * @return Set of beans - may be empty but not null
     */
    public <A extends Annotation> Set<Object> findAnnotatedBeans(Class<A> type) {
        Set<Object> result = new HashSet<>();
        for (Object bean : beans.values()) {
            if (bean.getClass().getAnnotation(type)!=null) {
                LOG.info("findAnnotatedBeans() adding to result {} :{}", bean, type.getName());
                result.add(bean);
            } // if
        } // for
        return result;
    } // findAnnotatedBeans()


    /**
     * Find exactly one bean of a given type.
     * If there are more beans of that type just one of them is returned. This is fairly random by design.
     *
     * @param <T> type to check resulting bean for
     * @param type instance of that type
     * @return resulting bean or null
     */
    public <T extends Object> T findBean(Class<T> type) {
        Set<T> allBeans = findQualifiedBeans(findBeans(type), Collections.emptySet());
        LOG.info("findBean() :{} - {}", type.getSimpleName(), allBeans);
        return (allBeans.size()>0) ? allBeans.iterator().next() : null;
    } // findBean()


    /**
     * Find exactly one beans of a given type and name.
     * Return null if not both conditions are met.
     *
     * @param <T> type to check resulting bean for
     * @param cls instance of that type
     * @param name name the searched bean must have
     * @return resulting bean or null
     */
    public <T extends Object> T findBean(Class<? extends T> cls, String name) {
        T result = null;
        Object bean = beans.get(name);
        if (bean!=null) {
            LOG.info("findBean() {} :{}", name, bean.getClass().getName());
            if (cls.isAssignableFrom(bean.getClass())) {
                result = convert(bean);
            } // if
        } // if
        return result;
    } // findBean()


    /**
     * Find all beans with a given qualifier from a given set.
     *
     * @param beanSet set of beans to scan for qualified beans
     * @param <Q> Qualifier type constraint
     * @param qualifiers collection of qualifiers to find beans for
     * @return Set of beans - may be empty but not null
     */
    private <T extends Object, Q extends Annotation> Set<T> findQualifiedBeans(Collection<T> beanSet, Collection<Q> qualifiers) {
        LOG.debug("findQualifiedBeans() checking {} for {}", beanSet, qualifiers);
        Set<T> result = new HashSet<>();
        for (T bean : beanSet) {
            LOG.debug("findQualifiedBeans() checking {} for {}", bean, qualifiers);
            boolean add = true;
            Class<? extends Object> beanClass = bean.getClass();
            if (qualifiers.isEmpty()) {
                for (Annotation a : beanClass.getAnnotations()) {
                    Class<? extends Annotation> type = a.annotationType();
                    if (type.getAnnotation(Qualifier.class)!=null) {
                        LOG.debug("findQualifiedBeans() acceptable qualifier {}? {}", type.getSimpleName(), type == Named.class);
                        // don't add if the class is qualified but no qualifier is asked for in the collection.1
                        add = add&&(type == Named.class);
                        LOG.info("findQualifiedBeans() would add {}. ({})", bean, add);
                    } // if
                } // for
            } // if
            for (Q qualifier : qualifiers) {
                Class<? extends Annotation> annotationType = qualifier.annotationType();
                if (annotationType.getAnnotation(Qualifier.class)==null) {
                    throw new RuntimeException("Not a qualifier: "+annotationType+" ("+qualifier.getClass().getName()+")");
                } // if
                LOG.debug("findQualifiedBeans() checking :{} {}", annotationType.getName(), beanClass.getAnnotation(annotationType)!=null);
                LOG.debug("findQualifiedBeans() checking {}|{} {}", annotationType.getSimpleName(), beanClass.getSimpleName(), beanClass.getSimpleName().startsWith(annotationType.getSimpleName()));
                add = add&&((beanClass.getAnnotation(annotationType)!=null)||(beanClass.getSimpleName().startsWith(annotationType.getSimpleName())));
            } // for
            if (add) {
                LOG.debug("findQualifiedBeans() found qualified bean {}", bean);
                result.add(bean);
            } // if
        } // for
        return result;
    } // findQualifiedBeans()


    /**
     * Find all beans with a given qualifier.
     *
     * @param <Q> Qualifier type constraint
     * @param qualifiers collection of qualifiers to find beans for
     * @return Set of beans - may be empty but not null
     */
    public <Q extends Annotation> Set<Object> findQualifiedBeans(Collection<Q> qualifiers) {
        return findQualifiedBeans(beans.values(), qualifiers);
    } // findQualifiedBeans()


    /**
     * Find exactly one bean of a given type.
     * If there are more beans of that type just one of them is returned. This is fairly random by design.
     *
     * @param <T> type to check resulting bean for
     * @param <Q> qualifier annotation type to check resulting bean for
     * @param type instance of that type
     * @param qualifiers collection of qualifiers to find bean for
     * @return resulting bean or null
     */
    public <T extends Object, Q extends Annotation> T findBean(Class<T> type, Collection<Q> qualifiers) {
        Set<T> allBeans = findQualifiedBeans(findBeans(type), qualifiers);
        return allBeans.size()>0 ? allBeans.iterator().next() : null;
    } // findBean()


    /**
     * Return the names of all beans in the dinistiq scope.
     *
     * @return collection of all bean names
     */
    public Collection<String> getAllBeanNames() {
        return beans.keySet();
    } // getAllBeanNames()


    /**
     * Simple private class to generate provider implementations from the dinistiq scope on the fly.
     */
    private class ImplicitProvider implements Provider<Object> {

        private final Dinistiq d;

        private final Class<? extends Object> c;

        private final String name;


        /**
         * Generate provider for a given class and an optional name from dinistiq instance.
         *
         * @param name optional name of the instance - may be null
         * @param d dinistiq instance
         * @param c class to find an instance of
         */
        public ImplicitProvider(Dinistiq d, Class<? extends Object> c, String name) {
            this.d = d;
            this.c = c;
            this.name = name;
        } // ImplicitProvider()


        /**
         * Lazily find object from dinistiq scope.
         *
         * @return instance or null
         */
        @Override
        public Object get() {
            // TODO: Deal with scopes.
            return (name==null) ? d.findBean(c) : d.findBean(c, name);
        }

    } // ImplicitProvider


    /**
     * Tries to resolve the value for a given placeholder.
     *
     * @param beanProperties properties of the beans to resolve the value for
     * @param dependencies global dependencies collector to store retrieved reference values as dependecy in
     * @param customer "customer" descriptor for logging who needed the value resolved
     * @param cls target class of the value
     * @param type target type of the value
     * @param name name of the placeholder
     * @param qualifiers qualifiers for the value to fulfill
     * @return replaced value or original string
     * @throws Exception
     */
    private Object getValue(Properties beanProperties, Map<String, Set<Object>> dependencies, String customer, Class<?> cls, Type type, String name, Collection<Annotation> qualifiers) throws Exception {
        LOG.debug("getValue() expecting qualifiers {} for {} :{}", qualifiers, name, cls.getSimpleName());
        ParameterizedType parameterizedType = (type instanceof ParameterizedType) ? (ParameterizedType) type : null;
        if ((name==null)&&Collection.class.isAssignableFrom(cls)) {
            LOG.debug("getValue() collection: {}", type);
            if (parameterizedType!=null) {
                Type collectionType = parameterizedType.getActualTypeArguments()[0];
                LOG.debug("getValue() inner type {}", collectionType);
                Collection<? extends Object> resultCollection = findBeans((Class<? extends Object>) collectionType);
                resultCollection = List.class.isAssignableFrom(cls) ? new ArrayList<>(resultCollection) : resultCollection;
                if (dependencies!=null) {
                    dependencies.get(customer).addAll(resultCollection);
                } // if
                return resultCollection;
            } // if
        } // if
        Object bean = (name==null) ? findBean(cls, qualifiers) : (beanProperties.containsKey(name) ? getReferenceValue(beanProperties.getProperty(name)) : beans.get(name));
        if (Provider.class.equals(cls)) {
            Dinistiq d = this;
            Class<? extends Object> c = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            LOG.info("getValue() Provider for {} :{}", name, c);
            bean = new ImplicitProvider(d, c, name);
            String beanName = ""+bean;
            if (dependencies!=null) {
                dependencies.put(beanName, new HashSet<>());
            } // if
            beans.put(beanName, bean);
        } // if
        if (cls.isAssignableFrom(bean.getClass())) {
            if ((dependencies!=null)&&beans.containsValue(bean)) {
                dependencies.get(customer).add(bean);
            } // if
            return bean;
        } // if
        throw new Exception("for "+customer+": no bean "+(name!=null ? name+" :" : "of type ")+cls.getSimpleName()+" found.");
    } // getValue()


    /**
     * Obtain a parameter array to call a method with injections or a constructor with injections.
     *
     * @param dependencies map of dependencies for beans - pass null if you don't want to record needed dependencies
     * @param beanName name of the bean
     * @param types types array for the call
     * @param genericTypes generic type array for the call
     * @param annotations annotations of the parameters
     * @return array suitable as parameter for invoke or newInstance calls
     * @throws Exception
     */
    private Object[] getParameters(Properties beanProperties, Map<String, Set<Object>> dependencies, String beanName, Class<? extends Object>[] types, Type[] genericTypes, Annotation[][] annotations) throws Exception {
        beanProperties = beanProperties==null ? new Properties() : beanProperties;
        Object[] parameters = new Object[types.length];
        for (int i = 0; i<types.length; i++) {
            String name = null;
            Collection<Annotation> qualifiers = new HashSet<>();
            for (Annotation a : annotations[i]) {
                if (a instanceof Named) {
                    name = ((Named) a).value();
                } // if
                boolean q = false;
                for (Class<?> ii : a.getClass().getInterfaces()) {
                    LOG.info("getParameters() {}: {}", ii, ii.getAnnotation(Qualifier.class));
                    q = q||(ii.getAnnotation(Qualifier.class)!=null);
                } // for
                if (q) {
                    qualifiers.add(a);
                } // if
            } // for
            // TODO: Deal with scopes.
            parameters[i] = getValue(beanProperties, dependencies, beanName, types[i], genericTypes[i], name, qualifiers);
        } // for
        return parameters;
    } // getParameters()


    /**
     * Creates an instance of the given type and registeres it with the container.
     *
     * @param dependencies dependencies within the scope
     * @param cls type to create an instance of
     * @param beanName beans name in the scope using the given dependencies
     */
    private <T extends Object> T createInstance(Map<String, Set<Object>> dependencies, Class<T> cls, String beanName) throws Exception {
        LOG.info("createInstance({})", cls.getSimpleName());
        Constructor<?> c = null;
        Constructor<?>[] constructors = cls.getDeclaredConstructors();
        LOG.debug("createInstance({}) constructors.length={}", cls.getSimpleName(), constructors.length);
        for (Constructor<?> ctor : constructors) {
            LOG.debug("createInstance({}) {} ({})", cls.getSimpleName(), ctor, ctor.getAnnotation(Inject.class)!=null);
            c = (ctor.getAnnotation(Inject.class)!=null) ? ctor : c;
        } // for
        c = (c==null) ? cls.getConstructor() : c;
        // Don't record constructor dependencies - they MUST be already fulfilled
        Object[] parameters = getParameters(null, null, beanName, c.getParameterTypes(), c.getGenericParameterTypes(), c.getParameterAnnotations());
        dependencies.put(beanName, new HashSet<>());
        boolean accessible = c.isAccessible();
        try {
            c.setAccessible(true);
            return convert(c.newInstance(parameters));
        } finally {
            c.setAccessible(accessible);
        } // try/finally
    } // createInstance()


    /**
     * Derives a bean name from an optional given name, the beans class and this classes annotations.
     *
     * @param name intended name - may be null
     * @param cls type of the bean
     * @return name to be used for the bean
     */
    private String getBeanName(Class<? extends Object> cls, String name) {
        LOG.debug("getBeanName({}) cls={}", name, cls);
        Named annotation = cls.getAnnotation(Named.class);
        String beanName = (name==null) ? (annotation==null ? null : annotation.value()) : name;
        if (StringUtils.isBlank(beanName)) {
            beanName = Introspector.decapitalize(cls.getSimpleName());
        } // if
        return beanName;
    } //  getBeanName()


    /**
     * Creates an instance of the given type and registers it with the container.
     *
     * Adds default name resolution.
     *
     * @param cls type to create an instance of
     * @param name optional name - if null the name is taken from the at Named annotation or from the class name otherwise
     * @throws Exception when instanciation is not possible for whatever reason
     */
    private void createAndRegisterInstance(Map<String, Set<Object>> dependencies, Class<? extends Object> cls, String name) throws Exception {
        LOG.info("createAndRegisterInstance({}) cls={}", name, cls);
        String beanName = getBeanName(cls, name);
        Object bean = createInstance(dependencies, cls, beanName);
        beans.put(beanName, bean);
    } // createAndRegisterInstance()


    /**
     * Calls a method annotated as post construct on a given bean if available.
     *
     * @param bean bean to check and call post contruct annotated method on
     * @throws SecurityException
     */
    private void callPostConstruct(Object bean) throws SecurityException {
        for (Method m : bean.getClass().getMethods()) {
            if (m.getAnnotation(PostConstruct.class)!=null) {
                LOG.info("() post construct method on {}: {}", bean, m.getName());
                try {
                    m.invoke(bean, new Object[0]);
                } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
                    LOG.error("() error calling post constructor "+m.getName()+" at "+bean+" :"+bean.getClass().getName(), ex);
                } // try/catch
            } // if
        } // for
    } // callPostConstruct()


    /**
     * Common initialization parts of createBean() and initBean().
     *
     * @param bean to be initialized
     * @param name name of the bean - may not be null!
     * @param dependencies dummy dependency collector but must be the same troughout the process.
     */
    private void initBean(Object bean, String name, Map<String, Set<Object>> dependencies) {
        try {
            // TODO: Deal with scopes.
            injectDependencies(dependencies, name, bean);
            callPostConstruct(bean);
        } catch (Exception e) {
            LOG.error("initBean() "+bean.getClass(), e);
        } // try/catch
    } // initBean()


    /**
     * Create a fresh instance of a given class and inject all needed dependencies from the dinistiq scope.
     *
     * @param <T> generic type limit for bean to be created
     * @param cls class to create an instance of
     * @param name an optional name of the beans used for injection discovery - may be null
     * @return fresh instance with dependencies filled in and post contruct method called if available
     */
    public <T extends Object> T createBean(Class<T> cls, String name) {
        try {
            String beanName = getBeanName(cls, name);
            Map<String, Set<Object>> dependencies = new HashMap<>();
            T bean = createInstance(dependencies, cls, beanName);
            initBean(bean, beanName, dependencies);
            return bean;
        } catch (Exception e) {
            return null;
        } // try/catch
    } //  createBean()


    /**
     * Initialize a fresh instance by injecting all needed dependencies from the dinistiq scope.
     *
     * @param bean externally created bean with missing dependencies
     * @param name an optional name of the bean used for injection discovery - may be null
     */
    public void initBean(Object bean, String name) {
        String beanName = getBeanName(bean.getClass(), name);
        Map<String, Set<Object>> dependencies = new HashMap<>();
        dependencies.put(beanName, new HashSet<>());
        initBean(bean, beanName, dependencies);
    } //  initBean()


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
        String defaultsName = PRODUCT_BASE_PATH+"/defaults/"+key+".properties";
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(defaultsName);
        LOG.debug("getProperties({}) searching defaults {} {}", key, defaultsName, resources);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            LOG.debug("getProperties({}) loading defaults from {}", key, defaultsName);
            beanProperties.load(resource.openStream());
        } // while
        String beanValuesName = PRODUCT_BASE_PATH+"/beans/"+key+".properties";
        InputStream beanStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(beanValuesName);
        LOG.debug("getProperties({}) searching bean values {} {}", key, beanValuesName, beanStream);
        if (beanStream!=null) {
            LOG.debug("getProperties({}) loading bean values from {}", key, beanValuesName);
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
            LOG.debug("getReferenceValue({}) {}", propertyValue, referenceName);
            result = beans.containsKey(referenceName) ? beans.get(referenceName) : result;
        } // if
        if (result instanceof String) {
            String stringValue = (String) result;
            Pattern p = REPLACEMENT_PATTERN;
            Matcher m = p.matcher(stringValue);
            while (m.find()) {
                LOG.debug("getReferenceValue({}) string replacement in {}", propertyValue, stringValue);
                String name = m.group();
                name = name.substring(2, name.length()-1);
                LOG.debug("getReferenceValue({}) replacing {}", propertyValue, name);
                String replacement = beans.containsKey(name) ? ""+beans.get(name) : (environment.containsKey(name) ? environment.get(name) : "__UNKNOWN__");
                stringValue = stringValue.replace("${"+name+"}", replacement);
                m = p.matcher(stringValue);
            } // while
            result = stringValue;
        } // if
        return result;
    } // getReferenceValue()


    /**
     * Store URL parts of a given named value with suffixed names in a given map of config values.
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
                LOG.info("storeUrlParts() splitting {} ({})", value, name);
                String protocol = value.substring(0, idx);
                values.put(name+".protocol", protocol);
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
                String[] userinfos = username.split(":");
                if (userinfos.length>1) {
                    username = userinfos[0];
                    values.put(name+".password", userinfos[1]);
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
                values.put(name+".host", host);
                if (StringUtils.isNotBlank(port)) {
                    values.put(name+".port", port);
                } // if
            } catch (Exception e) {
                LOG.error("storeUrlParts() error reading "+value+" as a url", e);
            } // try/catch
        } // if
    } // storeUrlParts()


    /**
     * Fill bean as a map.
     * Replaces object references but does not split compound values like sets or lists.
     *
     * @param bean must be of type Map<Object, Object>
     * @param mapProperties properties map with the values to be added to the map bean
     */
    @SuppressWarnings("unchecked")
    private void fillMap(Object bean, Properties mapProperties) {
        Map<Object, Object> map = (Map<Object, Object>) bean;
        for (String name : mapProperties.stringPropertyNames()) {
            map.put(name, getReferenceValue(mapProperties.getProperty(name)));
        }  // while
    } // fillMap


    /**
     * Injects all available dependencies into a given bean and records all dependencies.
     *
     * @param key key / name/ id of the bean
     * @param bean bean instance
     * @param dependencies dependencies map where the dependecies of the bean are recorded with the given key
     * @throws Exception
     */
    private void injectDependencies(Map<String, Set<Object>> dependencies, String key, Object bean) throws Exception {
        // Prepare values from properties files
        Properties beanProperties = getProperties(key);
        LOG.debug("injectDependencies({}) bean properties {}", key, beanProperties.keySet());

        // fill injected fields
        Class<? extends Object> beanClass = bean.getClass();
        String beanClassName = beanClass.getName();
        while (beanClass!=Object.class) {
            if (bean instanceof Map) {
                fillMap(bean, getProperties(key));
                LOG.info("injectDependencies() filled map '{}' {}", key, bean);
                return; // If it's a map we don't need to inject anything beyond some map properties files.
            } // if
            for (Field field : beanClass.getDeclaredFields()) {
                LOG.debug("injectDependencies({}) field {}", key, field.getName());
                if (field.getAnnotation(Inject.class)!=null) {
                    Named named = field.getAnnotation(Named.class);
                    String name = (named==null) ? null : (StringUtils.isBlank(named.value()) ? field.getName() : named.value());
                    LOG.info("injectDependencies({}) {} :{} needs injection with name {}", key, field.getName(), field.getGenericType(), name);
                    Collection<Annotation> qualifiers = new HashSet<>();
                    for (Annotation a : field.getAnnotations()) {
                        boolean q = false;
                        for (Class<?> ii : a.getClass().getInterfaces()) {
                            LOG.info("getParameters() {}: {}", ii, ii.getAnnotation(Qualifier.class));
                            q = q||(ii.getAnnotation(Qualifier.class)!=null);
                        } // for
                        if (q) {
                            qualifiers.add(a);
                        } // if
                    } // for
                    // TODO: Deal with scopes.
                    Object b = getValue(beanProperties, dependencies, key, field.getType(), field.getGenericType(), name, qualifiers);
                    boolean accessible = field.isAccessible();
                    try {
                        field.setAccessible(true);
                        field.set(bean, b);
                    } catch (SecurityException|IllegalArgumentException|IllegalAccessException e) {
                        LOG.error("injectDependencies() error setting field "+field.getName()+" :"+field.getType().getName()+" at '"+key+"' :"+beanClassName, e);
                    } finally {
                        field.setAccessible(accessible);
                    } // try/catch
                } // if
            } // for
            beanClass = beanClass.getSuperclass();
        } // while

        // call methods with annotated injections
        for (Method m : bean.getClass().getMethods()) {
            if (m.getAnnotation(Inject.class)!=null) {
                LOG.debug("injectDependencies({}) inject parameters on method {}", key, m.getName());
                Class<? extends Object>[] parameterTypes = m.getParameterTypes();
                Type[] genericParameterTypes = m.getGenericParameterTypes();
                Annotation[][] parameterAnnotations = m.getParameterAnnotations();
                Object[] parameters = getParameters(beanProperties, dependencies, key, parameterTypes, genericParameterTypes, parameterAnnotations);
                try {
                    m.invoke(bean, parameters);
                } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
                    LOG.error("injectDependencies() error injecting for method "+m.getName()+" at '"+key+"' :"+beanClassName, ex);
                } // try/catch
            } // if
        } // for

        // Fill in manually set values from properties file
        // TODO: Deal with scopes - do we need a second scope variable besides beans to hold "dependent" scope beans while injecting?
        for (String property : beanProperties.stringPropertyNames()) {
            String methodName = "set"+Character.toUpperCase(property.charAt(0))+property.substring(1);
            LOG.debug("injectDependencies({}) {} -> {}", key, property, methodName);
            Method m = null;
            // Have to find it just by name
            for (Method me : bean.getClass().getMethods()) {
                if (me.getName().equals(methodName)&&(me.getParameterTypes().length>0)) {
                    m = me;
                } // if
            } // for
            if (m==null) {
                LOG.warn("injectDependencies({}) no setter method found for property {}", key, property);
            } else {
                String propertyName = Introspector.decapitalize(m.getName().substring(3));
                Class<?> parameterType = m.getParameterTypes()[0];
                Type genericType = m.getGenericParameterTypes()[0];
                LOG.debug("injectDependencies({}) writable property found {} :{} {}", key, propertyName, parameterType, genericType);
                String propertyValue = beanProperties.getProperty(propertyName); // Must definetely be there without additional check
                boolean isBoolean = (parameterType==Boolean.class)||(m.getParameterTypes()[0]==Boolean.TYPE);
                boolean isCollection = Collection.class.isAssignableFrom(parameterType);
                Object[] parameters = new Object[1];
                LOG.debug("injectDependencies({}) trying to set value {} (bool {}) (collection {}) '{}'", key, propertyName, isBoolean, isCollection, propertyValue);
                try {
                    parameters[0] = getReferenceValue(propertyValue);
                    if (isBoolean&&(parameters[0] instanceof String)) {
                        parameters[0] = Boolean.valueOf(propertyValue);
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
                        if (!Collection.class.isAssignableFrom(parameters[0].getClass())) {
                            Collection<Object> values = List.class.isAssignableFrom(parameterType) ? new ArrayList<>() : new HashSet<>();
                            for (String value : propertyValue.split(",")) {
                                Object effectiveValue = getReferenceValue(value);
                                values.add(effectiveValue);
                                if ((dependencies!=null)&&(value.indexOf("${")>=0)) {
                                    if (beans.containsValue(effectiveValue)) {
                                        dependencies.get(key).add(effectiveValue);
                                    } // if
                                } // if
                            } // for
                            parameters[0] = values;
                        } // if
                    } else {
                        if ((dependencies!=null)&&(beans.containsValue(parameters[0]))&&(propertyValue.contains("${"))) {
                            dependencies.get(key).add(parameters[0]);
                        } // if
                    } // if
                    LOG.debug("injectDependencies({}) setting value {} '{}' :{}", key, propertyName, parameters[0], parameters[0].getClass());
                    m.invoke(bean, parameters);
                } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
                    LOG.error("injectDependencies() error setting property "+propertyName+" to '"+propertyValue+"' at "+key+" :"+beanClassName, ex);
                } // try/catch
            } // if
        } // for
    }  // injectDependencies()


    /**
     * Create a dinistiq context from the given class resolver and optional external beans.
     * Add all the external named beans from thei given map for later lookup to the context as well
     * and be sure that your class resolver takes the resources in the dinistiq/ path of your
     * class path into consideration.
     *
     * @param classResolver resolver to us when resolving all types of classes
     * @param externalBeans map of beans with their id (name) as the key
     * @throws java.lang.Exception thrown with a readable message if something goes wrong
     */
    public Dinistiq(ClassResolver classResolver, Map<String, Object> externalBeans) throws Exception {
        // measure time for init process
        long start = System.currentTimeMillis();

        Map<String, Set<Object>> dependencies = new HashMap<>();

        // Use all externally provided beans
        if (externalBeans!=null) {
            beans.putAll(externalBeans);
            for (String externalBeanName : externalBeans.keySet()) {
                dependencies.put(externalBeanName, new HashSet<>());
            } // for
        } // if

        // Add system properties to scope and split potential URL values
        for (Object keyObject : System.getProperties().keySet()) {
            String key = keyObject.toString();
            beans.put(key, System.getProperty(key));
            storeUrlParts(key, System.getProperty(key), beans);
        } // for
        // Add environment to scope and split potential URL values
        for (String key : environment.keySet()) {
            storeUrlParts(key, environment.get(key), beans);
        } // for
        LOG.debug("() initial beans {}", beans);

        // Read bean list from properties files mapping names to names of the classes to be instanciated
        Properties beanlist = new Properties();
        SortedSet<String> propertiesFilenames = classResolver.getProperties(PRODUCT_BASE_PATH+"/");
        LOG.debug("() checking {} files for properties", propertiesFilenames.size());
        for (String propertyResource : propertiesFilenames) {
            LOG.debug("() check {}", propertyResource);
            // ignore subfolders!
            if (propertyResource.indexOf('/', PRODUCT_BASE_PATH.length()+1)<0) {
                LOG.debug("() resource {}", propertyResource);
                beanlist.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyResource));
            } // if
        } // for
        List<Class<?>> classList = new ArrayList<>();
        List<String> nameList = new ArrayList<>();
        for (String key : beanlist.stringPropertyNames()) {
            String className = beanlist.getProperty(key);
            if (MAP_TYPE.equals(className)) {
                beans.put(key, new HashMap<>());
                dependencies.put(key, new HashSet<>());
            } else {
                // expect java.lang.Xyz("value")
                int idx = className.indexOf('(');
                if (className.startsWith(JAVALANG_PREFIX)&&(idx>0)) {
                    String value = getReferenceValue(className.substring(idx+2, className.length()-2)).toString();
                    className = className.substring(0, idx);
                    LOG.debug("() instanciating {} :{}", value, className);
                    Class<? extends Object> c = Class.forName(className.substring(0, idx));
                    Object instance = c.getConstructor(String.class).newInstance(value);
                    LOG.info("() storing value {} :{} - {}", key, instance.getClass().getName(), instance);
                    beans.put(key, instance);
                    dependencies.put(key, new HashSet<>());
                } else {
                    boolean setType = className.startsWith(SET_TYPE);
                    if ((setType||className.startsWith(LIST_TYPE))&&(idx>0)) {
                        String values[] = getReferenceValue(className.substring(idx+1, className.length()-1)).toString().split(",");
                        Collection<String> instance = setType ? new HashSet<>(Arrays.asList(values)) : Arrays.asList(values);
                        LOG.debug("() collection {} (set {}): {}", key, setType, instance);
                        beans.put(key, instance);
                        dependencies.put(key, new HashSet<>());
                    } else {
                        LOG.debug("() listing {}", className);
                        Class<? extends Object> c = Class.forName(className);
                        classList.add(c);
                        nameList.add(key);
                    } // if
                } // if
            } // if
        } // for
        LOG.info("() beanlist {}", beanlist);

        // List annotated beans
        Set<Class<Object>> classes = classResolver.getAnnotated(Singleton.class);
        LOG.info("() number of annotated beans {}", classes.size());
        for (Class<? extends Object> c : classes) {
            classList.add(c);
            nameList.add(null);
        } // for
        LOG.debug("() beans {}", beans.keySet());

        // Instanciate beans from the properties files and from annotations taking constructor injection dependencies into account
        int ripCord = 10;
        while ((ripCord>0)&&(!classList.isEmpty())) {
            LOG.debug("() trying {} beans: {}", nameList.size(), classList);
            ripCord--;
            List<Class<?>> restClassList = new ArrayList<>();
            List<String> restNameList = new ArrayList<>();
            for (int i = 0; i<classList.size(); i++) {
                try {
                    createAndRegisterInstance(dependencies, classList.get(i), nameList.get(i));
                } catch (Exception e) {
                    LOG.warn("() will retry {} later: {} - {}", classList.get(i).getName(), e.getClass().getName(), e.getMessage(), e);
                    restClassList.add(classList.get(i));
                    restNameList.add(nameList.get(i));
                } // try/catch
            } // for
            classList = restClassList;
            nameList = restNameList;
        } // while

        // Fill in injections and note needed dependencies
        for (String key : new HashSet<>(beans.keySet())) {
            Object get = beans.get(key);
            injectDependencies(dependencies, key, get);
        } // for

        // sort beans according to dependencies
        LOG.info("() sorting beans according to dependencies");
        ripCord = 10;
        while ((ripCord>0)&&(!dependencies.isEmpty())) {
            ripCord--;
            LOG.info("() {} beans left", dependencies.size());
            Set<String> deletions = new HashSet<>();
            for (String key : dependencies.keySet()) {
                LOG.debug("() checking if {} with {} dependencies can be safely put into the ordered list {}", key, dependencies.get(key).size(), dependencies.get(key));
                boolean dependenciesMet = true;
                for (Object dep : dependencies.get(key)) {
                    boolean isMet = orderedBeans.contains(dep);
                    LOG.debug("() {} depends on {} :{} met? {} {}", key, dep, dep.getClass().getName(), isMet, ((dep instanceof Collection) ? "is a collection" : ""));
                    dependenciesMet = dependenciesMet&&isMet;
                } // for
                if (dependenciesMet) {
                    LOG.info("() adding {} to the list {}", key, orderedBeans);
                    orderedBeans.add(beans.get(key));
                    deletions.add(key);
                } // if
            } // for
            for (String key : deletions) {
                dependencies.remove(key);
            } // for
        } // while
        if (dependencies.size()>0) {
            throw new Exception("Circular bean injection and initialization dependencies detected after "+(System.currentTimeMillis()-start)+"ms"+" "+dependencies);
        } // if

        // Call Post Construct
        LOG.info("() calling post construct on ordered beans {}", orderedBeans);
        for (Object bean : orderedBeans) {
            LOG.info("() bean {}", bean);
            callPostConstruct(bean);
        } // for
        LOG.info("() calling post construct for the rest of the beans");
        for (String key : beans.keySet()) {
            Object bean = beans.get(key);
            if (!orderedBeans.contains(bean)&&!String.class.isAssignableFrom(bean.getClass())) {
                LOG.warn("() bean without dependencies to call post construct method on {} :{}", key, bean.getClass().getSimpleName());
                callPostConstruct(bean);
            } // if
        } // for
        LOG.info("() setup completed after {}ms", (System.currentTimeMillis()-start));
    } // Dinistiq()


    /**
     * Create a dinistiq context from the given packages set and the config files placed in the dinistiq/
     * substructure of the resource path.
     * Add all the external named beans from thei given map for later lookup to the context as well.
     *
     * @param packages set of java package names
     * @param externalBeans Map of beans providded externally with their respective id (name) as the key
     * @throws Exception thrown when anything goes wrong with a readable message
     */
    public Dinistiq(Set<String> packages, Map<String, Object> externalBeans) throws Exception {
        this(new SimpleClassResolver(packages), externalBeans);
    } // Dinistiq()()


    /**
     * Create a dinistiq context from the given packages set and the config files placed in the dinistiq/
     * substructure of the resource path.
     *
     * @param packages set of java package names
     * @throws Exception thrown when anything goes wrong with a readable message
     */
    public Dinistiq(Set<String> packages) throws Exception {
        this(packages, null);
    } // Dinistiq()

} // Dinistiq
