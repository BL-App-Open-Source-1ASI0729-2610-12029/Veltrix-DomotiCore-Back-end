package com.domoticore.shared.domain.model;

public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    record Success<T, E>(T value) implements Result<T, E> {
    }

    record Failure<T, E>(E error) implements Result<T, E> {
    }

    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success;
    }

    default boolean isFailure() {
        return this instanceof Failure;
    }

    default <R> R fold(java.util.function.Function<T, R> onSuccess, java.util.function.Function<E, R> onFailure) {
        if (this instanceof Success<T, E> success) {
            return onSuccess.apply(success.value());
        }
        return onFailure.apply(((Failure<T, E>) this).error());
    }
}
