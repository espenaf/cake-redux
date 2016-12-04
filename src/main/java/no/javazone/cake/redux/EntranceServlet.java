package no.javazone.cake.redux;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class EntranceServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        if (req.getParameter("error") != null) {
            writer.println(req.getParameter("error"));
            return;
        }

        String code = req.getParameter("code");

        StringBuilder postParameters = new StringBuilder();
        postParameters.append(para("code", code)).append("&");
        postParameters.append(para("client_id", Configuration.getGoogleClientId())).append("&");
        postParameters.append(para("client_secret", Configuration.getGoogleClientSecret())).append("&");
        postParameters.append(para("redirect_uri", Configuration.getGoogleRedirectUrl())).append("&");
        postParameters.append(para("grant_type", "authorization_code"));
        URL url = new URL("https://accounts.google.com/o/oauth2/token");
        URLConnection urlConnection = url.openConnection();


        ((HttpURLConnection)urlConnection).setRequestMethod("POST");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Content-Length", "" + postParameters.toString().length());

        // Create I/O streams
        DataOutputStream outStream = new DataOutputStream(urlConnection.getOutputStream());
        // Send request
        outStream.writeBytes(postParameters.toString());
        outStream.flush();
        outStream.close();

        String googleresp;
        try (InputStream inputStream = urlConnection.getInputStream()) {
            googleresp = CommunicatorHelper.toString(inputStream);
        }

        String accessToken;
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonObject = (ObjectNode) objectMapper.readTree(googleresp);
        // get the access token from json and request info from Google
        accessToken = jsonObject.get("access_token").asText();

        // get some info about the user with the access token
        String getStr = "https://www.googleapis.com/oauth2/v1/userinfo?" + para("access_token",accessToken);
        URLConnection inconn = new URL(getStr).openConnection();
        String json;
        try (InputStream is = inconn.getInputStream()) {
            json = CommunicatorHelper.toString(is);
        }


        String username = null;
        String userEmail = null;
        JsonNode userInfo = objectMapper.readTree(json);
        username = userInfo.get("name").asText();
        userEmail = userInfo.get("email").asText();

        // When using not using G+, name can be blank
        username = getNameFromConfigIfBlank(username, userEmail);

        String userid = username + "<" + userEmail + ">";
        if (!haveAccess(userEmail)) {
            resp
                    .sendError(HttpServletResponse.SC_FORBIDDEN, "User not registered " + userid);
            return;
        }

        req.getSession().setAttribute("access_token", userid);
        req.getSession().setAttribute("username", username);

        writeLoginMessage(resp, writer, userid);
    }

    private String getNameFromConfigIfBlank(String username, String userEmail) {
        if(username.length() > 0) return username;

        return Stream.of(Configuration.getAutorizedUsers().split(","))
                .filter(u -> u.contains(userEmail))
                .map(u -> u.split("<")[0])
                .findFirst().orElse(username);
    }

    private boolean haveAccess(String userid) {
        if (Configuration.getAutorizedUsers().contains(userid)) {
            return true;
        }
        String autorizedUserFile = Configuration.autorizedUserFile();
        if (autorizedUserFile == null) {
            return false;
        }
        String authUsers;
        try {
            authUsers = CommunicatorHelper.toString(new FileInputStream(autorizedUserFile));
        } catch (IOException e) {
            return false;
        }
        return authUsers.contains(userid);
    }

    public static void writeLoginMessage(HttpServletResponse resp, PrintWriter writer, String userid) {
        resp.setContentType("text/html");
        writer.append("<html><body>");
        writer.append("<p>You are now logged in as ").append(userid).append("</p>");
        writer.append("<p><a href='secured/#/'>To cake</a></p>");
        writer.append("</body></html>");
    }

    private String para(String name,String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(name, "utf-8") + "=" + URLEncoder.encode(value,"UTF-8");
    }

}
