package com.domoticore.iam.domain.model.commands;

import com.domoticore.iam.domain.model.valueobjects.Email;

public record RegisterUserCommand(String name, Email email, String rawPassword) {
}
