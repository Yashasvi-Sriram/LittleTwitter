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
        if (request.getSession(false) == null) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("status", false);
                obj.put("message", "Invalid session");
                out.print(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                out.print(DBHandler.deAuthenticate(request));
                System.out.println("Logged Out");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        out.close();
    }

}
