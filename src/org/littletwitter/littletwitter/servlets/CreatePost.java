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

@WebServlet("/CreatePost")
public class CreatePost extends HttpServlet {
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
            String id = (String) request.getSession().getAttribute("id");
            String post = request.getParameter("content");
            out.print(DBHandler.createPost(id, post));
            out.close();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
