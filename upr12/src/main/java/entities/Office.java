package entities;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Office implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String city;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "office_addresses", joinColumns = @JoinColumn(name = "office_id"))
    @Column(name = "address", nullable = false)
    private List<String> address = new ArrayList<>();

    public Office() {
    }

    public Office(String city, List<String> address) {
        this.city = city;
        this.address = address == null ? new ArrayList<>() : new ArrayList<>(address);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public List<String> getAddress() { return address; }
    public void setAddress(List<String> address) {
        this.address = address == null ? new ArrayList<>() : new ArrayList<>(address);
    }
}
