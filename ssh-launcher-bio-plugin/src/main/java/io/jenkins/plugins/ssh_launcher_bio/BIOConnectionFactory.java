/*
 * The MIT License
 *
 * Copyright 2018 CloudBees, Inc.
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

package io.jenkins.plugins.ssh_launcher_bio;

import hudson.Extension;
import io.jenkins.plugins.ssh_launcher_api.SSHConnection;
import io.jenkins.plugins.ssh_launcher_api.SSHConnectionDetails;
import io.jenkins.plugins.ssh_launcher_api.SSHConnectionFactory;
import io.jenkins.plugins.ssh_launcher_api.SSHConnectionFactoryDescriptor;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;

public class BIOConnectionFactory implements SSHConnectionFactory {

    @DataBoundConstructor
    public BIOConnectionFactory() {}

    @Override
    public SSHConnection connect(SSHConnectionDetails details) throws IOException, InterruptedException {
        return new BIOConnection(details);
    }

    @Extension
    public static class DescriptorImpl extends SSHConnectionFactoryDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.BIOConnectionFactory_displayName();
        }

    }

}
