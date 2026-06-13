# DomotiCore User Stories (Technical)

## IAM

- As a frontend user, I can register with name, email and password and receive a JWT.
- As a frontend user, I can login with email and password and receive a JWT.
- As an authenticated user, I can read and update my own profile by user id.

## JSON-backed dashboard resources

- As a dashboard client, I can list, read, create, patch and delete demo resources exposed under `/api/v1/{resource}`.
- As an authenticated SME user, I can read and patch user-scoped resources such as business profile, team management and zone configuration.

## Error handling

- Validation errors return HTTP 400 with code `VALIDATION_ERROR`.
- Duplicate email on register returns HTTP 409 with code `CONFLICT`.
- Missing resources return HTTP 404 with code `NOT_FOUND`.
