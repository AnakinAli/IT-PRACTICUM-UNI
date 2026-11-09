package managed_beans;

import database.Database;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;

@RequestScoped
@Named("order")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<String> filteredOffices = new ArrayList<String>();
    private String city;
    private String selectedOffice;
    private String restUsername;
    private String username;
    private String status = "";
    private String resultMessage;

    @PostConstruct
    public void init() {
        String submittedCity = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("myForm:city");
        if (submittedCity != null) {
            city = submittedCity;
            filteredOffices = Database.getAddressesForCity(city);
        }
    }

    public void keyUp(AjaxBehaviorEvent event) {
        filteredOffices = Database.getAddressesForCity(city);
    }

    public void checkUsername(AjaxBehaviorEvent event) {
        status = Database.usernameExists(username) ? "Потребителското име е заето" : "";
    }

    public String send() {
        resultMessage = selectedOffice == null ? "Моля, изберете офис."
                : "Избран офис: " + selectedOffice;
        return null;
    }

    public ArrayList<String> getFilteredOffices() { return filteredOffices; }
    public void setFilteredOffices(ArrayList<String> value) { filteredOffices = value; }
    public String getCity() { return city; }
    public void setCity(String value) { city = value; }
    public String getSelectedOffice() { return selectedOffice; }
    public void setSelectedOffice(String value) { selectedOffice = value; }
    public String getRestUsername() { return restUsername; }
    public void setRestUsername(String value) { restUsername = value; }
    public String getUsername() { return username; }
    public void setUsername(String value) { username = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getResultMessage() { return resultMessage; }
}
