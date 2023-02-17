package it.unimore.fum.iot.smartIrrigation.model;

public class IrrigationControllerDescriptor {


    private Boolean accensione;

    private String policyConfiguration;

    private String livelloIrrigazione;

    private Boolean tipologiaIrrigazione;


    public IrrigationControllerDescriptor() {
    }

    public IrrigationControllerDescriptor(Boolean accensione, String policyConfiguration, String livelloIrrigazione, Boolean tipologiaIrrigazione) {
        this.accensione = accensione;
        this.policyConfiguration = policyConfiguration;
        this.livelloIrrigazione = livelloIrrigazione;
        this.tipologiaIrrigazione = tipologiaIrrigazione;
    }

    public Boolean getAccensione() {
        return accensione;
    }

    public void setAccensione(Boolean accensione) {
        this.accensione = accensione;
    }

    public String getPolicyConfiguration() {
        return policyConfiguration;
    }

    public void setPolicyConfiguration(String policyConfiguration) {
        this.policyConfiguration = policyConfiguration;
    }

    public String getLivelloIrrigazione() {
        return livelloIrrigazione;
    }

    public void setLivelloIrrigazione(String livelloIrrigazione) {
        this.livelloIrrigazione = livelloIrrigazione;
    }

    public Boolean getTipologiaIrrigazione() {
        return tipologiaIrrigazione;
    }

    public void setTipologiaIrrigazione(Boolean tipologiaIrrigazione) {
        this.tipologiaIrrigazione = tipologiaIrrigazione;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("IrrigationControllerDescriptor{");
        sb.append("accensione=").append(accensione);
        sb.append(", policyConfiguration=").append(policyConfiguration);
        sb.append(", livelloIrrigazione=").append(livelloIrrigazione);
        sb.append(", tipologiaIrrigazione=").append(tipologiaIrrigazione);
        sb.append('}');
        return sb.toString();
    }
}
