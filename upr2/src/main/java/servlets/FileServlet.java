package servlets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/FileServlet")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024L * 1024L * 100L,
        maxRequestSize = 1024L * 1024L * 200L
)
public class FileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Path UPLOAD_DIRECTORY =
            Paths.get(System.getProperty("java.io.tmpdir"), "upr2-uploads");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            saveUploadedFiles(request, "file_1");
            response.sendRedirect("fileUploaded.html");
        } catch (Exception exception) {
            getServletContext().log("Грешка при качване на файл", exception);
            response.sendRedirect("error.html");
        }
    }

    /** Saves every uploaded part named inputFileVariable. */
    private void saveUploadedFiles(HttpServletRequest request, String inputFileVariable)
            throws IOException, ServletException {
        Files.createDirectories(UPLOAD_DIRECTORY);
        boolean fileFound = false;

        for (Part filePart : request.getParts()) {
            if (!inputFileVariable.equals(filePart.getName()) || filePart.getSize() == 0) {
                continue;
            }

            String submittedName = filePart.getSubmittedFileName();
            if (submittedName == null || submittedName.isBlank()) {
                continue;
            }

            // getFileName() removes any path supplied by the browser and prevents path traversal.
            String safeFileName = Paths.get(submittedName).getFileName().toString();
            filePart.write(UPLOAD_DIRECTORY.resolve(safeFileName).toString());
            fileFound = true;
        }

        if (!fileFound) {
            throw new ServletException("Не е избран файл за качване.");
        }
    }
}
