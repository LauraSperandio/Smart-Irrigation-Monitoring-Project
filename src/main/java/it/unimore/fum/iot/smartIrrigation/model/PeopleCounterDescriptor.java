package it.unimore.fum.iot.smartIrrigation.model;

public class PeopleCounterDescriptor {

    public static final String CAMERA_PROVIDER = "camera_provider";

    public static final String RANDOM_PROVIDER = "random_provider";

    private int in;

    private int out;

//    private String provider;

    public PeopleCounterDescriptor() {
    }

    public PeopleCounterDescriptor(int in, int out) {
//    public PeopleCounterDescriptor(int in, int out, String provider) {
        this.in = in;
        this.out = out;
//        this.provider = provider;
    }

    public int getIn() {
        return in;
    }

    public void setIn(int in) {
        this.in = in;
    }

    public int getOut() {
        return out;
    }

    public void setOut(int out) {
        this.out = out;
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
        final StringBuffer sb = new StringBuffer("PeopleCounterDescriptor{");
        sb.append("in=").append(in);
        sb.append(", out=").append(out);
//        sb.append(", provider='").append(provider).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
