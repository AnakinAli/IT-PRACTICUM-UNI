package servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

@WebServlet("/FileList")
public class FileList extends HttpServlet {

    private static final long serialVersionUID = 1L;

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

        String delimiter = request.getParameter("delimiter");
        if (delimiter == null) {
            delimiter = ",";
        }

        File[] files = new File(path).listFiles(File::isFile);
        if (files == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Папката files не е намерена.");
            return;
        }

        String fileNames = Arrays.stream(files)
                .map(File::getName)
                .sorted()
                .collect(Collectors.joining(delimiter));

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(fileNames);
        }
    }
}
