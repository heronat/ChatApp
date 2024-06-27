package fr.utc.piteux.doudech.sr03.payload.requests;

import java.util.Date;

public class CanalCreationRequest {

    private final String canalTitle;
    private final String canalDescription;
    private Date creationDate;
    private Date validUntilDate;
    private final boolean canalEnabled;

    public CanalCreationRequest(String canalTitle, String canalDescription, Date creationDate, Date validUntilDate, boolean canalEnabled) {
        this.canalTitle = canalTitle;
        this.canalDescription = canalDescription;
        this.creationDate = creationDate;
        this.validUntilDate = validUntilDate;
        this.canalEnabled = canalEnabled;
    }

    public String getCanalTitle() {
        return canalTitle;
    }

    public String getCanalDescription() {
        return canalDescription;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getValidUntilDate() {
        return validUntilDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setValidUntilDate(Date validUntilDate) {
        this.validUntilDate = validUntilDate;
    }

    public boolean isCanalEnabled() {
        return canalEnabled;
    }

    @Override
    public String toString() {
        return "CanalCreationObj{" +
                "canal_title="  + this.canalTitle +
                ", canal_title=" + this.canalTitle +
                ", canal_exp_date=" + this.validUntilDate.toString() +
                '}';
    }
}

