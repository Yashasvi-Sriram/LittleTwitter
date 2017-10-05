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

@WebServlet("/Unfollow")
public class Unfollow extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        //JSONObject obj = new JSONObject();
        if (request.getSession(false) == null) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("status", false);
                obj.put("message", "Invalid session");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            out.print(obj);
        } else {
            request.getSession();
            String uid1 = (String) request.getSession().getAttribute("id");
            String uid2 = request.getParameter("uid");
            out.print(DBHandler.unFollow(uid1, uid2));
        }
        out.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
