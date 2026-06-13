package com.domoticore.iam.application.commandservices;

public sealed interface UserCommandFailure permits UserCommandFailure.Duplicate, UserCommandFailure.InvalidCredentials, UserCommandFailure.NotFound {

    String messageKey();

    record Duplicate() implements UserCommandFailure {
        @Override
        public String messageKey() {
            return "iam.user.error.duplicate";
        }
    }

    record InvalidCredentials() implements UserCommandFailure {
        @Override
        public String messageKey() {
            return "iam.user.error.invalidCredentials";
        }
    }

    record NotFound() implements UserCommandFailure {
        @Override
        public String messageKey() {
            return "iam.user.error.notFound";
        }
    }
}
