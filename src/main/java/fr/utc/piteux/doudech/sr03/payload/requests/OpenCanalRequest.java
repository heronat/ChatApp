package fr.utc.piteux.doudech.sr03.payload.requests;

import jakarta.validation.constraints.NotNull;

public class OpenCanalRequest {
    @NotNull(message = "Canal id is required")
    private int canalId;

    public OpenCanalRequest(int canalId) {
        this.canalId = canalId;
    }
    public OpenCanalRequest() {
    }

    public void setCanalId(int canalId) {
        this.canalId = canalId;
    }

    public int getCanalId() {
        return canalId;
    }
}
