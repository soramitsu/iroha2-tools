#!/usr/bin/env python3
import argparse
import json

from iroha2 import Client
from iroha2.data_model import *
from iroha2.data_model.account import Id as AccountId
from iroha2.data_model.asset import Id as AssetId
from iroha2.data_model.expression import *
from iroha2.sys.iroha_data_model import IdBox
from iroha2.sys.iroha_data_model.asset import DefinitionId
from iroha2.sys.iroha_data_model.query.account import FindAccountById
from iroha2.sys.iroha_data_model.query.asset import FindAssetById
from iroha2.sys.iroha_data_model.query.transaction import FindTransactionsByAccountId

ACCOUNT_ASSETS_OPERATION = 'account_assets'
ASSET_DETAILS_OPERATION = 'asset_details'
LATEST_TRANSACTIONS = 'last_tx'


def parse_arguments():
    parser = argparse.ArgumentParser(description='Execute queries to iroha2')
    parser.add_argument('iroha_url', type=str, help='Url of iroha node (user:pass@example.com:port)')
    subparsers = parser.add_subparsers(dest='operation')

    parser_account_assets = subparsers.add_parser(ACCOUNT_ASSETS_OPERATION, help='Find account assets by account id')
    parser_account_assets.add_argument('account_id', type=str, help='Account ID (account@domain)')

    parser_asset_details = subparsers.add_parser(ASSET_DETAILS_OPERATION, help='Find asset details by asset id')
    parser_asset_details.add_argument('account_id', type=str, help='Account ID (account@domain)')
    parser_asset_details.add_argument('asset_id', type=str, help='Asset ID (token#domain)')

    parser_latest_block = subparsers.add_parser(LATEST_TRANSACTIONS, help='Find last transactions by account id')
    parser_latest_block.add_argument('account_id', type=str, help='Account ID (account@domain)')
    parser.add_argument('--public_key', type=str,
                        help='Change default public key')
    parser.add_argument('--private_key', type=str,
                        help='Change default private key')
    return parser.parse_args()


def get_account_parts(account_id):
    return account_id.split('@')


def get_asset_parts(asset_id):
    return asset_id.split('#')


def find_account_by_id(account_id):
    return iroha_client.query(
        FindAccountById(
            Expression(
                Value(
                    IdBox(
                        AccountId(*get_account_parts(account_id))
                    )
                )
            )
        )
    )


def find_asset_by_id(account_id, asset_id):
    return iroha_client.query(
        FindAssetById(
            Expression(
                Value(
                    IdBox(
                        AssetId(
                            DefinitionId(*get_asset_parts(asset_id)),
                            AccountId(*get_account_parts(account_id))
                        )
                    )
                )
            )
        )
    )


def find_transactions_by_account_id(account_id):
    return iroha_client.query(
        FindTransactionsByAccountId(
            Expression(
                Value(
                    IdBox(
                        AccountId(*get_account_parts(account_id))
                    )
                )
            )
        )
    )


if __name__ == '__main__':
    args = parse_arguments()
    cfg = json.loads(open("./config.json").read())
    cfg['TORII_API_URL'] = args.iroha_url
    if args.public_key and args.private_key:
        cfg['PUBLIC_KEY'] = args.public_key
        cfg['PRIVATE_KEY']['payload'] = args.private_key
    iroha_client = Client(cfg)

    try:
        if args.operation == ACCOUNT_ASSETS_OPERATION:
            response = find_account_by_id(args.account_id)\
                .get('Identifiable', {}).get('Account', {}).get('assets', {})
        elif args.operation == ASSET_DETAILS_OPERATION:
            response = find_asset_by_id(args.account_id, args.asset_id)
        elif args.operation == LATEST_TRANSACTIONS:
            response = find_transactions_by_account_id(args.account_id)
        else:
            response = "Operation doesn't exists"
        print(response)
    except Exception as e:
        print("Could not execute query", e)
