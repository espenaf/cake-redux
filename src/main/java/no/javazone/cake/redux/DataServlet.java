package no.javazone.cake.redux;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class DataServlet extends HttpServlet {
    private EmsCommunicator emsCommunicator;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/json");
        PrintWriter writer = response.getWriter();
        String pathInfo = request.getPathInfo();
        if ("/talks".equals(pathInfo)) {
            String encEvent = request.getParameter("eventId");
            writer.append(emsCommunicator.talkShortVersion(encEvent));
        } else if ("/atalk".equals(pathInfo)) {
            String encTalk = request.getParameter("talkId");
            writer.append(emsCommunicator.fetchOneTalk(encTalk));
        } else {
            writer.append(emsCommunicator.allEvents());
        }
    }

    @Override
    public void init() throws ServletException {
        emsCommunicator = new EmsCommunicator();
    }

    public void setEmsCommunicator(EmsCommunicator emsCommunicator) {
        this.emsCommunicator = emsCommunicator;
    }
}
