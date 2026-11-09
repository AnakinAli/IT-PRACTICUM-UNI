package dao;

import entities.User;

import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateful
public class UserDAO {
    @PersistenceContext(unitName = "DefaultPersistenceUnit")
    private EntityManager em;

    public void addUserToDatabase(User user) {
        em.persist(user);
    }

    public List<User> getAllUsers() {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u ORDER BY u.id", User.class);
        return query.getResultList();
    }

    public User getUserById(int id) {
        return em.find(User.class, id);
    }
}
