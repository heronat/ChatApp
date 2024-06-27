FROM ubuntu:latest
LABEL authors="rayandoudech"
LABEL version="1.0.0"
LABEL description="Docker image for the client JS for Chat APP, built for SR03 Project P24"

FROM node:20-slim AS base

ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN corepack enable

FROM base AS prod-deps

WORKDIR /app
COPY package.json pnpm-lock.yaml* ./

RUN \
  if [ -f pnpm-lock.yaml ]; then echo "Lockfile was found, installing deps ..."; \
  else echo "Lockfile not found." && exit 1; \
  fi

RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install

FROM base AS runner

WORKDIR /app

COPY . .
COPY --from=prod-deps /app/node_modules ./node_modules

CMD ["pnpm", "run", "dev"]