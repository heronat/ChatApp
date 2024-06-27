package fr.utc.piteux.doudech.sr03.models;

import jakarta.persistence.*;

@Entity
@Table(name="invitations")
public class Invitation {

    public enum Status {
        PENDING,
        ACCEPTED,
        DECLINED,
        LEAVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "canal_id", nullable = false)
    private Canals canal;

    @ManyToOne
    @JoinColumn(name = "inviter_id")
    private Users inviter;

    @ManyToOne
    @JoinColumn(name = "invitee_id", nullable = false)
    private Users invitee;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Status status;

    // getters and setters


    public Invitation(Canals canal, Users inviter, Users invitee) {
        this.canal = canal;
        this.inviter = inviter;
        this.invitee = invitee;
        this.status = Status.PENDING;
    }

    public Invitation() {
    }

    public Long getId() {
        return id;
    }

    public Canals getCanal() {
        return canal;
    }

    public Users getInviter() {
        return inviter;
    }

    public Users getInvitee() {
        return invitee;
    }

    public Status getStatus() {
        return status;
    }

    public void setCanal(Canals canal) {
        this.canal = canal;
    }

    public void setInviter(Users inviter) {
        this.inviter = inviter;
    }

    public void setInvitee(Users invitee) {
        this.invitee = invitee;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}