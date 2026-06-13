package com.domoticore.iam.domain.model.commands;

import com.domoticore.iam.domain.model.valueobjects.Email;

public record LoginUserCommand(Email email, String rawPassword) {
}
