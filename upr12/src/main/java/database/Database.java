package database;

import dao.OfficeDAO;
import entities.Office;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Arrays;

@Singleton
@Startup
public class Database {
    @EJB
    private OfficeDAO officeDAO;

    @PostConstruct
    public void init() {
        addOfficeIfMissing("София", "бул. Витоша 1", "ул. Граф Игнатиев 10");
        addOfficeIfMissing("Пловдив", "ул. Княз Александър I 15");
        addOfficeIfMissing("Варна", "бул. Княз Борис I 53");
    }

    private void addOfficeIfMissing(String city, String... addresses) {
        if (!officeDAO.cityExists(city)) {
            officeDAO.saveNewOffice(new Office(city, Arrays.asList(addresses)));
        }
    }
}
