package org.littletwitter.littletwitter.servlets;


import java.sql.*;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DBHandler {
    private static String URL = "jdbc:postgresql://localhost:5080/postgres";
    private static String USERNAME = "pandu";
    private static String PASSWORD = "";

    public static JSONObject authenticate(String id, String password, HttpServletRequest request) {
        JSONObject obj = new JSONObject();

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return obj;
        }

        try {
            // Create the connection
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            String query = "SELECT count(*) FROM password WHERE id=? AND password=?;";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, id);
            preparedStmt.setString(2, password);
            ResultSet result = preparedStmt.executeQuery();
            result.next();
            boolean ans = (result.getInt(1) > 0);
            preparedStmt.close();
            conn.close();
            if (ans == true) {
                request.getSession(true).setAttribute("id", id);
                obj.put("status", true);
                obj.put("data", id);
            } else {
                obj.put("status", false);
                obj.put("message", "Authentication Failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONObject createPost(String id, String postText) {
        JSONObject obj = new JSONObject();
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            PreparedStatement pStmt = conn.prepareStatement("INSERT INTO post(uid,text,timestamp) VALUES(?,?,CURRENT_TIMESTAMP);");
            pStmt.setString(1, id);
            pStmt.setString(2, postText);
            if (pStmt.executeUpdate() > 0) {
                obj.put("status", true);
                obj.put("data", "Created Post");
            } else {
                obj.put("status", false);
                obj.put("message", "Unable to create");
            }
        } catch (Exception sqle) {
            sqle.printStackTrace();
        }
        return obj;
    }

    public static JSONObject writeComment(String id, String PostId, String comment) {
        JSONObject obj = new JSONObject();
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            PreparedStatement pStmt = conn.prepareStatement("INSERT INTO comment(postid,uid,timestamp,text) VALUES(?,?,CURRENT_TIMESTAMP,?);");
            pStmt.setInt(1, Integer.parseInt(PostId));
            pStmt.setString(2, id);
            pStmt.setString(3, comment);
            if (pStmt.executeUpdate() > 0) {
                obj.put("status", true);
                obj.put("data", "Created Post Successfully");
            } else {
                obj.put("status", false);
                obj.put("message", "Could not Post");
            }
        } catch (Exception sqle) {
            sqle.printStackTrace();
        }
        return obj;
    }

    public static JSONArray userFollow(String id) {

        JSONArray jsonObj = new JSONArray();
        try {
            // Create the connection
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            String query = "SELECT uid2 AS uid, name FROM follows, \"user\" WHERE \"user\".uid "
                    + "= uid2 AND uid1 = ?";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, id);
            ResultSet result = preparedStmt.executeQuery();

            jsonObj = resultSetConverter(result);
            preparedStmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    public static JSONObject deAuthenticate(HttpServletRequest request) throws JSONException {
        JSONObject obj = new JSONObject();
        if (request.getSession(false) == null) {
            obj.put("status", false);
            obj.put("message", "Invalid Session");
            return obj;
        } else {
            request.getSession(false).invalidate();
            obj.put("status", true);
            obj.put("data", "sucessfully logged out");
            return obj;
        }
    }

    public static JSONArray seeMyPosts(String id, int offset, int limit) {
        JSONArray json = new JSONArray();
        try (
                Connection conn = DriverManager.getConnection(
                        URL, USERNAME, "");
                PreparedStatement postSt = conn.prepareStatement("SELECT postid,timestamp,uid,text FROM post WHERE post.uid = ? ORDER BY timestamp DESC OFFSET ? LIMIT ?");
        ) {
            postSt.setString(1, id);
            postSt.setInt(2, offset);
            postSt.setInt(3, limit);
            ResultSet rs = postSt.executeQuery();
            conn.close();
            json = resultSetConverter(rs);
            return json;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json;

    }

    public static JSONArray seeUserPosts(String id, int offset, int limit) {
        JSONArray json = new JSONArray();
        try (
                Connection conn = DriverManager.getConnection(
                        URL, USERNAME, "");
                PreparedStatement postSt = conn.prepareStatement("SELECT postid,timestamp,uid,text FROM post WHERE post.uid = ? ORDER BY timestamp DESC OFFSET ? LIMIT ?");
        ) {
            postSt.setString(1, id);
            postSt.setInt(2, offset);
            postSt.setInt(3, limit);
            ResultSet rs = postSt.executeQuery();
            conn.close();
            json = resultSetConverter(rs);
            return json;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json;

    }

    public static JSONArray seePosts(String id, int offset, int limit) {
        JSONArray json = new JSONArray();
        try (
                Connection conn = DriverManager.getConnection(
                        URL, USERNAME, "");
                PreparedStatement postSt = conn.prepareStatement("SELECT postid,timestamp,uid,text FROM post WHERE post.uid IN (SELECT uid2 FROM follows WHERE uid1 = ? UNION SELECT uid FROM \"user\" WHERE uid=? ) ORDER BY timestamp ASC OFFSET ? LIMIT ?");
        ) {
            postSt.setString(1, id);
            postSt.setString(2, id);
            postSt.setInt(3, offset);
            postSt.setInt(4, limit);
            ResultSet rs = postSt.executeQuery();
            json = resultSetConverter(rs);
            return json;
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static JSONArray resultSetConverter(ResultSet rs) throws SQLException, JSONException {
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            int postid = -1;
            for (int i = 1; i < numColumns + 1; i++) {
                String column_name = rsmd.getColumnName(i);

                if (rsmd.getColumnType(i) == Types.ARRAY) {
                    obj.put(column_name, rs.getArray(column_name));
                } else if (rsmd.getColumnType(i) == Types.BIGINT) {
                    obj.put(column_name, rs.getInt(column_name));
                } else if (rsmd.getColumnType(i) == Types.BOOLEAN) {
                    obj.put(column_name, rs.getBoolean(column_name));
                } else if (rsmd.getColumnType(i) == Types.BLOB) {
                    obj.put(column_name, rs.getBlob(column_name));
                } else if (rsmd.getColumnType(i) == Types.DOUBLE) {
                    obj.put(column_name, rs.getDouble(column_name));
                } else if (rsmd.getColumnType(i) == Types.FLOAT) {
                    obj.put(column_name, rs.getFloat(column_name));
                } else if (rsmd.getColumnType(i) == Types.INTEGER) {
                    obj.put(column_name, rs.getInt(column_name));
                } else if (rsmd.getColumnType(i) == Types.NVARCHAR) {
                    obj.put(column_name, rs.getNString(column_name));
                } else if (rsmd.getColumnType(i) == Types.VARCHAR) {
                    obj.put(column_name, rs.getString(column_name));
                } else if (rsmd.getColumnType(i) == Types.TINYINT) {
                    obj.put(column_name, rs.getInt(column_name));
                } else if (rsmd.getColumnType(i) == Types.SMALLINT) {
                    obj.put(column_name, rs.getInt(column_name));
                } else if (rsmd.getColumnType(i) == Types.DATE) {
                    obj.put(column_name, rs.getDate(column_name));
                } else if (rsmd.getColumnType(i) == Types.TIMESTAMP) {
                    obj.put(column_name, rs.getTimestamp(column_name));
                } else {
                    obj.put(column_name, rs.getObject(column_name));
                }
                if (column_name.equals((String) "postid")) {
                    postid = rs.getInt(column_name);
                }
            }
            json.put(obj);
            if (postid != -1) {
                JSONArray comObj = getComments(postid);
                obj.put("Comment", comObj);
            }
        }
        return json;
    }

    public static JSONArray getComments(int postid) {
        JSONArray json = new JSONArray();
        try (
                Connection conn = DriverManager.getConnection(
                        URL, USERNAME, "");
                PreparedStatement commSt = conn.prepareStatement("SELECT timestamp,comment.uid, name, text FROM comment,\"user\" AS us WHERE postid = ? AND us.uid=comment.uid ORDER BY timestamp ASC")

        ) {
            commSt.setInt(1, postid);
            ResultSet rs = commSt.executeQuery();
            json = resultSetConverter(rs);
            return json;
        } catch (SQLException | JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json;

    }

    public static JSONObject follow(String uid1, String uid2) throws JSONException {
        JSONObject obj = new JSONObject();
        try (
                Connection conn = DriverManager.getConnection(
                        URL, USERNAME, "");
                PreparedStatement commSt = conn.prepareStatement("INSERT INTO follows VALUES(?,?)");

        ) {
            commSt.setString(1, uid1);
            commSt.setString(2, uid2);
            if (commSt.executeUpdate() > 0) {
                obj.put("status", true);
                obj.put("data", "user followed " + uid2);
            } else {
                obj.put("status", false);
                obj.put("message", "could not follow");
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            obj.put("status", false);
            obj.put("message", "Already followed");
        }
        return obj;
    }

    public static JSONObject unFollow(String uid1, String uid2) throws JSONException {
        JSONObject obj = new JSONObject();
        try (
                Connection conn = DriverManager.getConnection(
                        URL, USERNAME, "");
                PreparedStatement check = conn.prepareStatement("SELECT * FROM follows WHERE uid1=? AND uid2=?");

        ) {
            check.setString(1, uid1);
            check.setString(2, uid2);
            ResultSet result = check.executeQuery();
            if (result.next()) {
                PreparedStatement commSt = conn.prepareStatement("DELETE FROM follows WHERE uid1=? AND uid2=?");
                commSt.setString(1, uid1);
                commSt.setString(2, uid2);
                if (commSt.executeUpdate() > 0) {
                    obj.put("status", true);
                    obj.put("data", "unfollowed " + uid2);
                } else {
                    obj.put("status", false);
                    obj.put("message", "could not unFollow");

                }
            } else {
                obj.put("status", false);
                obj.put("message", "user not followed");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONArray getSuggestion(String search) {
        JSONArray jsonToSend = new JSONArray();
        if (search.length() < 3)
            return jsonToSend;
        try (
                Connection conn = DriverManager.getConnection(
                        URL, USERNAME, "");
                PreparedStatement commSt = conn.prepareStatement("SELECT name,uid,email FROM \"user\" WHERE name LIKE ? OR uid LIKE ? OR email LIKE ? LIMIT 10");
        ) {


            search = "%" + search + "%";
            commSt.setString(1, search);
            commSt.setString(2, search);
            commSt.setString(3, search);
            ResultSet rset = commSt.executeQuery();
            jsonToSend.put(resultSetConverter(rset));
            return jsonToSend;
        } catch (SQLException | JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonToSend;
    }

}
