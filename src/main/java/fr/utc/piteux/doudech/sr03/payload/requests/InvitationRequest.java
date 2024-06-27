package fr.utc.piteux.doudech.sr03.payload.requests;

public class InvitationRequest {
    private int userId;
    private int canalId;

    // getters and setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCanalId() {
        return canalId;
    }

    public void setCanalId(int canalId) {
        this.canalId = canalId;
    }

    @Override
    public String toString() {
        return "InvitationRequest{" +
                "userId=" + userId +
                ", canalId=" + canalId +
                '}';
    }
}