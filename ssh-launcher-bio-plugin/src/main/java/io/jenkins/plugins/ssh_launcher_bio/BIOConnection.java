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

import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.Session;
import hudson.model.Computer;
import hudson.model.Slave;
import hudson.model.TaskListener;
import hudson.plugins.sshslaves.Messages;
import hudson.plugins.sshslaves.PluginImpl;
import io.jenkins.plugins.ssh_launcher_api.SSHConnection;
import io.jenkins.plugins.ssh_launcher_api.SSHConnectionParameters;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class BIOConnection implements SSHConnection {

    private final SSHConnectionParameters params;
    private final Connection connection;
    private Session session;

    BIOConnection(SSHConnectionParameters params) throws IOException, InterruptedException {
        this.params = params;
        connection = new Connection(params.getHost(), params.getPort());
        String[] preferredKeyAlgorithms = params.getHostKeyVerificationStrategy().getPreferredKeyAlgorithms(params.getSlave());
        if (preferredKeyAlgorithms != null && preferredKeyAlgorithms.length > 0) { // JENKINS-44832
            connection.setServerHostKeyAlgorithms(preferredKeyAlgorithms);
        } else {
            params.getListener().getLogger().println("Warning: no key algorithms provided; JENKINS-42959 disabled");
        }
        PluginImpl.register(connection);
        session = connection.openSession();
        expandChannelBufferSize(session, params.getListener());
    }

    @Override
    public int exec(String command, OutputStream output) throws IOException, InterruptedException {
        return connection.exec(command, output);
    }

    @Override
    public void close() throws IOException {
        boolean connectionLost = reportTransportLoss(connection, params.getListener());
        if (session != null) {
            // give the process 3 seconds to write out its dying message before we cut the loss
            // and give up on this process. if the slave process had JVM crash, OOME, or any other
            // critical problem, this will allow us to capture that.
            // exit code is also an useful info to figure out why the process has died.
            try {
                params.getListener().getLogger().println(getSessionOutcomeMessage(session, connectionLost));
                session.getStdout().close();
                session.close();
            } catch (Throwable t) {
                t.printStackTrace(params.getListener().error(Messages.SSHLauncher_ErrorWhileClosingConnection()));
            }
            session = null;
        }

        Slave n = params.getSlave().getNode();
        if (n != null && !connectionLost) {
            final String fileName = n.getRemoteFS().replaceFirst("/+$", "") + "/slave.jar"; // TODO get from SSHConnectionDetails
            Future<?> tidyUp = Computer.threadPoolForRemoting.submit(new Runnable() {
                public void run() {
                    // this would fail if the connection is already lost, so we want to check that.
                    // TODO: Connection class should expose whether it is still connected or not.

                    SFTPv3Client sftpClient = null;
                    try {
                        sftpClient = new SFTPv3Client(connection);
                        sftpClient.rm(fileName);
                    } catch (Exception e) {
                        if (sftpClient == null) {// system without SFTP
                            try {
                                connection.exec("rm " + fileName, params.getListener().getLogger());
                            } catch (Error error) {
                                throw error;
                            } catch (Throwable x) {
                                x.printStackTrace(params.getListener().error(Messages.SSHLauncher_ErrorDeletingFile(getTimestamp())));
                                // We ignore other Exception types
                            }
                        } else {
                            e.printStackTrace(params.getListener().error(Messages.SSHLauncher_ErrorDeletingFile(getTimestamp())));
                        }
                    } finally {
                        if (sftpClient != null) {
                            sftpClient.close();
                        }
                    }
                }
            });
            try {
                // the delete is best effort only and if it takes longer than 60 seconds - or the launch
                // timeout (if specified) - then we should just give up and leave the file there.
                Integer launchTimeoutSeconds = params.getLaunchTimeoutSeconds();
                tidyUp.get(launchTimeoutSeconds == null ? 60 : launchTimeoutSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace(params.getListener().error(Messages.SSHLauncher_ErrorDeletingFile(getTimestamp())));
                // we should either re-apply our interrupt flag or propagate... we don't want to propagate, so...
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                e.printStackTrace(params.getListener().error(Messages.SSHLauncher_ErrorDeletingFile(getTimestamp())));
            } catch (TimeoutException e) {
                e.printStackTrace(params.getListener().error(Messages.SSHLauncher_ErrorDeletingFile(getTimestamp())));
            } finally {
                if (!tidyUp.isDone()) {
                    tidyUp.cancel(true);
                }
            }
        }

        PluginImpl.unregister(connection);
    }

    /**
     * If the SSH connection as a whole is lost, report that information.
     */
    private static boolean reportTransportLoss(Connection c, TaskListener listener) {
        Throwable cause = c.getReasonClosedCause();
        if (cause != null) {
            cause.printStackTrace(listener.error("Socket connection to SSH server was lost"));
        }
        return cause != null;
    }

    private static void expandChannelBufferSize(Session session, TaskListener listener) {
        // see hudson.remoting.Channel.PIPE_WINDOW_SIZE for the discussion of why 1MB is in the right ball park
        // but this particular session is where all the master/slave communication will happen, so
        // it's worth using a bigger buffer to really better utilize bandwidth even when the latency is even larger
        // (and since we are draining this pipe very rapidly, it's unlikely that we'll actually accumulate this much data)
        int sz = 4;
        session.setWindowSize(sz * 1024 * 1024);
        listener.getLogger().println("Expanded the channel window size to " + sz + "MB");
    }

    /**
     * Find the exit code or exit status, which are differentiated in SSH
     * protocol.
     */
    private static String getSessionOutcomeMessage(Session session, boolean isConnectionLost) throws InterruptedException {
        session.waitForCondition(ChannelCondition.EXIT_STATUS | ChannelCondition.EXIT_SIGNAL, 3000);

        Integer exitCode = session.getExitStatus();
        if (exitCode != null) {
            return "Slave JVM has terminated. Exit code=" + exitCode;
        }

        String sig = session.getExitSignal();
        if (sig != null) {
            return "Slave JVM has terminated. Exit signal=" + sig;
        }

        if (isConnectionLost) {
            return "Slave JVM has not reported exit code before the socket was lost";
        }

        return "Slave JVM has not reported exit code. Is it still running?";
    }

    // TODO perhaps share from API
    private static String getTimestamp() {
        return String.format("[%1$tD %1$tT]", new Date());
    }

//    static {
//        com.trilead.ssh2.log.Logger.enabled = true;
//        com.trilead.ssh2.log.Logger.logger = new DebugLogger() {
//            public void log(int level, String className, String message) {
//                System.out.println(className+"\n"+message);
//            }
//        };
//    }
}
