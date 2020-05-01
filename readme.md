[protocol](https://github.com/mikenoethiger/bank-server-socket#protocol) | [bank-client](https://github.com/mikenoethiger/bank-client) | [bank-server-socket](https://github.com/mikenoethiger/bank-server-socket) | [bank-server-graphql](https://github.com/mikenoethiger/bank-server-graphql) | [bank-server-rabbitmq](https://github.com/mikenoethiger/bank-server-rabbitmq)

# About

This project is part of a module exercise in distributed systems (Verteilte Systeme, vesys) at [FHNW](https://github.com/FHNW) (lecturer [Dominik Gruntz](https://github.com/dgruntz)).

A minimal banking application is implemented using various technology. This project contains the client side implementations for each technology.

Server side implementations can be found in the respective repos:
 
* [bank-server-socket](https://github.com/mikenoethiger/bank-server-socket)
* [bank-server-graphql](https://github.com/mikenoethiger/bank-server-graphql)
* [bank-server-rabbitmq](https://github.com/mikenoethiger/bank-server-rabbitmq)

Most implementations rely on the [text protocol](https://github.com/mikenoethiger/bank-server-socket#protocol) defined in the socket implementation and only vary in transportation.

# Run

Launch `bank.BankLauncher` as Java Application and pick backend you wish to connect to in the launcher.

Or with gradle:

```bash
$ gradle run
```

> **Notice:** You need to run the backend yourself in order to connect.
> See the repo of the corresponding server implementation for running instructions.
> It's mostly as trivial as `gradle run`.

> **Notice 2:** You can change/add connection credentials shown in the backend selection window. Just edit `src/main/resources/Servers.txt` (e.g. if you host the backend somewhere else than localhost.)