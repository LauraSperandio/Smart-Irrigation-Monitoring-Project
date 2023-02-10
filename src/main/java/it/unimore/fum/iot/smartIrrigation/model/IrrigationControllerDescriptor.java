package it.unimore.fum.iot.smartIrrigation.model;

public class IrrigationControllerDescriptor {

/*    public static final String CAMERA_PROVIDER = "camera_provider";

    public static final String RANDOM_PROVIDER = "random_provider";*/

    private String accensione;

    private String policyConfiguration;

    private String livelloIrrigazione;

    private String tipologiaIrrigazione;

//    private String provider;

    public IrrigationControllerDescriptor() {
    }

    public IrrigationControllerDescriptor(String accensione, String policyConfiguration, String livelloIrrigazione, String tipologiaIrrigazione) {
        this.accensione = accensione;
        this.policyConfiguration = policyConfiguration;
        this.livelloIrrigazione = livelloIrrigazione;
        this.tipologiaIrrigazione = tipologiaIrrigazione;
    }

    public String getAccensione() {
        return accensione;
    }

    public void setAccensione(String accensione) {
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

    public String getTipologiaIrrigazione() {
        return tipologiaIrrigazione;
    }

    public void setTipologiaIrrigazione(String tipologiaIrrigazione) {
        this.tipologiaIrrigazione = tipologiaIrrigazione;
    }

    /**
     public String getProvider() {
     return provider;
     }

     public void setProvider(String provider) {
     this.provider = provider;
     }
     */

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("IrrigationControllerDescriptor{");
        sb.append("accensione=").append(accensione);
        sb.append(", policyConfiguration=").append(policyConfiguration);
        sb.append(", livelloIrrigazione=").append(livelloIrrigazione);
        sb.append(", tipologiaIrrigazione=").append(tipologiaIrrigazione);
//        sb.append(", provider='").append(provider).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
