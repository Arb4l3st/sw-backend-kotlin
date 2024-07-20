# What would I do next:

- [ ] Change enum values to english

While this app remains small, I think it's ok to make service objects, but in future I would:

- [ ] Separate services to services + data sources
- [ ] Change objects to classes with interfaces, add DI (for example - Koin)

Also, I would make some research about suspend functions and Exposed. Now I don't like realisation
of nested suspended transaction. For instance, if I have API function with newSuspendedTransaction under
the hood and I call it from another newSuspendedTransaction, rollback will not be done as expected.
