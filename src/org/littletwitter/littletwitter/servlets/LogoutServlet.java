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

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject obj = new JSONObject();
        try {
            if (request.getSession(false) == null) {
                obj.put("status", false);
                obj.put("message", "Invalid Session");
            } else {
                request.getSession(false).invalidate();
                obj.put("status", true);
                obj.put("data", "Successfully logged out");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        out.print(obj);
        out.close();

        System.out.println("Logged Out");
    }

}
