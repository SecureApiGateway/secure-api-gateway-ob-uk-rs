# Data Events
An admin api to import, export, update and delete event messages by api client for test facility bank

All events operations are filtered by api client, so that each api client only can manage his owned events.

| operation | endpoint                                                        | description                                            |
|-----------|-----------------------------------------------------------------|--------------------------------------------------------|
| POST      | /rs/admin/data/events                                           | import event messages                                  |
| PUT       | /rs/admin/data/events?apiClientId={{api client id}}             | update event messages for a tpp (api client)           |
| PUT       | /rs/admin/data/events?apiClientId={{api client id}}&jti={{jti}} | update an event messages for a tpp (api client) by jti |
| GET       | /rs/admin/data/events?apiClientId={{api client id}}             | export all event messages for a tpp                    |
| DELETE    | /rs/admin/data/events?apiClientId={{api client id}}             | delete all event messages for a tpp                    |
| DELETE    | /rs/admin/data/events?apiClientId={{api client id}}&jti={{jti}} | delete an event message for a tpp by jti               |

The API is protected by IG and must be used with a proper client certificate and access token with all scopes (openid, payment, account, fundsconfirmation)

An Event Notification message needs to be structured as JWT aligned with Security Event Token standard (SET) (https://datatracker.ietf.org/doc/html/rfc8417)
and must be signed for non-repudiation.

IG have the responsibility of:
* Sign each event from the request payload to be import through admin/data/events RS API as Signed JWT SET

#### Payload example
```json
{
  "client_id": "3ffb98cc-be98-4b10-a405-bde41e88c2c7",
  "events":
  [
    {
      "iss": "https://examplebank.com/",
      "iat": 1516239022,
      "jti": "b460a07c-4962-43d1-85ee-9dc10fbb8f6c",
      "sub": "https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003",
      "aud": "7umx5nTR33811QyQfi",
      "txn": "dfc51628-3479-4b81-ad60-210b43d02306",
      "toe": 1516239022,
      "events": {
        "urn:uk:org:openbanking:events:resource-update": {
          "subject": {
            "subject_type": "http://openbanking.org.uk/rid_http://openbanking.org.uk/rty",
            "http://openbanking.org.uk/rid": "pmt-7290-003",
            "http://openbanking.org.uk/rty": "domestic-payment",
            "http://openbanking.org.uk/rlk": [
              {
                "version": "v3.0",
                "link": "https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003"
              },
              {
                "version": "v1.1",
                "link": "https://examplebank.com/api/open-banking/v1.1/payment-submissions/pmt-7290-003"
              }
            ]
          }
        }
      }
    }
  ]
}
```
<details>
<summary>JSON payload schema</summary>

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Schema for events notification messages, to be processed and imported by SAPIG",
  "type": "object",
  "properties": {
    "client_id": {
      "type": "string",
      "description": "A value that represents the identification value assigned a tpp application after registration (api client Id)"
    },
    "events": {
      "type": "array",
      "description": "Security Event Token (SET) claims, https://datatracker.ietf.org/doc/html/rfc8417#section-2.2",
      "items": {
        "type": "object",
        "properties": {
          "iss": {
            "type": "string",
            "description": "https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.1"
          },
          "iat": {
            "type": "number",
            "description": "https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.6"
          },
          "jti": {
            "type": "string",
            "description": "Unique identifier for the SET, https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.7"
          },
          "sub": {
            "type": "string",
            "description": "https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.2"
          },
          "aud": {
            "type": "string",
            "description": "https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.3"
          },
          "txn": {
            "type": "string",
            "description": "Transaction Identifier"
          },
          "toe": {
            "type": "number",
            "description": "A value that represents the date and time at which the event occurred.  This value is a NumericDate"
          },
          "events": {
            "type": "object",
            "description": "This claim contains a set of event statements that each provide information describing a single logical event that has occurred",
            "properties": {
              "urn:uk:org:openbanking:events:resource-update": {
                "type": "object",
                "properties": {
                  "subject": {
                    "type": "object",
                    "properties": {
                      "subject_type": {
                        "type": "string"
                      },
                      "http://openbanking.org.uk/rid": {
                        "type": "string"
                      },
                      "http://openbanking.org.uk/rty": {
                        "type": "string"
                      },
                      "http://openbanking.org.uk/rlk": {
                        "type": "array",
                        "items": {
                          "type": "object",
                          "properties": {
                            "version": {
                              "type": "string"
                            },
                            "link": {
                              "type": "string"
                            }
                          },
                          "required": [
                            "version",
                            "link"
                          ]
                        }
                      }
                    },
                    "required": [
                      "subject_type",
                      "http://openbanking.org.uk/rid",
                      "http://openbanking.org.uk/rty",
                      "http://openbanking.org.uk/rlk"
                    ]
                  }
                },
                "required": [
                  "subject"
                ]
              }
            },
            "required": [
              "urn:uk:org:openbanking:events:resource-update"
            ]
          }
        },
        "required": [
          "iss",
          "iat",
          "jti",
          "sub",
          "aud",
          "events"
        ]
      }
    }
  },
  "required": [
    "client_id",
    "events"
  ]
}
```
</details>

### Response examples
**Import and update**

> returns the same payload sent in the request body