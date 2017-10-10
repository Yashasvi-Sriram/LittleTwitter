package org.littletwitter.littletwitter.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/UserFollow")
public class UserFollow extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject obj = new JSONObject();
        if (request.getSession(false) == null) {
            try {
                obj.put("status", false);
                obj.put("message", "Invalid session");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            String uid = (String) request.getSession().getAttribute("userId");
            try {
                obj.put("status", true);
                obj.put("data", DBHandler.userFollow(uid));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            out.close();
        }
        out.print(obj);
        out.close();
    }

}
