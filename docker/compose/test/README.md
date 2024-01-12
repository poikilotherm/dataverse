# Compose Persona for Tests

With this persona, you can start Dataverse, its dependencies as well as other "external" services that you might want to run end-to-end or integration tests for.

To avoid starting unnecessary services when focusing testing on specific tech, there are different profiles available:

- `all`: start **all** external services
- `s3`: start S3 related services

In `compose.override.yml` you can adapt configuration of the core services (Dataverse, Postgres, Solr, SMTP).
Please note that you cannot override/merge settings selectively per profile, which is a limitation of Docker Compose.