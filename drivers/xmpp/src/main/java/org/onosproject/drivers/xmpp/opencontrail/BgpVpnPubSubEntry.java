package org.onosproject.drivers.xmpp.opencontrail;

public class BgpVpnPubSubEntry {

    private int label;
    private int nrliAf;
    private String nrliIpAddress;
    private int nextHopAf;
    private String nextHopAddress;
    private int version;

    public BgpVpnPubSubEntry(int label, int nrliAf, String nrliIpAddress, int nextHopAf, String nextHopAddress, int version) {
        this.label = label;
        this.nrliAf = nrliAf;
        this.nrliIpAddress = nrliIpAddress;
        this.nextHopAf = nextHopAf;
        this.nextHopAddress = nextHopAddress;
        this.version = version;
    }

    public int getLabel() {
        return label;
    }

    public int getNrliAf() {
        return nrliAf;
    }

    public String getNrliIpAddress() {
        return nrliIpAddress;
    }

    public int getNextHopAf() {
        return nextHopAf;
    }

    public String getNextHopAddress() {
        return nextHopAddress;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "BgpVpnPubSubEntry{" +
                "label=" + label +
                ", nrliAf=" + nrliAf +
                ", nrliIpAddress='" + nrliIpAddress + '\'' +
                ", nextHopAf=" + nextHopAf +
                ", nextHopAddress='" + nextHopAddress + '\'' +
                ", version=" + version +
                '}';
    }
}
