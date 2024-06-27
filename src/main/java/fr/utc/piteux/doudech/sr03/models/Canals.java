package fr.utc.piteux.doudech.sr03.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="canals")
public class Canals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="cid")
    private int cid;

    @ManyToOne
    @JoinColumn(name="creator_id")
    @NotNull(message = "creator is mandatory")
    private Users creator;
    @Column(name="canal_title")
    @NotBlank(message = "canalTitle is mandatory")
    private String canalTitle;
    @Column(name="canal_description")
    private String canalDescription;
    @Column(name="creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @Column(name="valid_until")
    private Date validUntilDate;
    @Column(name="is_canal_enabled")
    private boolean isCanalEnabled;

    @ManyToMany
    @JoinTable( name = "users_canals",
            joinColumns = @JoinColumn( name = "canal_id" ),
            inverseJoinColumns = @JoinColumn( name = "users_id" ) )
    private List<Users> usersJoined = new ArrayList<>();

    public Canals(Users creator, String canalTitle, String canalDescription, Date creationDate, Date validUntilDate) {
        this.creator = creator;
        this.canalTitle = canalTitle;
        this.canalDescription = canalDescription;
        this.creationDate = creationDate;
        this.validUntilDate = validUntilDate;
        this.isCanalEnabled = true;
        this.usersJoined.add(creator);
    }

    public Canals() {

    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public Users getCreatorId() {
        return creator;
    }

    public void setCreatorId(Users creatorId) {
        this.creator = creatorId;
    }

    public String getCanalTitle() {
        return canalTitle;
    }

    public void setCanalTitle(String canalTitle) {
        this.canalTitle = canalTitle;
    }

    public String getCanalDescription() {
        return canalDescription;
    }

    public void setCanalDescription(String canalDescription) {
        this.canalDescription = canalDescription;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getValidUntilDate() {
        return validUntilDate;
    }

    public void setValidUntilDate(Date validUntilDate) {
        this.validUntilDate = validUntilDate;
    }

    public boolean isCanalEnabled() {
        return isCanalEnabled;
    }

    public void setCanalEnabled(boolean canalEnabled) {
        isCanalEnabled = canalEnabled;
    }

    public List<Users> getUsersJoined() {
        return usersJoined;
    }

    public void addUserJoined(Users user){
        this.usersJoined.add(user);
    }

    public void removeUser(Users user){
        this.usersJoined.remove(user);
    }

    @Override
    public String toString() {
        return "Canal{" +
                "canal_id=" + this.cid +
                ", canal_title=" + this.canalTitle +
                ", canal_title=" + this.creator.getEmail() +
                '}';
    }
}
