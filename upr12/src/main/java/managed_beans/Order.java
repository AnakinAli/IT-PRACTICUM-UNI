package managed_beans;

import dao.OfficeDAO;
import entities.Office;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ManagedBean(name = "order")
@ViewScoped
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private OfficeDAO officeDAO;

    private String city;
    private String newCity;
    private String newAddress;
    private ArrayList<String> addresses = new ArrayList<>();

    public void filterAddresses() {
        addresses = new ArrayList<>(officeDAO.getAddressesForCity(city));
    }

    public void addOfficeAddress() {
        String cleanCity = normalize(newCity);
        String cleanAddress = normalize(newAddress);

        if (cleanCity.isEmpty() || cleanAddress.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Градът и адресът не могат да съдържат само интервали.", null));
            return;
        }

        if (officeDAO.cityExists(cleanCity)) {
            officeDAO.addAddress(cleanCity, cleanAddress);
        } else {
            officeDAO.saveNewOffice(new Office(cleanCity, Collections.singletonList(cleanAddress)));
        }

        city = cleanCity;
        addresses = new ArrayList<>(officeDAO.getAddressesForCity(city));
        newCity = null;
        newAddress = null;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Адресът е записан успешно.", null));
    }

    public List<String> getCities() { return officeDAO.getCities(); }
    private String normalize(String value) { return value == null ? "" : value.trim(); }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getNewCity() { return newCity; }
    public void setNewCity(String newCity) { this.newCity = newCity; }
    public String getNewAddress() { return newAddress; }
    public void setNewAddress(String newAddress) { this.newAddress = newAddress; }
    public ArrayList<String> getAddresses() { return addresses; }
    public void setAddresses(ArrayList<String> addresses) { this.addresses = addresses; }
}
