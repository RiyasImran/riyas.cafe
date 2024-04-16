package com.riyas.cafe.models;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import javax.persistence.*;
import java.io.Serializable;

//@NamedQuery(name = "User.getAllUsers", query = "select new com.riyas.cafe.wrapper.UserWrapper(u.id,u.name,u.email,u.contactNumber,u,status) from User u where u.role='user'")

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "contactNumber")
    private String contactNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    private String status;

    @Column(name = "role")
    private String role;
}
