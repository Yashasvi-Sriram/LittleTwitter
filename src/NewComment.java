
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/NewComment")
public class NewComment extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
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
            String id = (String) request.getSession().getAttribute("userId");
            String text = request.getParameter("text");
            String postId = request.getParameter("postId");
            out.print(DBHandler.writeComment(id, postId, text));
        }
        out.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
