package org.jboss.hal.testsuite.test.runtime;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet(urlPatterns = {DataSenderServlet.URL_PATTERN})
@MultipartConfig
public class DataSenderServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(DataSenderServlet.class);

    public static final String URL_PATTERN = "/download";
    public static final String AMOUNT_OF_DATA_IN_MB_ATTR = "amountOfDataInMB";


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int amountOfDataInMB = Integer.parseInt(request.getParameter(AMOUNT_OF_DATA_IN_MB_ATTR));

        OutputStream out = response.getOutputStream();
        byte[] generatedData = new byte[1024 * 1024];
        for (int i = 0; i < amountOfDataInMB; i++) {
            out.write(generatedData);
        }
        out.flush();
    }
}

