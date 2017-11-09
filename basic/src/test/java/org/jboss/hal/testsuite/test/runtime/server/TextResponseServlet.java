package org.jboss.hal.testsuite.test.runtime.server;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/infinity")
public class TextResponseServlet extends HttpServlet {

    public static final String URL_PATTERN = "/infinity";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(200);
        PrintWriter out = response.getWriter();
        out.print("To infinity and beyond");
        out.close();
    }
}
