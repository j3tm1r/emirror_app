package it.droidconit.emirror.server_responses;

public class Device {
    private String username;
    private String password;

    public Device() {
    }

    public Device(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}