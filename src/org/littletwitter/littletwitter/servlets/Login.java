package org.littletwitter.littletwitter.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/Login")
public class Login extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("here");
        String id=null;
        String password=null;
        try {
        	id = URLDecoder.decode(request.getParameter("userId"),"UTF-8") ;
            password = URLDecoder.decode(request.getParameter("password"),"UTF-8");
        }catch(Exception e) {
        	System.out.println("Error");
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(DBHandler.authenticate(id, password, request));
        out.close();
    }

}