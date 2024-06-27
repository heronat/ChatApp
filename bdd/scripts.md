# Commands to manage the database containers

Containers of the database are managed by docker-compose. The database is a PostgreSQL database.

We've created a Dockerfile to build the image of the database. The image is based on the official PostgreSQL image. Docker will import the file backup.sql to the database when the container is created, then the database will be ready to use.


## Process :

- If you want to launch the containers without importing the database, you can use the command below.
- If you want to export the database, you can use the command below. The file backup.sql will be created in the root of this folder.
- If you want to remove the containers and the volumes (so remove all the data), you can use the command below.
- If you want to launch the containers with importing new fresh database, we suggest :
  - First, you remove the containers and the volumes.
  - Then, you build the image and launch the containers.

## Commands 

Launch the containers without importing the database :

```bash
docker-compose up -d
```

Export the database :
```bash
docker exec sr03-chatapp-postgres pg_dump -U sr03 -d sr03_postgres > backup.sql
```

Remove the containers and the volumes :
```bash
docker-compose down -v
```

Build the image and launch the containers :
```bash
docker-compose build && docker-compose up -d
```

⚠️ Execute a shell in the database container 💀 :
```bash
docker exec -it sr03-chatapp-postgres /bin/bash
```
