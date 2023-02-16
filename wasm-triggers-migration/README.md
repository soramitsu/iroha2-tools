## WASM-TRIGGERS-MIGRATION

### Usage
You have to run app with some arguments in strict order

### Mode
0 - default (unregister + register trigger)
1 - only register trigger
2 - only unregister trigger

Example: (for modes: 0 (default) and 2(unregister))
1. http://127.0.0.1:8080 **(Peer URL)**
2. temp/files/ **(Path to directory with WASM files)**
3. alice **(Admin name)**
4. wonderland **(Domain name)**
5. 1c61faf8fe94e253b93114240394f79a607b7fa55f9e5a41ebec74b88055768b **(Public key)**
6. 282ed9f3cf92811c3818dbc4ae594ed59dc1a2f78e4241e31924e101d6b1fb83 **(Private key)**
7. 0 **(mode)**
8. username (for basic auth)
9. password (for basic auth)

Example: (for mode: 1 (register))
1. http://127.0.0.1:8080 **(Peer URL)**
2. temp/files/trigger_mint_credit.wasm **(Path to WASM file)**
3. alice **(Admin name)**
4. wonderland **(Domain name)**
5. 1c61faf8fe94e253b93114240394f79a607b7fa55f9e5a41ebec74b88055768b **(Public key)**
6. 282ed9f3cf92811c3818dbc4ae594ed59dc1a2f78e4241e31924e101d6b1fb83 **(Private key)**
7. 0 **(mode)**
8. 0 **(Repeats: 0 = Indefinitely, 1+ = Exactly number of times)**
9. 0 **(Trigger type: predefined set of trigger types. See "enum class TriggerType")**
10. bob@admin **(Technical account for trigger)**
11. 3600 ||  **("3600" interval in seconds for trigger type 0. Should be 0 for other types)**
12. username (for basic auth)
13. password (for basic auth)
