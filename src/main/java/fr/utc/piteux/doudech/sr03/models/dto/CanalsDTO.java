package fr.utc.piteux.doudech.sr03.models.dto;

import java.util.ArrayList;
import java.util.List;

public class CanalsDTO {
    private int canal_id;
    private UsersDTO creator;
    private String canal_title;
    private String canal_description;
    private String creation_date;
    private String valid_until;
    private boolean is_canal_enabled;

    private List<UsersDTO> joined_users = new ArrayList<>();
    private List<UsersDTO> connected_users = new ArrayList<>();

    public CanalsDTO(int canal_id, UsersDTO creator, String canal_title, String canal_description, String creation_date, String valid_until, boolean is_canal_enabled) {
        this.canal_id = canal_id;
        this.creator = creator;
        this.canal_title = canal_title;
        this.canal_description = canal_description;
        this.creation_date = creation_date;
        this.valid_until = valid_until;
        this.is_canal_enabled = is_canal_enabled;
    }

    public void addJoinedUser(UsersDTO user) {
        joined_users.add(user);
    }
    public void addConnectedUser(UsersDTO user) {
        connected_users.add(user);
    }

    public int getCanal_id() {
        return canal_id;
    }

    public UsersDTO getCreator() {
        return creator;
    }

    public String getCanal_title() {
        return canal_title;
    }

    public String getCanal_description() {
        return canal_description;
    }

    public String getCreation_date() {
        return creation_date;
    }

    public String getValid_until() {
        return valid_until;
    }

    public boolean isIs_canal_enabled() {
        return is_canal_enabled;
    }

    public List<UsersDTO> getJoined_users() {
        return joined_users;
    }

    public List<UsersDTO> getConnected_users() {
        return connected_users;
    }
}
