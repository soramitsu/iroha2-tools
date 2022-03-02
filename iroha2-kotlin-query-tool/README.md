# Iroha 2 query tool

### Building docker image
Enter to iroha2-kotlin-query-tool directory

```
gradle build
docker build -t iroha2-query .
```

To query Iroha2 peer with basic authentication run the following command (username, password, peer_url, 
    admin_account, admin_private_key_hex, admin_public_key_hex):
```
docker run iroha2-query 
       "USERNAME"
       "PASSWORD"
       "https://peer_address.co.jp/"
       "admin@domain"
       "aaaaaaaaaaaaaabbbbbbbbbbbbccccccccccccccdddddddddddddeeeeeeeeeee"  
       "aaaaaaaaaaaaaabbbbbbbbbbbbccccccccccccccdddddddddddddeeeeeeeeeee"
```
Peer domains information in json format will be printed.
