package fr.utc.piteux.doudech.sr03.payload.requests;

import jakarta.validation.constraints.NotNull;

public class InvitationRequestResponse {
    @NotNull(message = "invitation_id is mandatory")
    private int invitationId;
    @NotNull(message = "canal_id is mandatory")
    private int canalId;
    @NotNull(message = "status is mandatory")
    private String status;

    // getters and setters

    public int getCanalId() {
        return canalId;
    }

    public void setCanalId(int canalId) {
        this.canalId = canalId;
    }

    public long getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(int invitationId) {
        this.invitationId = invitationId;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "InvitationRequest{" +
                ", canalId='" + canalId + '\'' +
                ", invitationId='" + invitationId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

}