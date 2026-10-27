package ir.reza.taskmanager.service;

public class LogService {

    private String prefix = "APP";

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void log(String message) {
        System.out.println("[" + prefix + "] " + message);
    }
}