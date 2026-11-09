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
    private String resultMessage;

    @PostConstruct
    public void init() {
        /*
         * A request-scoped bean is recreated for the final form submit.
         * Restore the choices before JSF validates the submitted radio value.
         */
        String submittedCity = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("myForm:city");

        if (submittedCity != null) {
            city = submittedCity;
            filteredOffices = Database.getAddressesForCity(city);
        }
    }

    public ArrayList<String> getFilteredOffices() {
        return filteredOffices;
    }

    public void setFilteredOffices(ArrayList<String> filteredOffices) {
        this.filteredOffices = filteredOffices;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSelectedOffice() {
        return selectedOffice;
    }

    public void setSelectedOffice(String selectedOffice) {
        this.selectedOffice = selectedOffice;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void keyUp(AjaxBehaviorEvent event) {
        filteredOffices = Database.getAddressesForCity(city);
    }

    public String send() {
        resultMessage = selectedOffice == null
                ? "Моля, изберете офис."
                : "Избран офис: " + selectedOffice;
        return null;
    }
}
