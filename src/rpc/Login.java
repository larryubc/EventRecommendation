package rpc;

import db.DBConnection;
import db.DBConnectionFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


@WebServlet("/login")
public class Login extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DBConnection conn = DBConnectionFactory.getDBConnection();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            JSONObject msg = new JSONObject();
            HttpSession session = request.getSession();
            if (session.getAttribute("user") == null) {
                response.setStatus(403);
                msg.put("status", "Session Invalid");
            } else {
                String user = (String) session.getAttribute("user");
                String name = conn.getFullname(user);
                msg.put("status", "OK");
                msg.put("user_id", user);
                msg.put("name", name);
            }
            RpcHelper.writeJsonObject(response, msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            JSONObject msg = new JSONObject();

            String user = request.getParameter("user_id");
            String pwd = request.getParameter("password");

            if (conn.verifyLogin(user, pwd)) {

                HttpSession session = request.getSession();
                session.setAttribute("user", user);

                session.setMaxInactiveInterval(20 * 60);

                String name = conn.getFullname(user);
                msg.put("status", "OK");
                msg.put("user_id", user);
                msg.put("name", name);
            } else {
                response.setStatus(401);
            }
            RpcHelper.writeJsonObject(response, msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
