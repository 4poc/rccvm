package cc.apoc.rccvm.qemu;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a virtual machine instance running in qemu.
 * 
 * @author apoc
 */
public class VirtualMachine {
    private static final Logger logger = LoggerFactory.getLogger("rccvm.qemu");

    public static class Config {
        String wd;
        String qemu_command;
        int monitor_port;

        String image;
        int daemon_guest_port;
        public int daemon_host_port;

        int memory;

        boolean enable_kvm;
        boolean enable_vnc;

        String vnc_host;
        int vnc_display;
    }

    QemuCommandBuilder command;

    private Process process;

    private MonitorClient monitor;

    private BufferedReader bufferedInputStream;
    private BufferedReader bufferedErrorStream;

    /**
     * Holds the last messages received over the serial console, those messages
     * are logged by logStreams aswell.
     * 
     * The guest can write arbitary messages on this console. Might be useful in
     * the future.
     */
    private List<String> serialConsoleMessages;

    public VirtualMachine(VirtualMachine.Config config) {
        serialConsoleMessages = new LinkedList<String>();
        command = new QemuCommandBuilder(config.wd, config.qemu_command);

        command.addDiskImage(config.image);

        // forward guest rccvmd port (5000) to the host
        command.addHostFwd(config.daemon_host_port, config.daemon_guest_port);

        // open monitor (with nowait)
        command.addMonitorPort(config.monitor_port, true);

        command.addMemory(config.memory);

        if (config.enable_kvm)
            command.addEnableKVM();
        if (config.enable_vnc)
            command.addVNC(config.vnc_host, config.vnc_display);

        monitor = new MonitorClient(config.monitor_port);

        if (monitor.connect()) {
            monitor.sendPowerdown();
        }
    }

    public boolean start() {
        command.addSerialStdio(); // redirect bootprompt to stdio
        command.setRestrict(true); // restrict networking
        command.addDisplayNone();

        // disk changes are stored in a snapshot:
        command.addSnapshot();

        ProcessBuilder pb = command.createProcessBuilder();

        if (pb == null) {
            logger.error("unable to start vm!");
            return false;
        }

        pb.directory(new File(command.getWorkingDir()));

        try {
            process = pb.start();

            startStreams();

            // blocks until the monitor is available (until it can connect to
            // it):
            if (!waitForMonitor()) {
                stop();
                return false;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            stop();
            return false;
        }

        return true;
    }

    private boolean waitForMonitor() {
        logger.info("waiting for monitor service to become ready");

        int tries = 10;
        while (tries > 0) {
            if (monitor.connect()) {
                logger.info(String.format("connected to monitor (tries=%d)", tries));
                return true;
            }

            try {
                Thread.sleep(100);
                tries--;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
        }
        logger.error("error connecting to monitor");
        return false;
    }

    /**
     * We listen on STDOUT and STDERR for messages from the VM.
     */
    private void startStreams() {
        InputStream inputStream = process.getInputStream();
        bufferedInputStream = new BufferedReader(new InputStreamReader(inputStream));
        InputStream errorStream = process.getErrorStream();
        bufferedErrorStream = new BufferedReader(new InputStreamReader(errorStream));
    }

    public void stop() {
        logStreams();
        logger.info("stopping vm qemu process");
        if (process != null) {
            if (monitor.isConnected()) {
                monitor.sendPowerdown();
                monitor.close();
            }
            try {
                Thread.sleep(250);
                // TODO: this is kinda weird sequence to stop the vm
                logger.info("qemu process exit code was: " + process.exitValue());
            } catch (IllegalThreadStateException e) {
                logger.info("qemu process is still running");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.info("process.destory");
            process.destroy();
        }
        process = null;
    }

    public BufferedReader getBufferedInputStream() {
        return bufferedInputStream;
    }

    public BufferedReader getBufferedErrorStream() {
        return bufferedErrorStream;
    }

    /**
     * Read from stdout and stderr and log each line.
     */
    public void logStreams() {
        logger.debug("error stream:");
        logStream(this.bufferedErrorStream, false);
        logger.debug("input stream:");
        logStream(this.bufferedInputStream, true);
    }

    /**
     * Logs stream messages from the specified buffered reader.
     * 
     * If appendSerial is true, the messages are also appended to the
     * serialConsoleMessages attribute.
     * 
     * @param br
     *            a buffered reader to read from.
     * @param appendSerial
     */
    private void logStream(BufferedReader br, boolean appendSerial) {
        try {
            if (br != null) {
                while (br.ready()) {
                    String line = br.readLine();
                    logger.debug("vm> " + line);

                    if (appendSerial) {
                        serialConsoleMessages.add(line);
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<String> getSerialConsoleMessages() {
        return serialConsoleMessages;
    }

    /**
     * Does a system-reset on the specified machine.
     */
    public void reset() {
        monitor.sendReset();
    }

    /**
     * Does a system-powerdown on the specified machine, this should trigger a
     * proper shutdown.
     * 
     * Does not work currently because for some reason acpid is not working.
     * TODO: its now using monitor not qmp, does it still not work?
     */
    public void powerdown() {
        monitor.sendPowerdown();
    }

    public boolean running() {
        return process != null;
    }

    public void saveSnapshot(String tag) {
        monitor.sendSaveVM(tag);
    }

    public void loadSnapshot(String tag) {
        monitor.sendLoadVM(tag);
    }

    public MonitorClient getMonitorClient() {
        return monitor;
    }
}
