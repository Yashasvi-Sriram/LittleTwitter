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

@WebServlet("/SeeMyPosts")
public class SeeMyPosts extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject obj = new JSONObject();
        if (request.getSession(false) == null) {
            try {
                obj.put("status", false);
                obj.put("message", "Invalid session");
                out.print(obj);
            } catch (JSONException e) {

                e.printStackTrace();
            }
        } else {
            int offset = 0;
            int limit = 1000;
            request.getSession();
            if (request.getParameter("offset") != null)
                offset = Integer.parseInt(request.getParameter("offset"));
            if (request.getParameter("limit") != null)
                limit = Integer.parseInt(request.getParameter("limit"));
            request.getSession();
            String id = (String) request.getSession().getAttribute("id");
            try {
                obj.put("status", true);
                obj.put("data", DBHandler.seeMyPosts(id, offset, limit));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            out.print(obj);
        }


    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
