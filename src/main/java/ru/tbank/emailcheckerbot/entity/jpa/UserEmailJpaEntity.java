package ru.tbank.emailcheckerbot.entity.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.repository.converter.TokenConverter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_emails")
@AllArgsConstructor
@NoArgsConstructor
public class UserEmailJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "mail_provider")
    @Enumerated(EnumType.STRING)
    private MailProvider mailProvider;

    @Email
    private String email;

    @NotNull
    @Column(name = "access_token")
    @Convert(converter = TokenConverter.class)
    private String accessToken;

    @NotNull
    @Column(name = "refresh_token")
    @Convert(converter = TokenConverter.class)
    private String refreshToken;

    @NotNull
    @Column(name = "access_token_ended")
    private Instant accessTokenEnded;

    @NotNull
    @Column(name = "last_message_uid")
    private Long lastMessageUID;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @NotNull
    private UserJpaEntity user;
}