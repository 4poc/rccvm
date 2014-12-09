package cc.apoc.rccvm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.apoc.rccvm.qemu.VirtualMachine;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;

public class TaskQueue extends Thread {
    private static final Logger logger = LoggerFactory.getLogger("rccvm.core");

    public static class Task {
        @Expose
        UUID id;

        // pass-through request we sent to internal
        String request;
        @Expose
        JsonObject response;

        @Expose
        Date createdAt;
        @Expose
        Date startedAt;
        @Expose
        Date finishedAt;
        @Expose
        boolean terminated;

        public Task(String request) {
            this.request = request;
            id = UUID.randomUUID();
            createdAt = new Date();
        }

        public String toString() {
            return String.format("Task[%s]", id.toString());
        }

        public String toJson() {
            return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
        }

        public void setResponseText(String data) {
            response = new JsonParser().parse(data).getAsJsonObject();
        }
    }

    BlockingQueue<Task> queue;
    Map<UUID, Task> tasks;

    Config config;
    VirtualMachine vm;

    InternalClient internal;

    public TaskQueue(Config config, VirtualMachine vm) {
        this.config = config;
        this.vm = vm;

        internal = new InternalClient(config);

        queue = new LinkedBlockingQueue<Task>();
        tasks = new HashMap<UUID, Task>();
    }

    public Task createTask(String request) {
        Task task = new Task(request);

        tasks.put(task.id, task);
        queue.add(task);

        return task;
    }

    public Task getTask(String taskId) throws Exception {
        UUID id = UUID.fromString(taskId);
        if (!tasks.containsKey(id))
            throw new Exception("task not found");
        return tasks.get(id);
    }

    public void run() {
        Task task = null;
        boolean snapshotMade = false;
        while (true) {
            // blocks until a task becomes available:
            try {
                task = queue.take();
            } catch (InterruptedException e) {
                logger.info("task queue thread interrupted");
                break;
            }
            if (task != null) {
                if (!snapshotMade) {
                    vm.saveSnapshot("default_");
                    snapshotMade = true;
                } else {
                    vm.loadSnapshot("default_");
                }

                logger.info(task.toString() + " running...");
                task.startedAt = new Date();

                try {
                    task.setResponseText(internal.post("/execute", task.request));
                    logger.info(task.toString() + " completed");
                } catch (Exception e) {
                    logger.info(task.toString() + " interrupted");
                } finally {
                    task.finishedAt = new Date();
                }
            }
        }

    }

    public boolean waitForInternal() {
        logger.info("waiting for internal service to become ready");

        int tries = 20;
        while (tries > 0) {
            if (internal.get("/backends") != null) {
                logger.info(String.format("internal service ready, received response (tries=%d)",
                        tries));
                return true;
            }

            try {
                Thread.sleep(1000);
                tries--;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
        }
        logger.error("internal service not ready!");
        return false;
    }
}
