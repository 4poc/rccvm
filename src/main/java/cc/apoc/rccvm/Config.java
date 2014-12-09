package cc.apoc.rccvm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import cc.apoc.rccvm.qemu.VirtualMachine;

import com.google.gson.Gson;

public class Config {
    String web_bind_host;
    int web_bind_port;
    public boolean bypass_task_queue;
    int client_timeout;
    VirtualMachine.Config vm;

    public static Config read(String filename) {
        try {
            Gson gson = new Gson();
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            return gson.fromJson(reader, Config.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
