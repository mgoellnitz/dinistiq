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
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
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

    private static final Pattern REPLACEMENT_PATTERN = Pattern.compile("\\$\\{[a-zA-Z0-9_\\.]*\\}");

    private final Map<String, String> environment = new HashMap<>(System.getenv());

    private final List<Object> orderedBeans = new ArrayList<>();

    private final Map<String, Object> beans = new HashMap<>();


    /**
     * small helper method to keep areas with suppressed warnings small.
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
        Set<T> allBeans = findBeans(type);
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
     * return the names of all beans in the inistiq scope.
     *
     * @return collection of all bean names
     */
    public Collection<String> getAllBeanNames() {
        return beans.keySet();
    } // getAllBeanNames()


    private Object getValue(Map<String, Set<Object>> dependencies, String customer, Class<?> cls, Type type, final String name) throws Exception {
        ParameterizedType parameterizedType = (type instanceof ParameterizedType) ? (ParameterizedType) type : null;
        if ((name==null)&&Collection.class.isAssignableFrom(cls)) {
            LOG.debug("getValue() collection: {}", type);
            if (parameterizedType!=null) {
                Type collectionType = parameterizedType.getActualTypeArguments()[0];
                LOG.debug("getValue() inner type {}", collectionType);
                Set<? extends Object> resultCollection = findBeans((Class<? extends Object>) collectionType);
                if (List.class.isAssignableFrom(cls)) {
                    LOG.debug("getValue() transforming to list");
                    return new ArrayList<>(resultCollection);
                } // if
                return resultCollection;
            } // if
        } // if
        Object bean = (name==null) ? findBean(cls) : beans.get(name);
        if (Provider.class.equals(cls)) {
            final Dinistiq d = this;
            final Class<? extends Object> c = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            LOG.info("getValue() Provider for {} :{}", name, c);
            bean = new Provider() {

                @Override
                public Object get() {
                    return (name==null) ? d.findBean(c) : d.findBean(c, name);
                }

            };
            String beanName = ""+bean;
            if (dependencies!=null) {
                dependencies.put(beanName, new HashSet<>());
            } // if
            beans.put(beanName, bean);
        } // if
        if (cls.isAssignableFrom(bean.getClass())) {
            return bean;
        } // if
        throw new Exception("for "+customer+": no bean "+(name != null ? name+" :" : "of type ")+cls.getSimpleName()+" found.");
    } // getValue()


    /**
     * Obtain a parameter array to call a method with injections or a constructor with injections
     *
     * @param dependencies map of dependencies for beans - pass null if you don't want to record needed dependencies
     * @param beanName name of the bean
     * @param types types array for the call
     * @param genericTypes generic type array for the call
     * @param annotations annotations of the parameters
     * @return array suitable as parameter for invoke or newInstance calls
     * @throws Exception
     */
    private Object[] getParameters(Map<String, Set<Object>> dependencies, String beanName, Class<? extends Object>[] types, Type[] genericTypes, Annotation[][] annotations) throws Exception {
        Object[] parameters = new Object[types.length];
        for (int i = 0; i<types.length; i++) {
            String name = null;
            for (Annotation a : annotations[i]) {
                if (a instanceof Named) {
                    name = ((Named) a).value();
                } // if
            } // for
            parameters[i] = getValue(dependencies, beanName, types[i], genericTypes[i], name);
            if (dependencies!=null) {
                dependencies.get(beanName).add(parameters[i]);
            } // if
        } // for
        return parameters;
    } // getParameters()


    /**
     * creates an instance of the given type and registeres it with the container.
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
            LOG.debug("createInstance({}) {}", cls.getSimpleName(), ctor);
            c = (ctor.getAnnotation(Inject.class)!=null) ? ctor : c;
        } // for
        c = (c==null) ? cls.getConstructor() : c;
        // Don't record constructor dependencies - they MUST be already fulfilled
        Object[] parameters = getParameters(null, beanName, c.getParameterTypes(), c.getGenericParameterTypes(), c.getParameterAnnotations());
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
     * creates an instance of the given type and registeres it with the container.
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
     * Create a fresh instance of a given class and inject all needed dependencies from the dinistiq scope.
     *
     * @param <T>
     * @param cls
     * @param name an optional name of the beans used for injection discovery - may be null
     * @return fresh instance with dependencies filled in and post contruct method called if available
     */
    public <T extends Object> T createBean(Class<T> cls, String name) {
        try {
            String beanName = getBeanName(cls, name);
            Map<String, Set<Object>> dependencies = new HashMap<>();
            T bean = createInstance(dependencies, cls, beanName);
            injectDependencies(dependencies, beanName, bean);
            callPostConstruct(bean);
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
        try {
            String beanName = getBeanName(bean.getClass(), name);
            Map<String, Set<Object>> dependencies = new HashMap<>();
            dependencies.put(beanName, new HashSet<>());
            injectDependencies(dependencies, beanName, bean);
            callPostConstruct(bean);
        } catch (Exception e) {
            LOG.error("initBean() "+bean.getClass(), e);
        } // try/catch
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
        final String defaultsName = PRODUCT_BASE_PATH+"/defaults/"+key+".properties";
        final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(defaultsName);
        LOG.debug("getProperties({}) searching defaults {} {}", key, defaultsName, resources);
        while (resources.hasMoreElements()) {
            final URL resource = resources.nextElement();
            LOG.debug("getProperties({}) loading defaults from {}", key, defaultsName);
            beanProperties.load(resource.openStream());
        } // while
        final String beanValuesName = PRODUCT_BASE_PATH+"/beans/"+key+".properties";
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
                LOG.debug("getReferenceValue({}) replacingn {}", propertyValue, name);
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
                LOG.info("storeUrlParts() splitting {} ({})", value, name);
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
     * Fill bean as a map.
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
        LOG.debug("({}) bean properties {}", key, beanProperties.keySet());

        // fill injected fields
        Class<? extends Object> beanClass = bean.getClass();
        String beanClassName = beanClass.getName();
        while (beanClass!=Object.class) {
            if (bean instanceof Map) {
                fillMap(bean, getProperties(key));
                LOG.info("() filled map '{}' {}", key, bean);
            } // if
            for (Field field : beanClass.getDeclaredFields()) {
                LOG.debug("({}) field {}", key, field.getName());
                if (field.getAnnotation(Inject.class)!=null) {
                    Named named = field.getAnnotation(Named.class);
                    String name = (named==null) ? null : (StringUtils.isBlank(named.value()) ? field.getName() : named.value());
                    LOG.info("({} :{}) needs injection with name {}", field.getName(), field.getGenericType(), name);
                    Object b = getValue(dependencies, key, field.getType(), field.getGenericType(), name);
                    final boolean accessible = field.isAccessible();
                    try {
                        field.setAccessible(true);
                        field.set(bean, b);
                        dependencies.get(key).add(b);
                    } catch (SecurityException|IllegalArgumentException|IllegalAccessException e) {
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
                LOG.debug("({}) inject parameters on method {}", key, m.getName());
                Class<? extends Object>[] parameterTypes = m.getParameterTypes();
                Type[] genericParameterTypes = m.getGenericParameterTypes();
                Annotation[][] parameterAnnotations = m.getParameterAnnotations();
                Object[] parameters = getParameters(dependencies, key, parameterTypes, genericParameterTypes, parameterAnnotations);
                try {
                    m.invoke(bean, parameters);
                } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException ex) {
                    LOG.error("() error injecting for method "+m.getName()+" at '"+key+"' :"+beanClassName, ex);
                } // try/catch
            } // if
            if (m.getName().startsWith("set")&&(m.getParameterTypes().length>0)) {
                String propertyName = Introspector.decapitalize(m.getName().substring(3));
                Class<?> parameterType = m.getParameterTypes()[0];
                Type genericType = m.getGenericParameterTypes()[0];
                LOG.debug("({}) writable property found {} :{} {}", key, propertyName, parameterType, genericType);
                if (beanProperties.stringPropertyNames().contains(propertyName)) {
                    String propertyValue = beanProperties.getProperty(propertyName);
                    boolean isBoolean = (parameterType==Boolean.class)||(m.getParameterTypes()[0]==Boolean.TYPE);
                    boolean isCollection = Collection.class.isAssignableFrom(parameterType);
                    Object[] parameters = new Object[1];
                    LOG.debug("({}) trying to set value {} {}:{} {}", key, propertyName, isBoolean, isCollection, propertyValue);
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
                            Set<Object> valueSet = new HashSet<>();
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
    }  // injectDependencies()


    /**
     * Create a dinistiq context from the given class resolver and optional external beans.
     * Add all the external named beans from thei given map for later lookup to the context as well
     * and be sure that your class resolver takes the resources in the dinistiq/ path of your
     * class path into cosideration.
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
                    if (className.startsWith(LIST_TYPE)&&(idx>0)) {
                        String values[] = getReferenceValue(className.substring(idx+1, className.length()-1)).toString().split(",");
                        List<String> instance = Arrays.asList(values);
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
        final Set<Class<Object>> classes = classResolver.getAnnotated(Singleton.class);
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
                    LOG.warn("() will retry {} later: {} - {}", classList.get(i), e.getClass().getName(), e.getMessage());
                    restClassList.add(classList.get(i));
                    restNameList.add(nameList.get(i));
                } // try/catch
            } // for
            classList = restClassList;
            nameList = restNameList;
        } // while

        // Fill in injections and note needed dependencies
        for (String key : new HashSet<>(beans.keySet())) {
            injectDependencies(dependencies, key, beans.get(key));
        } // for

        // sort beans according to dependencies
        LOG.info("() sorting beans according to dependencies");
        ripCord = 10;
        while ((ripCord>0)&&(!dependencies.isEmpty())) {
            ripCord--;
            LOG.info("() {} beans left", dependencies.size());
            Set<String> deletions = new HashSet<>();
            for (String key : dependencies.keySet()) {
                LOG.debug("() checking if {} can be safely put into the ordered list", key);
                boolean dependenciesMet = true;
                for (Object dep : dependencies.get(key)) {
                    boolean isMet = orderedBeans.contains(dep);
                    if ((!isMet)&&(dep instanceof Collection)) {
                        isMet = true;
                        @SuppressWarnings("unchecked")
                        Collection<Object> depCollection = (Collection<Object>) dep;
                        for (Object d : depCollection) {
                            isMet = isMet&&orderedBeans.contains(d);
                        } // for
                    } // if
                    LOG.debug("() {} is missing {} :{}", key, dep, dep.getClass().getName());
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
