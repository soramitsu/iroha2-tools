# Iroha2 query tool

## Build 
```shell
docker build -t iroha2-query-tool .
```
Should be rebuild if script or config changed

## Run
```shell
docker run --network="host" iroha2-query-tool "user:pass@example.com:port" last_tx  genesis@genesis
```
Get list of commands and options
```shell
docker run --network="host" iroha2-query-tool -h

usage: iroha2-query-tool.py [-h] [--public_key PUBLIC_KEY]
                            [--private_key PRIVATE_KEY]
                            iroha_url
                            {account_assets,asset_details,last_tx,latest_block}
                            ...

Execute queries to iroha2

positional arguments:
  iroha_url             Url of iroha node (user:pass@example.com:port)
  {account_assets,asset_details,last_tx,latest_block}
    account_assets      Find account assets by account id
    asset_details       Find asset details by asset id
    last_tx             Find last transactions by account id
    latest_block        Find latest block by account id

optional arguments:
  -h, --help            show this help message and exit
  --public_key PUBLIC_KEY
                        Change default public key
  --private_key PRIVATE_KEY
                        Change default private key

```
