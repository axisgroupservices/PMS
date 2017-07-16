mvn clean install

docker build -f Dockerfile -t paypal-payouts .

docker run --rm -p 8080:8080 paypal-payouts
