/*
 * The MIT License
 *
 * Copyright (c) 2004-, all the contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.ssh_launcher_api;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.slaves.SlaveComputer;
import hudson.model.TaskListener;
import hudson.util.VersionNumber;

import java.util.List;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Guess where Java is.
 */
@Restricted(NoExternalUse.class) // https://github.com/jenkinsci/ssh-slaves-plugin/commit/a77153107b98f9e8c34ee814f97dec1e24d0f1e2 introduced; https://jenkins.io/doc/developer/extensions/ssh-slaves/#javaprovider never implemented elsewhere
public abstract class JavaProvider implements ExtensionPoint {
    
    private static final VersionNumber JAVA_LEVEL_7 = new VersionNumber("7");
    private static final VersionNumber JAVA_LEVEL_8 = new VersionNumber("8");
    private static final VersionNumber JAVA_8_MINIMAL_SINCE = new VersionNumber("2.54");
    
    /**
     * Returns the list of possible places where java executable might exist.
     *
     * @return
     *      Can be empty but never null. Absolute path to the possible locations of Java.
     */
    public abstract List<String> getJavas(SlaveComputer computer, TaskListener listener);

    /**
     * All regsitered instances.
     */
    public static ExtensionList<JavaProvider> all() {
        return ExtensionList.lookup(JavaProvider.class);
    }

    /**
     * Gets minimal required Java version.
     * 
     * @return Minimal Java version required on the master and agent side.
     *         It will be {@link #JAVA_LEVEL_7} if the core version cannot be determined due to whatever reason.
     * 
     */
    @Nonnull
    public static VersionNumber getMinJavaLevel() {
        // TODO: Use reflection to utilize new core API once JENKINS-45842 is integrated
        // TODO: Get rid of it once Jenkins core requirement is bumped
        /*try {
            Method method = Jenkins.class.getMethod("getJavaMinLevel");
            Object res = method.invoke(null);
            if (res instanceof VersionNumber) {
                return (VersionNumber) res;
            }
        } catch(SecurityException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            // Fallback to the default behavior, not supported yet
        }*/
        
        // Now use the known map of the previous updates
        final VersionNumber version = Jenkins.getVersion();
        if (version == null) {
            // Version cannot be determined, assume it's an old one to retain compatibility.
            return JAVA_LEVEL_7;
        }
        
        return version.isOlderThan(JAVA_8_MINIMAL_SINCE) ? JAVA_LEVEL_7 : JAVA_LEVEL_8;
    }
}
