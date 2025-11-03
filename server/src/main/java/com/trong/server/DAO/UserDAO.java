package com.trong.server.DAO;

import com.trong.model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends DAO {

    public User checkLogin(User user) {
        String sql = "SELECT * FROM tblUser WHERE username = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User foundUser = new User();
                foundUser.setIdUser(rs.getInt("idUser"));
                foundUser.setUsername(rs.getString("username"));
                foundUser.setPassword(rs.getString("password"));
                foundUser.setFullname(rs.getString("fullname"));
                foundUser.setEmail(rs.getString("email"));
                foundUser.setTel(rs.getString("tel"));
                foundUser.setRole(rs.getString("role"));
                return foundUser;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
