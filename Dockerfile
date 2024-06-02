FROM amazoncorretto:17.0.9 AS build

WORKDIR /work
COPY . /work

RUN chmod +x gradlew && ./gradlew --no-daemon installDist

FROM amazoncorretto:17.0.9

COPY --from=build /work/build/install/market/ /app/market/

ENTRYPOINT ["/app/market/bin/market"]
