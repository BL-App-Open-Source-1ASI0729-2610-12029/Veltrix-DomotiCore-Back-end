package com.domoticore.iam.domain.model.aggregates;

import com.domoticore.iam.domain.model.commands.RegisterUserCommand;
import com.domoticore.iam.domain.model.commands.UpdateUserCommand;
import com.domoticore.iam.domain.model.valueobjects.AccountType;
import com.domoticore.iam.domain.model.valueobjects.Email;
import com.domoticore.iam.infrastructure.persistence.jpa.converters.EmailAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email")
)
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Convert(converter = EmailAttributeConverter.class)
    @Column(name = "email", nullable = false, unique = true)
    private Email emailAddress;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String role;

    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    private AccountType accountType;

    @Column(name = "onboarding_completed")
    private Boolean onboardingCompleted = false;

    protected User() {
    }

    public static User newEmpty() {
        return new User();
    }

    public User(RegisterUserCommand command, String encodedPassword) {
        this.name = command.name();
        this.emailAddress = command.email();
        this.passwordHash = encodedPassword;
        this.role = "User";
        this.onboardingCompleted = false;
        this.avatar = "https://ui-avatars.com/api/?name="
                + command.name().replace(" ", "+")
                + "&background=3455d1&color=ffffff";
    }

    public void apply(UpdateUserCommand command) {
        if (command.name() != null) {
            this.name = command.name();
        }
        if (command.email() != null) {
            this.emailAddress = new Email(command.email());
        }
        if (command.role() != null) {
            this.role = command.role();
        }
        if (command.avatar() != null) {
            this.avatar = command.avatar();
        }
        if (command.accountType() != null) {
            this.accountType = AccountType.fromJson(command.accountType());
        }
        if (command.onboardingCompleted() != null) {
            this.onboardingCompleted = command.onboardingCompleted();
        }
    }

    public String getEmail() {
        return emailAddress != null ? emailAddress.value() : null;
    }

    public void setEmail(String email) {
        this.emailAddress = email != null ? new Email(email) : null;
    }
}
