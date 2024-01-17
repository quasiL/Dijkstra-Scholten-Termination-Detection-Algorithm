package cz.cvut.dsv.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
public class Address implements Serializable {

    @Getter
    private final String ipAddress;

    @Getter
    private final Integer port;

    public Address(Address address) {
        this(address.getIpAddress(), address.getPort());
    }

    @Override
    public String toString() {
        return ipAddress + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Address)) {
            return false;
        }
        Address address = (Address) o;

        return Objects.equals(this.ipAddress, address.ipAddress) && Objects.equals(this.port, address.port);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.ipAddress != null ? this.ipAddress.hashCode() : 0);
        hash = 53 * hash + this.port;
        return hash;
    }
}
