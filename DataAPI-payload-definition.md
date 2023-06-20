## Admin API json payload definition
Payload expected by the admin API to import or update user bank accounts data.

- [See: How to create test facility bank users and data](https://github.com/SecureApiGateway/SecureApiGateway/wiki/How-to-create-test-facility-bank-users-and-data)

**SAPI-G release versions supported**: 

| SAPI-G | Open Banking API  |
|--------|-------------------|
| v1.0   | v3.1.4 to v3.1.10 |

The user bank accounts data must be provided as json payload, one or more per user.
- The `userName` must not contains spaces
- The json payload with user data should be no larger than ~20k lines: When the payload that contains the user accounts data is larger than ~20k they should be split on the `accountDatas` array.

```json
{
  "userName": "customerUserName",
  "party": {},
  "accountDatas": [
    {
      "account": {},
      "party": {},
      "balances": [],
      "product": {},
      "beneficiaries": [],
      "directDebits": [],
      "standingOrders": [],
      "transactions": [],
      "statements": [],
      "scheduledPayments": [],
      "offers": []
    }
  ]
}
```
### Dictionary

| Root dictionary                    | Field          | References                                                                                                                                                                                                                                                             |
|------------------------------------|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| String (no spaces)                 | `userName`     | N/A                                                                                                                                                                                                                                                                    | 
| OBParty2                           | `party`        | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Parties.html#uml-diagram) [dictionary](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Parties.html#data-dictionary) |
| Array of `account Data dictionary` | `accountDatas` | N/A                                                                                                                                                                                                                                                                    |

| account Data dictionary | Field               | References                                                                                                                                                                                                                                                                                   |
|-------------------------|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| OBAccount6              | `account`           | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Accounts.html#uml-diagram) [dictionary](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Accounts.html#data-dictionary)                     | 
| OBParty2                | `party`             | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Parties.html#uml-diagram) [dictionary](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Parties.html#data-dictionary)                       |
| OBCashBalance1          | `balances`          | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Balances.html#uml-diagram) [dictionary](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Balances.html#data-dictionary)                     | 
| OBReadDataProduct2      | `product`           | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Products.html#uml-diagram) [dictionary](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Products.html#data-dictionary)                     |
| OBBeneficiary5          | `beneficiaries`     | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Beneficiaries.html#uml-diagram) [dictionary](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Beneficiaries.html#data-dictionary)           |
| OBReadDataDirectDebit2  | `directDebits`      | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/direct-debits.html#uml-diagram) [dictionary](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/direct-debits.html#data-dictionary)           |
| OBStandingOrder5        | `standingOrders`    | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/standing-orders.html#uml-diagram) [dictionary](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/standing-orders.html#data-dictionary)       |
| OBTransaction6          | `transactions`      | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Transactions.html#uml-diagram) [dictionary](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Transactions.html#data-dictionary)             |
| OBStatement2            | `statements`        | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Statements.html#uml-diagram) [dictionary](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Statements.html#data-dictionary)                 |
| OBScheduledPayment3     | `scheduledPayments` | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/scheduled-payments.html#uml-diagram) [dictionary](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/scheduled-payments.html#data-dictionary) |
| OBOffer1                | `offers`            | [Diagram](https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Offers.html#uml-diagram) [dictionary]( https://openbankinguk.github.io/read-write-api-site3/v3.1.10/resources-and-data-models/aisp/Offers.html#data-dictionary)                        |


### Payload example for username `test`
```json
{
  "userName": "test",
  "party": {
    "Name": "0123456789"
  },
  "accountDatas": [
    {
      "account": {
        "Currency": "GBP",
        "Nickname": "NICKNAME13",
        "AccountType": "Business",
        "AccountSubType": "CurrentAccount",
        "OpeningDate": "2015-06-22T00:00:00.000Z",
        "Account": [
          {
            "SchemeName": "UK.OBIE.SortCodeAccountNumber",
            "Identification": "010203123456789",
            "Name": "General Financial"
          }
        ]
      },
      "party": {
        "PartyId": "0123456789",
        "Name": "0123456789"
      },
      "transactions": [
        {
          "TransactionId": "Ta36790ac991b5cd8581875523c068d5d",
          "Amount": {
            "Amount": "11.00",
            "Currency": "GBP"
          },
          "CreditDebitIndicator": "Debit",
          "Status": "Booked",
          "BookingDateTime": "2023-01-08T00:00:00+00:00",
          "ValueDateTime": "2023-01-08T00:00:00+00:00",
          "BankTransactionCode": {
            "Code": "IssuedCreditTransfers",
            "SubCode": "Charges"
          },
          "ProprietaryBankTransactionCode": {
            "Code": "PAY",
            "Issuer": "LBG"
          },
          "Balance": {
            "Amount": {
              "Amount": "828.00",
              "Currency": "GBP"
            },
            "CreditDebitIndicator": "Credit",
            "Type": "InterimBooked"
          },
          "TransactionInformation": "DEB"
        }
      ],
      "balances": [
        {
          "Amount": {
            "Amount": "828.00",
            "Currency": "GBP"
          },
          "CreditDebitIndicator": "Credit",
          "Type": "InterimBooked",
          "DateTime": "2023-05-15T11:57:04Z"
        }
      ],
      "beneficiaries": [
        {
          "Reference": "BEN REF",
          "CreditorAccount": {
            "SchemeName": "UK.OBIE.SortCodeAccountNumber",
            "Identification": "010203123456789",
            "Name": "BEN REF"
          }
        }
      ],
      "directDebits": [
        {
          "MandateIdentification": "dir deb identification",
          "DirectDebitStatusCode": "Active",
          "Name": "REASSURE",
          "PreviousPaymentAmount": {
            "Amount": "0.00",
            "Currency": "GBP"
          },
          "Frequency": "UK.OBIE.NotKnown"
        }
      ],
      "standingOrders": [
        {
          "Frequency": "IntrvlMnthDay:01:16",
          "Reference": "STAND reference",
          "FirstPaymentDateTime": "2023-07-16T00:00:00+00:00",
          "FirstPaymentAmount": {
            "Amount": "55.00",
            "Currency": "GBP"
          },
          "NextPaymentDateTime": "2023-08-16T00:00:00+00:00",
          "NextPaymentAmount": {
            "Amount": "55.00",
            "Currency": "GBP"
          },
          "FinalPaymentAmount": {
            "Amount": "55.00",
            "Currency": "GBP"
          },
          "StandingOrderStatusCode": "Active",
          "CreditorAccount": {
            "SchemeName": "UK.OBIE.SortCodeAccountNumber",
            "Identification": "010203123456789",
            "Name": "CredName"
          }
        }
      ],
      "scheduledPayments": [],
      "offers": [],
      "statements": [],
      "parties": [
        {
          "PartyId": "123456789",
          "PartyType": "Delegate",
          "FullLegalName": "General Financial",
          "LegalStructure": "UK.OBIE.Sole"
        }
      ]
    }
  ]
}
```
> To save the example payload in a file, we recommend use the file name pattern `${userName}.json`, example: `test.json`

> When the user bank accounts data is split it up in few files, we recommend use the file name pattern `${userName}-${1..n}.json` (ex. `test-1.json`, `test-2.json`...`test-n.json`)