# Iroha 2 stuff signer

### Building docker image
Enter to `simple-signer` directory

```
gradle build
docker build -t iroha2-text-signer:1.0 .
```
### Using script
To sign some text, run the following command (public_key, private_key, text to sign (as Hex)):
```
docker run iroha2-text-signer:1.0 
       "35f192dedab31688a7c49758ca49ff296909829a686ed98e3b7a9842b9ed5bd7"  
       "2553484d7d00b742577200215c42a71ed3ea89e1033d4dcdbc239ccc9be8b78f" 
       "abcd"
```
Signed result in Hex format will be printed.
