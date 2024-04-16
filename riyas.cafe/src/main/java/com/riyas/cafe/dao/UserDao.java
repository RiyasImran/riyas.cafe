package com.riyas.cafe.dao;

import com.riyas.cafe.models.User;
import com.riyas.cafe.wrapper.UserWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import javax.transaction.Transactional;
import java.util.List;

public interface UserDao extends JpaRepository<User, Integer> {
    User findByEmail(String email);

    @Query("select new com.riyas.cafe.wrapper.UserWrapper(u.id, u.name, u.email, u.contactNumber, u.status) "+
            "from User u where u.role ='user'")
    List<UserWrapper> getAllUsers();

    @Query("select u.email from User u where u.role ='admin'")
    List<String> getAllAdminsEmail();

    @Transactional
    @Modifying
    @Query("update User u set u.status=:status where u.id=:id ")
    Integer updateStaus(@Param("status") String status, @Param("id") Integer id);
}
