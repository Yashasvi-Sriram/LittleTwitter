package org.littletwitter.littletwitter.servlets;

import java.sql.*;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBHandler {
    private static String URL = "jdbc:postgresql://localhost:5940/postgres";
    private static String USERNAME = "joshi";
    private static String PASSWORD = "";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject authenticate(String userId, String password, HttpServletRequest request) {
        JSONObject obj = new JSONObject();
        String query = "SELECT count(*) FROM password WHERE id=? AND password=?;";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement preparedStmt = conn.prepareStatement(query)) {
            preparedStmt.setString(1, userId);
            preparedStmt.setString(2, password);
            ResultSet result = preparedStmt.executeQuery();
            result.next();
            boolean ans = (result.getInt(1) > 0);
            if (ans) {
                request.getSession(true).setAttribute("userId", userId);
                obj.put("status", true);
                obj.put("data", userId);
            } else {
                obj.put("status", false);
                obj.put("message", "Authentication Failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONObject createPost(String id, String postText, String base64Image) {
        JSONObject obj = new JSONObject();
        String query = "INSERT INTO post(uid, text, image, timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP);";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pStmt = conn.prepareStatement(query)) {
            pStmt.setString(1, id);
            pStmt.setString(2, postText);
            pStmt.setString(3, base64Image);
            if (pStmt.executeUpdate() > 0) {
                obj.put("status", true);
                obj.put("data", "Created Post");
            } else {
                obj.put("status", false);
                obj.put("message", "Unable to create");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONObject writeComment(String id, String postId, String text) {
        JSONObject obj = new JSONObject();
        String query = "INSERT INTO comment(postid,uid,timestamp,text) VALUES(?,?,CURRENT_TIMESTAMP,?);";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pStmt = conn.prepareStatement(query)) {
            pStmt.setInt(1, Integer.parseInt(postId));
            pStmt.setString(2, id);
            pStmt.setString(3, text);
            if (pStmt.executeUpdate() > 0) {
                obj.put("status", true);
                obj.put("data", "Created comment successfully");
            } else {
                obj.put("status", false);
                obj.put("message", "Unable to create");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONArray userFollow(String id) {
        JSONArray json = new JSONArray();
        String query = "SELECT uid2 AS uid, \"user\".name FROM follows, \"user\" WHERE \"user\".uid = uid2 AND uid1 = ?";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement preparedStmt = conn.prepareStatement(query)) {
            preparedStmt.setString(1, id);
            ResultSet result = preparedStmt.executeQuery();
            json = resultSetConverter(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONArray seeMyPosts(String id, int offset, int limit) {
        return seeUserPosts(id, offset, limit);
    }

    public static JSONArray seeUserPosts(String id, int offset, int limit) {
        JSONArray json = new JSONArray();
        String query = "SELECT postid, timestamp, uid, text, image FROM post WHERE post.uid = ? ORDER BY timestamp DESC OFFSET ? LIMIT ?";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement postSt = conn.prepareStatement(query)) {
            postSt.setString(1, id);
            postSt.setInt(2, offset);
            postSt.setInt(3, limit);
            ResultSet rs = postSt.executeQuery();
            json = resultSetConverter(rs);
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONArray seePosts(String id, int offset, int limit) {
        JSONArray json = new JSONArray();
        String query = "SELECT postid, timestamp, uid, text, image FROM post WHERE post.uid IN (SELECT uid2 FROM follows WHERE uid1 = ? UNION SELECT uid FROM \"user\" WHERE uid=? ) ORDER BY timestamp ASC OFFSET ? LIMIT ?";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement postSt = conn.prepareStatement(query)) {
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

    public static JSONArray getComments(int postId) {
        JSONArray json = new JSONArray();
        String query = "SELECT timestamp,comment.uid, us.name, text FROM comment,\"user\" AS us WHERE postid = ? AND us.uid=comment.uid ORDER BY timestamp ASC";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement commSt = conn.prepareStatement(query)) {
            commSt.setInt(1, postId);
            ResultSet rs = commSt.executeQuery();
            json = resultSetConverter(rs);
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject follow(String uid1, String uid2) {
        JSONObject obj = new JSONObject();
        String query = "INSERT INTO follows VALUES(?,?)";
        try {
            try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                 PreparedStatement commSt = conn.prepareStatement(query)) {
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
                obj.put("status", false);
                obj.put("message", "Already followed");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONObject unFollow(String uid1, String uid2) {
        JSONObject obj = new JSONObject();
        String query = "SELECT * FROM follows WHERE uid1=? AND uid2=?";
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement check = conn.prepareStatement(query)) {
            check.setString(1, uid1);
            check.setString(2, uid2);
            ResultSet result = check.executeQuery();
            if (result.next()) {
                String query2 = "DELETE FROM follows WHERE uid1=? AND uid2=?";
                PreparedStatement commSt = conn.prepareStatement(query2);
                commSt.setString(1, uid1);
                commSt.setString(2, uid2);
                if (commSt.executeUpdate() > 0) {
                    obj.put("status", true);
                    obj.put("data", "unFollowed " + uid2);
                } else {
                    obj.put("status", false);
                    obj.put("message", "could not unFollow");
                }
            } else {
                obj.put("status", false);
                obj.put("message", "user not followed");
            }
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONArray getSuggestion(String search) {
        JSONArray jsonArray = new JSONArray();
        String query = "SELECT \"user\".name,\"user\".uid,\"user\".email FROM \"user\" WHERE \"user\".name LIKE ? OR \"user\".uid LIKE ? OR \"user\".email LIKE ? LIMIT 10";
        if (search.length() < 3) {
            return jsonArray;
        }
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement commSt = conn.prepareStatement(query)) {
            search = "%" + search + "%";
            commSt.setString(1, search);
            commSt.setString(2, search);
            commSt.setString(3, search);
            ResultSet rset = commSt.executeQuery();
            jsonArray = resultSetConverter(rset);
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    private static JSONArray resultSetConverter(ResultSet rs) throws SQLException, JSONException {
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            int postId = -1;
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
                    postId = rs.getInt(column_name);
                }
            }
            json.put(obj);
            if (postId != -1) {
                JSONArray comObj = getComments(postId);
                obj.put("Comment", comObj);
            }
        }
        return json;
    }
}