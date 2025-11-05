package com.scm.scm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {
    private static final int EXPIRATION_MINUTES = 60; //Token valido por una hora

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = Usuario.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    //{}
    public PasswordResetToken(String token, Usuario usuario) {
        this.token=token;
        this.usuario=usuario;
        this.expiryDate=calculateExpiryDate();
    }

    public LocalDateTime calculateExpiryDate(){
        return  LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

    }


    public boolean isExpired(){
        return  LocalDateTime.now().isAfter(expiryDate);
    }




}
