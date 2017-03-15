package org.jboss.hal.testsuite.test.runtime.messaging.deployment;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "MessagingStatisticsServlet", urlPatterns = {"/"})
public class MessagingStatisticsServlet extends HttpServlet {

    private static final long serialVersionUID = -8836136359009135619L;

    @EJB
    MessagingStatisticsBean bean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<p>Calling bean: <b>" + bean.getClass().getName() + "</b></p>");
        bean.commit();
        bean.rollback();
    }

}
