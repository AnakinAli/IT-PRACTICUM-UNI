package dao;

import entities.Office;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class OfficeDAO {
    @PersistenceContext(unitName = "DefaultPersistenceUnit")
    private EntityManager em;

    public void saveNewOffice(Office office) {
        em.persist(office);
    }

    public boolean addAddress(String city, String address) {
        List<Office> offices = findByCity(city);
        if (offices.isEmpty()) {
            return false;
        }
        offices.get(0).getAddress().add(address);
        return true;
    }

    public List<String> getAddressesForCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Office> offices = findByCity(city.trim());
        if (offices.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(offices.get(0).getAddress());
    }

    public boolean cityExists(String city) {
        return city != null && !city.trim().isEmpty() && !findByCity(city.trim()).isEmpty();
    }

    public List<String> getCities() {
        return em.createQuery("SELECT office.city FROM Office office ORDER BY office.city", String.class)
                .getResultList();
    }

    private List<Office> findByCity(String city) {
        TypedQuery<Office> query = em.createQuery(
                "SELECT office FROM Office office WHERE office.city = :city", Office.class);
        query.setParameter("city", city);
        query.setMaxResults(1);
        return query.getResultList();
    }
}
