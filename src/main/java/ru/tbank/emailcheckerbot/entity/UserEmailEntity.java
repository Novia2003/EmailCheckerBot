package ru.tbank.emailcheckerbot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_emails")
@AllArgsConstructor
@NoArgsConstructor
public class UserEmailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "email_provider")
    private String emailProvider;

    @Email
    private String email;

    @NotNull
    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "end_access_token_life")
    private Instant endAccessTokenLife;

    @NotNull
    @Column(name = "last_message_uid")
    private Long lastMessageUID;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @NotNull
    private UserEntity user;
}
