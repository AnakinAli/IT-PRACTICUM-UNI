package database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Database {
    private static final HashMap<String, ArrayList<String>> offices =
            new HashMap<String, ArrayList<String>>();
    private static final ArrayList<String> users = new ArrayList<String>(Arrays.asList(
            "ivan", "maria", "student"
    ));

    static {
        addOffices("Русе", "ул. Александровска 26", "бул. Липник 121", "ул. Борисова 42");
        addOffices("Стара Загора", "бул. Цар Симеон Велики 112",
                "ул. Генерал Столетов 28", "бул. Патриарх Евтимий 80");
        addOffices("Нова Загора", "ул. Свети Свети Кирил и Методий 12",
                "пл. Свобода 3", "ул. Васил Левски 20");
    }

    private Database() { }

    private static void addOffices(String city, String... addresses) {
        offices.put(city, new ArrayList<String>(Arrays.asList(addresses)));
    }

    public static boolean usernameExists(String username) {
        if (username == null) {
            return false;
        }
        String normalizedUsername = username.trim();
        return users.stream().anyMatch(user -> user.equalsIgnoreCase(normalizedUsername));
    }

    public static ArrayList<String> getAddressesForCity(String searchText) {
        ArrayList<String> filteredOffices = new ArrayList<String>();
        if (searchText == null || searchText.trim().isEmpty()) {
            return filteredOffices;
        }

        String[] parts = searchText.split(",", 2);
        final String cityPart = parts[0].trim().toLowerCase(Locale.ROOT);
        final String addressPart = parts.length > 1
                ? parts[1].trim().toLowerCase(Locale.ROOT) : "";

        offices.entrySet().stream()
                .filter(element -> element.getKey().toLowerCase(Locale.ROOT).contains(cityPart))
                .sorted(Map.Entry.comparingByKey())
                .forEach(element -> element.getValue().stream()
                        .filter(address -> address.toLowerCase(Locale.ROOT).contains(addressPart))
                        .forEach(address -> filteredOffices.add(element.getKey() + ", " + address)));
        return filteredOffices;
    }
}
