package cc.apoc.rccvm.qemu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.apoc.rccvm.utils.Utils;

/**
 * A helper class that builds the command line to start qemu.
 * 
 * @author apoc
 */
public class QemuCommandBuilder {
    private static final Logger logger = LoggerFactory.getLogger("rccvm.qemu");

    private boolean valid;
    private String error;
    private List<String> args;

    /**
     * Automatically adds a -net user,<userNetworkStack.join(',')> option to the
     * cmd.
     */
    private List<String> userNetworkStack;

    /**
     * If set adds a restrict=on to the user network stack, disallowing any
     * network connections except specified forwardings.
     */
    private boolean restrictNetwork;

    private String workingDirectory;

    public QemuCommandBuilder(String workingDirectory, String cmd) {
        this.workingDirectory = workingDirectory;
        args = new ArrayList<String>();
        args.add(cmd);
        valid = true;

        userNetworkStack = new ArrayList<String>();

        File executable = new File(cmd);
        if (!executable.exists() || !executable.canExecute()) {
            valid = false;
            error = "qemu binary not found or not executable!";
        }
    }

    public QemuCommandBuilder addArgument(String name, String value) {
        args.add(name);
        args.add(value);
        logger.trace("add qemu argument " + name + " " + value);
        return this;
    }

    public QemuCommandBuilder addArgument(String name) {
        args.add(name);
        logger.trace("add qemu argument " + name);
        return this;
    }

    /**
     * Add virtual RAM size to megs megabytes.
     * 
     * @param memory
     * @return
     */
    public QemuCommandBuilder addMemory(int memory) {
        if (memory < 64 || memory > 1024) {
            error = "invalid memory size, kernel needs atleast 64MB";
            valid = false;
        }
        addArgument("-m", String.valueOf(memory));
        return this;
    }

    public QemuCommandBuilder addKernel(String filename) {
        if (!(new File(filename).exists())) {
            error = "invalid kernel";
            valid = false;
        }
        addArgument("-kernel", filename);
        return this;
    }

    public QemuCommandBuilder addInitrd(String filename) {
        if (!(new File(filename).exists())) {
            error = "invalid initramdisk";
            valid = false;
        }
        addArgument("-initrd", filename);
        return this;
    }

    public QemuCommandBuilder addDiskImage(String filename) {
        /*
         * if (!(new File(filename).exists())) { error = "invalid disk image";
         * valid = false; }
         */
        addArgument("-hda", filename);
        return this;
    }

    public QemuCommandBuilder addKernelCmdline(String cmdline) {
        addArgument("-append", cmdline);
        return this;
    }

    public QemuCommandBuilder addNoGraphic() {
        addArgument("-nographic");
        return this;
    }

    public QemuCommandBuilder addDisplayNone() {
        addArgument("-display", "none");
        return this;
    }

    public QemuCommandBuilder addMonitorStdio() {
        addArgument("-monitor", "stdio");
        return this;
    }

    public QemuCommandBuilder addSnapshot() {
        addArgument("-snapshot");
        return this;
    }

    /**
     * Redirect the serial interface /dev/ttyS0 to standard I/O (VERY useful for
     * debugging etc.)
     * 
     * We might also show this in-game on a display.
     * 
     * @return
     */
    public QemuCommandBuilder addSerialStdio() {
        addArgument("-serial", "stdio");
        return this;
    }

    public QemuCommandBuilder addVNC(String host, int display) {
        addArgument("-vnc", String.format("%s:%d", host, display));
        return this;
    }

    public QemuCommandBuilder addLoadVM(String tag) {
        addArgument("-loadvm", tag);
        return this;
    }

    public QemuCommandBuilder addQmpPort(int port) {
        addArgument("-qmp", String.format("tcp:127.0.0.1:%d,server", port));
        return this;
    }

    public QemuCommandBuilder addMonitorPort(int port) {
        addMonitorPort(port, false);
        return this;
    }

    public QemuCommandBuilder addMonitorPort(int port, boolean nowait) {
        addArgument("-monitor",
                String.format("tcp:127.0.0.1:%d,server" + (nowait ? ",nowait" : ""), port));
        return this;
    }

    /**
     * Adds a TCP host forwarding to the user network stack.
     * 
     * This opens a port on the host that is forwarded to the specified port on
     * the guest.
     * 
     * @return
     */
    public QemuCommandBuilder addHostFwd(int hostport, int guestport) {
        if (hostport < 1024) {
            error = "ports < 1024 require root privileges to run!";
            valid = false;
        }
        userNetworkStack.add(String.format("hostfwd=tcp:127.0.0.1:%d-10.0.2.15:%d", hostport,
                guestport));
        return this;
    }

    public QemuCommandBuilder addTelnetFwd(int hostport) {
        return addHostFwd(hostport, 23);
    }

    public QemuCommandBuilder setRestrict(boolean restrict) {
        restrictNetwork = restrict;
        return this;
    }

    public QemuCommandBuilder addPidFile(String filename) {
        if ((new File(filename).exists())) {
            error = "invalid pidfile already exists?!";
            valid = false;
        }
        addArgument("-pidfile", filename);
        return this;
    }

    public List<String> getCommandLine() {
        List<String> cmdline = new ArrayList<String>(args);

        StringBuilder sb = new StringBuilder();
        sb.append("user,");
        if (restrictNetwork)
            sb.append("restrict=on,");
        sb.append(Utils.join(userNetworkStack, ","));

        cmdline.add("-net");
        cmdline.add("nic");
        cmdline.add("-net");
        cmdline.add(sb.toString());
        // cmdline.add("-display curses");

        return cmdline;
    }

    public ProcessBuilder createProcessBuilder() {
        if (!valid) {
            logger.error("invalid cmd: " + error);
            return null;
        }
        logger.info("qemu command: " + Utils.join(getCommandLine(), " "));
        ProcessBuilder pb = new ProcessBuilder(getCommandLine());
        pb.directory(new File(workingDirectory));
        return pb;
    }

    public void addEnableKVM() {
        addArgument("-enable-kvm");
    }

    public String getWorkingDir() {
        return workingDirectory;
    }

}
