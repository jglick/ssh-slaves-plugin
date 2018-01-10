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

package io.jenkins.plugins.ssh_launcher_api;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import hudson.model.TaskListener;
import hudson.plugins.sshslaves.verifiers.SshHostKeyVerificationStrategy;
import hudson.slaves.SlaveComputer;
import javax.annotation.Nonnull;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * @see SSHConnectionFactory#connect
 */
public final class SSHConnectionDetails {

    private final @Nonnull String host;
    private final int port;
    private final @Nonnull StandardUsernameCredentials credentials;
    private final @Nonnull SshHostKeyVerificationStrategy hostKeyVerificationStrategy;
    private final @Nonnull SlaveComputer slave;
    private final @Nonnull TaskListener listener;

    @Restricted(NoExternalUse.class)
    public SSHConnectionDetails(@Nonnull String host, int port, @Nonnull StandardUsernameCredentials credentials, @Nonnull SshHostKeyVerificationStrategy hostKeyVerificationStrategy, @Nonnull SlaveComputer slave, @Nonnull TaskListener listener) {
        this.host = host;
        this.port = port;
        this.credentials = credentials;
        this.hostKeyVerificationStrategy = hostKeyVerificationStrategy;
        this.slave = slave;
        this.listener = listener;
    }

    public @Nonnull String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public @Nonnull StandardUsernameCredentials getCredentials() {
        return credentials;
    }

    public @Nonnull SshHostKeyVerificationStrategy getHostKeyVerificationStrategy() {
        return hostKeyVerificationStrategy;
    }

    public @Nonnull SlaveComputer getSlave() {
        return slave;
    }
    
    public @Nonnull TaskListener getListener() {
        return listener;
    }

}
