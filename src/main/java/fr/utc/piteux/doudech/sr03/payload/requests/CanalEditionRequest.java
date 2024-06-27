package fr.utc.piteux.doudech.sr03.payload.requests;

import jakarta.validation.constraints.NotNull;

import java.util.Date;

public class CanalEditionRequest extends CanalCreationRequest {

    @NotNull(message = "canal is mandatory")
    private final Integer canal;
    public CanalEditionRequest(String canalTitle, String canalDescription, Date creationDate, Date validUntilDate, boolean canalEnabled, Integer canal) {
        super(canalTitle, canalDescription, creationDate, validUntilDate, canalEnabled);
        this.canal = canal;
    }

    public Integer getCanal() {
        return canal;
    }
}