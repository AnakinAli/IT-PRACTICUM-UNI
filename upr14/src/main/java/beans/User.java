package beans;

import dao.UserDAO;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Named("user")
@RequestScoped
public class User {
    private static final int MAX_PHOTO_SIZE = 16_777_215;

    @EJB
    private UserDAO userDao;

    private String username;
    private String password;
    private Part photo;
    private List<entities.User> users;
    private final ArrayList<String> userPhotos = new ArrayList<>();
    private Integer selectedUserId;
    private entities.User selectedUser;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Part getPhoto() {
        return photo;
    }

    public void setPhoto(Part photo) {
        this.photo = photo;
    }

    public ArrayList<String> getUserPhotos() {
        return userPhotos;
    }

    public String upload() {
        try {
            byte[] photoContent = readPhoto();
            userDao.addUserToDatabase(
                    new entities.User(username.trim(), password, photoContent));
            return "uploaded?faces-redirect=true";
        } catch (IOException | IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Снимката не може да бъде записана", e.getMessage()));
            return null;
        }
    }

    private byte[] readPhoto() throws IOException {
        if (photo == null || photo.getSize() == 0) {
            return null;
        }
        if (photo.getSize() > MAX_PHOTO_SIZE) {
            throw new IllegalArgumentException("Максималният размер е 15,9 MB.");
        }
        String contentType = photo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Позволени са само изображения.");
        }

        try (InputStream input = photo.getInputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                if (output.size() > MAX_PHOTO_SIZE) {
                    throw new IllegalArgumentException("Максималният размер е 15,9 MB.");
                }
            }
            return output.toByteArray();
        }
    }

    public List<entities.User> getUsers() {
        if (users == null) {
            users = userDao.getAllUsers();
            userPhotos.clear();
            for (entities.User currentUser : users) {
                userPhotos.add(toPhotoSource(currentUser.getPhoto()));
            }
        }
        return users;
    }

    public Integer getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(Integer selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public entities.User getSelectedUser() {
        if (selectedUser == null && selectedUserId != null) {
            selectedUser = userDao.getUserById(selectedUserId);
        }
        return selectedUser;
    }

    public String getSelectedUserPhoto() {
        entities.User currentUser = getSelectedUser();
        return toPhotoSource(currentUser == null ? null : currentUser.getPhoto());
    }

    private String toPhotoSource(byte[] photoBytes) {
        if (photoBytes == null || photoBytes.length == 0) {
            String contextPath = FacesContext.getCurrentInstance()
                    .getExternalContext().getRequestContextPath();
            return contextPath + "/resources/images/defaultAvatar.svg";
        }
        return "data:image/*;base64," + Base64.getEncoder().encodeToString(photoBytes);
    }
}
