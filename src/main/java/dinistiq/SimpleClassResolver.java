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

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * Resolve classes or classnames from a given set of packages.
 *
 * One design goal in finding the classes was to hit the JAR files just once but apart from that this is a
 * far too simple implementation.
 *
 */
public class SimpleClassResolver implements ClassResolver {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleClassResolver.class);

    private final Set<String> packageNames;

    private final Set<String> properties;

    private final Set<String> classNames;


    /**
     * Adds all relevant JAR/.class URLs for a given package to an already present set of URLs.
     *
     * @param urls set of URLs to add the newly resolved ones to.
     * @param packageName name of the package
     */
    private void addUrlsForPackage(Set<URL> urls, String packageName) {
        String packagePath = packageName.replace('.', '/');
        try {
            Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while (urlEnumeration.hasMoreElements()) {
                URL u = urlEnumeration.nextElement();
                String url = u.toString();
                int idx = url.indexOf('!');
                url = idx>0 ? url.substring(0, idx) : url;
                url = url.startsWith("jar:") ? url.substring(4) : url;
                url = url.startsWith("vfs:/") ? "file"+url.substring(3, url.length()-packagePath.length()-2) : url;
                url = !url.endsWith(".jar") ? url.substring(0, url.length()-packagePath.length()) : url;
                if (LOG.isInfoEnabled()) {
                    LOG.info("addUrlsForPackage() resulting URL "+url);
                } // if
                urls.add(new URL(url));
            } // while
        } catch (IOException e) {
            LOG.error("addUrlsForPackage()", e);
        } // try/catch
    } // addUrlsForPackage()


    /**
     * Checks a file name if it needs to be considered for properties files or derive a class name from.
     *
     * @param name name of a file to be scanned
     */
    protected final void checkClassAndAdd(String name) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkClassAndAdd() name="+name);
        } // if
        if (name.endsWith(".class")&&(name.indexOf('$')<0)) {
            name = name.replace(File.separatorChar, '/').replace('/', '.');
            String className = name.substring(0, name.length()-6);
            if (LOG.isDebugEnabled()) {
                LOG.debug("checkClassAndAdd() class name "+className);
            } // if
            boolean add = false;
            for (String packageName : packageNames) {
                add = add||className.startsWith(packageName);
            } // if
            if (add) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("checkClassAndAdd(): "+className);
                } // if
                classNames.add(className);
            } // if
        } // if
        if (name.endsWith(".properties")) {
            if (LOG.isInfoEnabled()) {
                LOG.info("checkClassAndAdd() properties "+name);
            } // if
            properties.add(name.replace(File.separatorChar, '/'));
        } // if
    } // checkClassAndAdd()


    protected final void recurseSubDir(File dir, int basePathLength) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("recurseSubDir() scanning "+dir.getAbsolutePath());
        } // if
        for (File f : (dir.isDirectory() ? dir.listFiles() : new File[0])) {
            String fileName = f.getAbsolutePath().substring(basePathLength);
            if (LOG.isDebugEnabled()) {
                LOG.debug("recurseSubDir() fileName="+fileName);
            } // if
            if (fileName.endsWith(".class")||fileName.endsWith(".properties")) {
                checkClassAndAdd(fileName);
            } else {
                recurseSubDir(f, basePathLength);
            } // if
        } // for
    } // recurseSubDir()


    public SimpleClassResolver(Set<String> packageNames) {
        this.packageNames = packageNames;
        // to have the properties files in the path which we intend to use for configuration
        this.packageNames.add(this.getClass().getPackage().getName());

        properties = new HashSet<>();
        classNames = new HashSet<>();
        Set<URL> urls = new HashSet<>();
        for (String packageName : packageNames) {
            addUrlsForPackage(urls, packageName);
        } // if
        if (LOG.isDebugEnabled()) {
            LOG.debug("() url # "+urls.size());
        } // if
        for (URL u : urls) {
            try {
                String path = URLDecoder.decode(u.getPath(), "UTF-8");
                if (LOG.isInfoEnabled()) {
                    LOG.info("(): path "+path);
                } // if
                if (path.endsWith(".jar")) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("(): scanning jar "+path);
                    } // if
                    JarInputStream is = new JarInputStream(u.openStream());
                    for (JarEntry entry = is.getNextJarEntry(); entry!=null; entry = is.getNextJarEntry()) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("(): entry "+entry.getName());
                        } // if
                        checkClassAndAdd(entry.getName());
                    } // for
                    is.close();
                } else {
                    File dir = new File(path);
                    int basePathLength = dir.getAbsolutePath().length()+1;
                    recurseSubDir(dir, basePathLength);
                } // if
            } catch (IOException e) {
                LOG.error("()", e);
            } // try/catch
        } // for
    } // SimpleClassResolver()


    /**
     * Helper method to keep areas with suppressed warnings small.
     *
     * @param <T>
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private <T extends Object> Class<T> loadClass(String className) throws ClassNotFoundException {
        return (Class<T>) Class.forName(className);
    } // loadClass()


    /**
     * Get classes from underlying packages satisfying the given annotation and superclass which are no interfaces.
     *
     * @see ClassResolver#getSubclasses(java.lang.Class)
     */
    @Override
    public <T extends Object> Set<Class<T>> getSubclasses(Class<T> c) {
        Set<Class<T>> result = new HashSet<>();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSubclasses() checking "+classNames.size()+" classes");
        } // if
        for (String className : classNames) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getSubclasses() className="+className);
                } // if
                Class<T> cls = loadClass(className);
                if ((!cls.isInterface())&&c.isAssignableFrom(cls)&&((c.getModifiers()&Modifier.ABSTRACT)==0)) {
                    result.add(cls);
                } // if
            } catch (ClassNotFoundException e) {
                LOG.error("getSubclasses()", e);
            } // try/catch
        } // if
        return result;
    } // getSubclasses()


    /**
     * Get classes from underlying packages satisfying the given annotation and superclass which are no interfaces.
     *
     * @see ClassResolver#getAnnotated(java.lang.Class)
     */
    @Override
    public <T extends Object> Set<Class<T>> getAnnotated(Class<? extends Annotation> annotation) {
        Set<Class<T>> result = new HashSet<>();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAnnotated() checking "+classNames.size()+" classes");
        } // if
        for (String className : classNames) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getAnnotated() className="+className);
                } // if
                Class<T> cls = loadClass(className);
                if ((!cls.isInterface())&&(cls.getAnnotation(annotation)!=null)&&((cls.getModifiers()&Modifier.ABSTRACT)==0)) {
                    result.add(cls);
                } // if
            } catch (ClassNotFoundException e) {
                LOG.error("getAnnotated()", e);
            } // try/catch
        } // if
        return result;
    } // getAnnotated()


    /**
     * Get classes from underlying packages satisfying the given annotation and superclass.
     * Interfaces are excluded.
     *
     * @see ClassResolver#getAnnotatedSubclasses(java.lang.Class, java.lang.Class)
     */
    @Override
    public <T extends Object> Set<Class<T>> getAnnotatedSubclasses(Class<T> c, Class<? extends Annotation> annotation) {
        Set<Class<T>> result = new HashSet<>();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAnnotatedSubclasses() checking "+classNames.size()+" classes");
        } // if
        for (String className : classNames) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getAnnotatedSubclasses() className="+className);
                } // if
                Class<T> cls = loadClass(className);
                if ((cls.getAnnotation(annotation)!=null)&&c.isAssignableFrom(cls)&&(!cls.isInterface())) {
                    result.add(cls);
                } // if
            } catch (ClassNotFoundException e) {
                LOG.error("getAnnotatedSubclasses()", e);
            } // try/catch
        } // if
        return result;
    } // getAnnotatedSubclasses()


    /**
     * @see ClassResolver#getProperties(java.lang.String)
     */
    @Override
    public SortedSet<String> getProperties(String path) {
        SortedSet<String> result = new TreeSet<>();
        for (String property : properties) {
            if (LOG.isInfoEnabled()) {
                LOG.info("getProperties("+path+") checking "+property);
            } // if
            if (property.startsWith(path)) {
                result.add(property);
            } // if
        } // for
        return result;
    } // getProperties()

} // SimpleClassResolver
