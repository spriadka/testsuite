package org.jboss.hal.testsuite.test.runtime;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {UploadServlet.URL_PATTERN})
@MultipartConfig
public class UploadServlet extends HttpServlet {

    public static final String URL_PATTERN = "/upload";

    private static final Logger log = LoggerFactory.getLogger(UploadServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        out.write("First images part: ");
        Part imagesPart = request.getPart("images"); // returns only the last part with test3.png
        out.write(imagesPart.getHeader("Content-Disposition"));

        out.write("\n");
        out.write("List of all parts: \n");
        for (Part part : request.getParts()) { // getParts() returns only 1 part, that part is the part with test3.png
            out.write(part.getHeader("Content-Disposition"));
            out.write("\n");
        }
    }
}

