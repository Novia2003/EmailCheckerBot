package ru.tbank.emailcheckerbot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String token;

    @NotNull
    @Column(name = "last_message_uid")
    private Long lastMessageUID;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @NotNull
    private UserEntity user;
}
