package cc.apoc.rccvm;

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.apoc.rccvm.qemu.VirtualMachine;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger("rccvm.core");

    Config config;
    Server server;
    TaskQueue queue;
    VirtualMachine vm;

    public void startup() throws Exception {
        logger.info("startup");

        config = Config.read("config.json");

        logger.info("start vm");
        vm = new VirtualMachine(config.vm);
        if (!vm.start()) {
            logger.error("unable to start vm!");
            return;
        }
        queue = new TaskQueue(config, vm);
        queue.start();
        if (!queue.waitForInternal()) {
            vm.stop();
            return;
        }

        logger.info("start web server");

        // stupid java bloat
        server = new Server(new InetSocketAddress(config.web_bind_host, config.web_bind_port));
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        ServletHolder holder = new ServletHolder();
        holder.setServlet(new Servlet(config, queue));
        handler.addServletWithMapping(holder, "/");
        server.start();
        server.join();
    }

    public void shutdown() {
        logger.info("shutdown");
        vm.stop();
    }

    public static void main(String[] args) throws Exception {
        final Main instance = new Main();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                instance.shutdown();
            }
        });

        instance.startup();
    }
}
