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

/**
 * @see SSHConnectionFactory#connect
 */
public final class SSHConnectionDetails {

    private final String host;
    private final int port;
    private final StandardUsernameCredentials credentials;
    private final SshHostKeyVerificationStrategy hostKeyVerificationStrategy;
    private final SlaveComputer slave;
    private final TaskListener listener;

    SSHConnectionDetails(String host, int port, StandardUsernameCredentials credentials, SshHostKeyVerificationStrategy hostKeyVerificationStrategy, SlaveComputer slave, TaskListener listener) {
        this.host = host;
        this.port = port;
        this.credentials = credentials;
        this.hostKeyVerificationStrategy = hostKeyVerificationStrategy;
        this.slave = slave;
        this.listener = listener;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public StandardUsernameCredentials getCredentials() {
        return credentials;
    }

    public SshHostKeyVerificationStrategy getHostKeyVerificationStrategy() {
        return hostKeyVerificationStrategy;
    }

    public SlaveComputer getSlave() {
        return slave;
    }
    
    public TaskListener getListener() {
        return listener;
    }

}
