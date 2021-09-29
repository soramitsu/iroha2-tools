# Iroha2 query tool

## Build 
```shell
docker build -t iroha2-query-tool .
```
Rebuild if script or config changed.
For mac os: add --platform=linux/amd64 parameter after `build`

## Run
```shell
docker run --network="host" iroha2-query-tool "https://user:pass@example.com" last_tx  genesis@genesis
```
Get list of commands and options
```shell
docker run --network="host" iroha2-query-tool -h

usage: iroha2-query-tool.py [-h] [--public_key PUBLIC_KEY] [--private_key PRIVATE_KEY] iroha_url {account_assets,asset_details,all_tx,last_tx} ...

Execute queries to iroha2

positional arguments:
  iroha_url             Url of iroha node (user:pass@example.com:port)
  {account_assets,asset_details,all_tx,last_tx}
    account_assets      Find account assets by account id
    asset_details       Find asset details by asset id
    all_tx              Find transactions by account id
    last_tx             Find latest transaction by account id

optional arguments:
  -h, --help            show this help message and exit
  --public_key PUBLIC_KEY
                        Change default public key
  --private_key PRIVATE_KEY
                        Change default private key


```
