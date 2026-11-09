package servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@WebServlet("/GenereteZipArchive")
public class GenereteZipArchive extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private void SendArchiveToStream(String path, OutputStream os)
            throws IOException, FileNotFoundException {
        File dir = new File(path);
        File[] filesOrDirs = dir.listFiles();

        if (filesOrDirs == null) {
            throw new FileNotFoundException("Папката с файлове не е намерена: " + path);
        }

        try (ZipOutputStream zipArchive = new ZipOutputStream(os)) {
            byte[] data = new byte[1024];

            for (File fileOrDir : filesOrDirs) {
                if (fileOrDir.isDirectory()) {
                    continue;
                }

                ZipEntry entry = new ZipEntry(fileOrDir.getName());
                zipArchive.putNextEntry(entry);

                try (FileInputStream file = new FileInputStream(fileOrDir)) {
                    int length;
                    while ((length = file.read(data)) != -1) {
                        zipArchive.write(data, 0, length);
                    }
                }

                zipArchive.closeEntry();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = request.getServletContext();
        String path = context.getRealPath("/files");

        if (path == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Папката files не е достъпна като реална директория.");
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=MyArchive.zip");

        OutputStream os = response.getOutputStream();
        SendArchiveToStream(path, os);
    }
}
