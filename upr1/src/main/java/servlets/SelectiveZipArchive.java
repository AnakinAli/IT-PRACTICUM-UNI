package servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@WebServlet("/SelectiveZipArchive")
public class SelectiveZipArchive extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private void sendSelectedArchiveToStream(String path, String[] fileNames, OutputStream os)
            throws IOException {
        Path filesDirectory = new File(path).getCanonicalFile().toPath();

        try (ZipOutputStream zipArchive = new ZipOutputStream(os)) {
            byte[] data = new byte[1024];

            for (String fileName : fileNames) {
                File selectedFile = filesDirectory.resolve(fileName).normalize().toFile().getCanonicalFile();

                if (!selectedFile.toPath().startsWith(filesDirectory) || !selectedFile.isFile()) {
                    continue;
                }

                zipArchive.putNextEntry(new ZipEntry(selectedFile.getName()));
                try (FileInputStream file = new FileInputStream(selectedFile)) {
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
        String[] fileNames = request.getParameterValues("file");
        if (fileNames == null || fileNames.length == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Трябва да изберете поне един файл.");
            return;
        }

        ServletContext context = request.getServletContext();
        String path = context.getRealPath("/files");
        if (path == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Папката files не е достъпна като реална директория.");
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=SelectedFiles.zip");

        sendSelectedArchiveToStream(path, fileNames, response.getOutputStream());
    }
}
