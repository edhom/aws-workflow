FROM java:8
COPY ./calc_fib.jar /usr
WORKDIR /usr
ENTRYPOINT ["java", "-jar", "calc_fib.jar"]
