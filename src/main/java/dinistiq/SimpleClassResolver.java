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

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * Resolve classes or classnames from a given set of packages.
 *
 * One design goal in finding the classes was to hit the JAR files just once but apart from that this is a
 * far too simple implementation.
 *
 */
public class SimpleClassResolver implements ClassResolver {

    private static final Log LOG = LogFactory.getLog(SimpleClassResolver.class);

    private final Set<String> packageNames;

    private final Set<String> properties = new HashSet<String>();

    private Set<String> classNames = null;


    public SimpleClassResolver(Set<String> packageNames) {
        this.packageNames = packageNames;
        // to have the properties files in the path which we intend to use for configuration
        this.packageNames.add(this.getClass().getPackage().getName());
    } // SimpleClassResolver()


    public SimpleClassResolver() {
        this(new HashSet<String>());
    } // SimpleClassResolver()


    /**
     * Adds all relevant JAR/.class URLs for a given package to an already present set of URLs.
     *
     * @param urls set of URLs to add the newly resolved ones to.
     * @param packageName name of the package
     */
    private void addUrlsForPackage(Set<URL> urls, String packageName) {
        String packagePath = packageName.replace('.', '/');
        try {
            Enumeration<URL> urlEnumeration = this.getClass().getClassLoader().getResources(packagePath);
            while (urlEnumeration.hasMoreElements()) {
                URL u = urlEnumeration.nextElement();
                String url = u.toString();
                int idx = url.indexOf('!');
                if (idx>0) {
                    url = url.substring(0, idx);
                } // if
                if (url.startsWith("jar:")) {
                    url = url.substring(4);
                } // if
                if (!url.endsWith(".jar")) {
                    url = url.substring(0, url.length()-packagePath.length());
                } //
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
     * Checks a set of class or properties names and adds them to the classNames collection.
     * Check is performed against package name and file extension.
     *
     * @param classNames
     * @param name
     */
    protected final void checkClassAndAdd(Set<String> classNames, String name) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkClassAndAdd() name="+name);
        } // if
        if (name.endsWith(".class")&&(name.indexOf('$')<0)) {
            name = name.replace(File.separatorChar, '/');
            name = name.replace('/', '.');
            String className = name.substring(0, name.length()-6);
            if (LOG.isDebugEnabled()) {
                LOG.debug("checkClassAndAdd() class name "+className);
            } // if
            boolean add = false;
            for (String packageName : packageNames) {
                add = add||(className.startsWith(packageName));
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


    protected final void recurseSubDir(Set<String> classNames, File dir, int basePathLength) {
        for (File f : dir.listFiles()) {
            String fileName = f.getAbsolutePath().substring(basePathLength);
            if (LOG.isDebugEnabled()) {
                LOG.debug("recurseSubDir() fileName="+fileName);
            } // if
            if ((fileName.endsWith(".class"))||(fileName.endsWith(".properties"))) {
                checkClassAndAdd(classNames, fileName);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("recurseSubDir() drilling down "+f.getAbsolutePath());
                } // if
                recurseSubDir(classNames, f, basePathLength);
            } // if
        } // for
    } // recurseSubDir()


    /**
     * Get all names of classes from the underlying packages.
     */
    protected Set<String> getClassNames() {
        if (classNames==null) {
            classNames = new HashSet<String>();
            Set<URL> urls = new HashSet<URL>();
            for (String packageName : packageNames) {
                addUrlsForPackage(urls, packageName);
            } // if
            if (LOG.isDebugEnabled()) {
                LOG.debug("getClassNames() url # "+urls.size());
            } // if
            for (URL u : urls) {
                try {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("getClassNames(): path "+u.getPath());
                    } // if
                    if (u.getPath().endsWith(".jar")) {
                        JarInputStream is = new JarInputStream(u.openStream());
                        JarEntry entry;
                        while ((entry = is.getNextJarEntry())!=null) {
                            checkClassAndAdd(classNames, entry.getName());
                        } // while
                    } else {
                        File dir = new File(u.getPath());
                        int basePathLength = dir.getAbsolutePath().length()+1;
                        recurseSubDir(classNames, dir, basePathLength);
                    } // if
                } catch (IOException e) {
                    LOG.error("getClassNames()", e);
                } // try/catch
            } // for
        } // if
        return classNames;
    } // getClassNames()


    /**
     * Get classes from underlying packages satisfying the given annotation and superclass which are no interfaces.
     *
     * @see ClassResolver#getSubclasses(java.lang.Class)
     */
    public <T extends Object> Set<Class<T>> getSubclasses(Class<T> c) {
        Set<Class<T>> result = new HashSet<Class<T>>();
        Set<String> classNames = getClassNames();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSubclasses() checking "+classNames.size()+" classes");
        } // if
        for (String className : classNames) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getSubclasses() className="+className);
                } // if
                @SuppressWarnings("unchecked")
                Class<T> cls = (Class<T>) Class.forName(className);
                if ((!cls.isInterface())&&c.isAssignableFrom(cls)&&((c.getModifiers()&Modifier.ABSTRACT)==0)) {
                    result.add(cls);
                } // if
            } catch (Throwable e) {
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
        Set<Class<T>> result = new HashSet<Class<T>>();
        Set<String> classNames = getClassNames();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAnnotated() checking "+classNames.size()+" classes");
        } // if
        for (String className : classNames) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getAnnotated() className="+className);
                } // if
                @SuppressWarnings("unchecked")
                Class<T> cls = (Class<T>) Class.forName(className);
                if ((!cls.isInterface())&&(cls.getAnnotation(annotation)!=null)&&((cls.getModifiers()&Modifier.ABSTRACT)==0)) {
                    result.add(cls);
                } // if
            } catch (Throwable e) {
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
        Set<Class<T>> result = new HashSet<Class<T>>();
        Set<String> classNames = getClassNames();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAnnotatedSubclasses() checking "+classNames.size()+" classes");
        } // if
        for (String className : classNames) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getAnnotatedSubclasses() className="+className);
                } // if
                @SuppressWarnings("unchecked")
                Class<T> cls = (Class<T>) Class.forName(className);
                if ((cls.getAnnotation(annotation)!=null)&&c.isAssignableFrom(cls)&&(!cls.isInterface())) {
                    result.add(cls);
                } // if
            } catch (Throwable e) {
                LOG.error("getAnnotatedSubclasses()", e);
            } // try/catch
        } // if
        return result;
    } // getAnnotatedSubclasses()


    /**
     * @see ClassResolver#getProperties(java.lang.String)
     */
    @Override
    public Set<String> getProperties(String path) {
        Set<String> result = new HashSet<String>();
        for (String property : properties) {
            if (LOG.isInfoEnabled()) {
                LOG.info("("+path+") checking "+property);
            } // if
            if (property.startsWith(path)) {
                result.add(property);
            } // if
        } // for
        return result;
    } // getProperties()

} // SimpleClassResolver
