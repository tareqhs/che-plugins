/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.jdt.internal.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.util.HashtableOfArrayToObject;
import org.eclipse.jdt.internal.core.util.Util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * A package fragment root that corresponds to a .jar or .zip.
 *
 * <p>NOTE: The only visible entries from a .jar or .zip package fragment root
 * are .class files.
 * <p>NOTE: A jar package fragment root may or may not have an associated resource.
 * @author Evgen Vidolob
 */
public class JarPackageFragmentRoot extends PackageFragmentRoot {
    private final static ArrayList EMPTY_LIST = new ArrayList();
    /**
     * The path to the jar file
     * (a workspace relative path if the jar is internal,
     * or an OS path if the jar is external)
     */
    protected final IPath jarPath;

    protected JarPackageFragmentRoot(File file, JavaProject project, JavaModelManager manager) {
        super(file, project, manager);
        jarPath = new Path(file.getPath());
    }


    @Override
    public IPath getPath() {
        return jarPath;
    }

    /**
     * Compute the package fragment children of this package fragment root.
     * These are all of the directory zip entries, and any directories implied
     * by the path of class files contained in the jar of this package fragment root.
     */
    protected boolean computeChildren(OpenableElementInfo info, File underlyingResource) throws
                                                                                                                            JavaModelException {
        HashtableOfArrayToObject rawPackageInfo = new HashtableOfArrayToObject();
        IJavaElement[] children;
        ZipFile jar = null;
        try {
//            Object file = JavaModel.getTarget(getPath(), true);
//            long level = Util.getJdkLevel(file);
            String compliance = CompilerOptions.VERSION_1_8;
            jar = getJar();

            // always create the default package
            rawPackageInfo.put(CharOperation.NO_STRINGS, new ArrayList[] { EMPTY_LIST, EMPTY_LIST });

            for (Enumeration e= jar.entries(); e.hasMoreElements();) {
                ZipEntry member= (ZipEntry) e.nextElement();
                initRawPackageInfo(rawPackageInfo, member.getName(), member.isDirectory(), compliance);
            }

            // loop through all of referenced packages, creating package fragments if necessary
            // and cache the entry names in the rawPackageInfo table
            children = new IJavaElement[rawPackageInfo.size()];
            int index = 0;
            for (int i = 0, length = rawPackageInfo.keyTable.length; i < length; i++) {
                String[] pkgName = (String[]) rawPackageInfo.keyTable[i];
                if (pkgName == null) continue;
                children[index++] = getPackageFragment(pkgName);
            }
        } catch (CoreException e) {
            if (e.getCause() instanceof ZipException) {
                // not a ZIP archive, leave the children empty
                Util.log(IStatus.ERROR, "Invalid ZIP archive: " + toStringWithAncestors()); //$NON-NLS-1$
                children = NO_ELEMENTS;
            } else if (e instanceof JavaModelException) {
                throw (JavaModelException)e;
            } else {
                throw new JavaModelException(e);
            }
        } finally {
           manager.closeZipFile(jar);
        }

        info.setChildren(children);
        ((JarPackageFragmentRootInfo) info).rawPackageInfo = rawPackageInfo;
        return true;
    }
    /**
     * Returns a new element info for this element.
     */
    protected Object createElementInfo() {
        return new JarPackageFragmentRootInfo();
    }
    /**
     * A Jar is always K_BINARY.
     */
    protected int determineKind(File underlyingResource) {
        return IPackageFragmentRoot.K_BINARY;
    }
    /**
     * Returns true if this handle represents the same jar
     * as the given handle. Two jars are equal if they share
     * the same zip file.
     *
     * @see Object#equals
     */
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof JarPackageFragmentRoot) {
            JarPackageFragmentRoot other= (JarPackageFragmentRoot) o;
            return this.jarPath.equals(other.jarPath);
        }
        return false;
    }
    public String getElementName() {
        return this.jarPath.lastSegment();
    }
    /**
     * Returns the underlying ZipFile for this Jar package fragment root.
     *
     * @exception CoreException if an error occurs accessing the jar
     */
    public ZipFile getJar() throws CoreException {
        return manager.getZipFile(getPath());
    }

    public void closeJar(ZipFile jar){
        manager.closeZipFile(jar);
    }
    /**
     * @see IPackageFragmentRoot
     */
    public int getKind() {
        return IPackageFragmentRoot.K_BINARY;
    }
    int internalKind() throws JavaModelException {
        return IPackageFragmentRoot.K_BINARY;
    }
    /**
     * Returns an array of non-java resources contained in the receiver.
     */
    public Object[] getNonJavaResources() throws JavaModelException {
        // We want to show non java resources of the default package at the root (see PR #1G58NB8)
        Object[] defaultPkgResources =  ((JarPackageFragment) getPackageFragment(CharOperation.NO_STRINGS)).storedNonJavaResources();
        int length = defaultPkgResources.length;
        if (length == 0)
            return defaultPkgResources;
        Object[] nonJavaResources = new Object[length];
        for (int i = 0; i < length; i++) {
            JarEntryResource nonJavaResource = (JarEntryResource) defaultPkgResources[i];
            nonJavaResources[i] = nonJavaResource.clone(this);
        }
        return nonJavaResources;
    }
    public PackageFragment getPackageFragment(String[] pkgName) {
        return new JarPackageFragment(this,manager, pkgName);
    }
//    public IPath internalPath() {
//        if (isExternal()) {
//            return this.jarPath;
//        } else {
//            return super.internalPath();
//        }
//    }
    public File resource(PackageFragmentRoot root) {
//        if (this.resource == null) {
//            // external jar
//            return null;
//        }
        return super.resource(root);
    }


    /**
     * @see IJavaElement
     */
    public IResource getUnderlyingResource() throws JavaModelException {
        if (isExternal()) {
            if (!exists()) throw newNotPresentException();
            return null;
        } else {
            return super.getUnderlyingResource();
        }
    }
    public int hashCode() {
        return this.jarPath.hashCode();
    }
    private void initRawPackageInfo(HashtableOfArrayToObject rawPackageInfo, String entryName, boolean isDirectory, String compliance) {
        int lastSeparator = isDirectory ? entryName.length()-1 : entryName.lastIndexOf('/');
        String[] pkgName = Util.splitOn('/', entryName, 0, lastSeparator);
        String[] existing = null;
        int length = pkgName.length;
        int existingLength = length;
        while (existingLength >= 0) {
            existing = (String[]) rawPackageInfo.getKey(pkgName, existingLength);
            if (existing != null) break;
            existingLength--;
        }
//        JavaModelManager manager = JavaModelManager.getJavaModelManager();
        for (int i = existingLength; i < length; i++) {
            // sourceLevel must be null because we know nothing about it based on a jar file
            if (Util.isValidFolderNameForPackage(pkgName[i], null, compliance)) {
                System.arraycopy(existing, 0, existing = new String[i+1], 0, i);
                existing[i] = manager.intern(pkgName[i]);
                rawPackageInfo.put(existing, new ArrayList[] { EMPTY_LIST, EMPTY_LIST });
            } else {
                // non-Java resource folder
                if (!isDirectory) {
                    ArrayList[] children = (ArrayList[]) rawPackageInfo.get(existing);
                    if (children[1/*NON_JAVA*/] == EMPTY_LIST) children[1/*NON_JAVA*/] = new ArrayList();
                    children[1/*NON_JAVA*/].add(entryName);
                }
                return;
            }
        }
        if (isDirectory)
            return;

        // add classfile info amongst children
        ArrayList[] children = (ArrayList[]) rawPackageInfo.get(pkgName);
        if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(entryName)) {
            if (children[0/*JAVA*/] == EMPTY_LIST) children[0/*JAVA*/] = new ArrayList();
            String nameWithoutExtension = entryName.substring(lastSeparator + 1, entryName.length() - 6);
            children[0/*JAVA*/].add(nameWithoutExtension);
        } else {
            if (children[1/*NON_JAVA*/] == EMPTY_LIST) children[1/*NON_JAVA*/] = new ArrayList();
            children[1/*NON_JAVA*/].add(entryName);
        }

    }
    /**
     * @see IPackageFragmentRoot
     */
    public boolean isArchive() {
        return true;
    }
    /**
     * @see IPackageFragmentRoot
     */
    public boolean isExternal() {
        return resource() == null;
    }
    /**
     * Jars and jar entries are all read only
     */
    public boolean isReadOnly() {
        return true;
    }

//    /**
//     * Returns whether the corresponding resource or associated file exists
//     */
//    protected boolean resourceExists(File underlyingResource) {
//        if (underlyingResource == null) {
//            return
////                    JavaModel.getExternalTarget(
////                            getPath()/*don't make the path relative as this is an external archive*/,
////                            true/*check existence*/) != null;
//        } else {
//            return super.resourceExists(underlyingResource);
//        }
//    }

    protected void toStringAncestors(StringBuffer buffer) {
        if (isExternal())
            // don't show project as it is irrelevant for external jar files.
            // also see https://bugs.eclipse.org/bugs/show_bug.cgi?id=146615
            return;
        super.toStringAncestors(buffer);
    }

    public URL getIndexPath() {
        try {
            IClasspathEntry entry = ((JavaProject) getParent()).getClasspathEntryFor(getPath());
            if (entry != null) return ((ClasspathEntry)entry).getLibraryIndexLocation();
        } catch (JavaModelException e) {
            // ignore exception
        }
        return null;
    }
}
