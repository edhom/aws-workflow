FROM java:8
COPY ./calc_fib.jar /usr
WORKDIR /usr
ENTRYPOINT ["sh","-c", "java -jar calc_fib.jar /src/input_full.csv && mv output.csv /src/output.csv"]
