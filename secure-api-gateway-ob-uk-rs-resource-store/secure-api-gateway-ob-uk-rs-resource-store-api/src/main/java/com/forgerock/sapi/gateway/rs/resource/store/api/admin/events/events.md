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
  "apiClientId": "3ffb98cc-be98-4b10-a405-bde41e88c2c7",
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
    "apiClientId": {
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
    "apiClientId",
    "events"
  ]
}
```
</details>

### Response examples
**Import and update**
```json
{
  "apiClientId": "3ffb98cc-be98-4b10-a405-bde41e88c2c7",
  "events": [
    {
      "jti": "b460a07c-4962-43d1-85ee-9dc10fbb8f6c",
      "set": "eyJ0eXAiOiJKV1QiLCJodHRwOi8vb3BlbmJhbmtpbmcub3JnLnVrL2lhdCI6MTY5MjM3MzAxNy4zNzksImh0dHA6Ly9vcGVuYmFua2luZy5vcmcudWsvdGFuIjoib3BlbmJhbmtpbmcub3JnLnVrIiwiY3JpdCI6WyJodHRwOi8vb3BlbmJhbmtpbmcub3JnLnVrL2lhdCIsImh0dHA6Ly9vcGVuYmFua2luZy5vcmcudWsvaXNzIiwiaHR0cDovL29wZW5iYW5raW5nLm9yZy51ay90YW4iXSwia2lkIjoieGNKZVZ5dFRrRkwyMWxISVVWa0FkNlFWaTRNIiwiaHR0cDovL29wZW5iYW5raW5nLm9yZy51ay9pc3MiOiIwMDE1ODAwMDAxMDQxUkVBQVkiLCJhbGciOiJQUzI1NiJ9.eyJpc3MiOiJodHRwczovL2V4YW1wbGViYW5rLmNvbS8iLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6ImI0NjBhMDdjLTQ5NjItNDNkMS04NWVlLTlkYzEwZmJiOGY2YyIsInN1YiI6Imh0dHBzOi8vZXhhbXBsZWJhbmsuY29tL2FwaS9vcGVuLWJhbmtpbmcvdjMuMC9waXNwL2RvbWVzdGljLXBheW1lbnRzL3BtdC03MjkwLTAwMyIsImF1ZCI6Ijd1bXg1blRSMzM4MTFReVFmaSIsInR4biI6ImRmYzUxNjI4LTM0NzktNGI4MS1hZDYwLTIxMGI0M2QwMjMwNiIsInRvZSI6MTUxNjIzOTAyMiwiZXZlbnRzIjp7InVybjp1azpvcmc6b3BlbmJhbmtpbmc6ZXZlbnRzOnJlc291cmNlLXVwZGF0ZSI6eyJzdWJqZWN0Ijp7InN1YmplY3RfdHlwZSI6Imh0dHA6Ly9vcGVuYmFua2luZy5vcmcudWsvcmlkX2h0dHA6Ly9vcGVuYmFua2luZy5vcmcudWsvcnR5IiwiaHR0cDovL29wZW5iYW5raW5nLm9yZy51ay9yaWQiOiJwbXQtNzI5MC0wMDMiLCJodHRwOi8vb3BlbmJhbmtpbmcub3JnLnVrL3J0eSI6ImRvbWVzdGljLXBheW1lbnQiLCJodHRwOi8vb3BlbmJhbmtpbmcub3JnLnVrL3JsayI6W3sidmVyc2lvbiI6InYzLjAiLCJsaW5rIjoiaHR0cHM6Ly9leGFtcGxlYmFuay5jb20vYXBpL29wZW4tYmFua2luZy92My4wL3Bpc3AvZG9tZXN0aWMtcGF5bWVudHMvcG10LTcyOTAtMDAzIn0seyJ2ZXJzaW9uIjoidjEuMSIsImxpbmsiOiJodHRwczovL2V4YW1wbGViYW5rLmNvbS9hcGkvb3Blbi1iYW5raW5nL3YxLjEvcGF5bWVudC1zdWJtaXNzaW9ucy9wbXQtNzI5MC0wMDMifV19fX19.MNhxg1ujcn0y-NW7DrSRw-HUaRqO28ifX7lHSxW_xcnupo9tMsP2Z0hkLIRquRa1gRE--WLWc_E7prUmsUYUqr4MTcX1XQgAYs3FHW5mX6x5wLrP7zC4Hs5SKqjPiEPqov27ZlBTpbXRRXe5L8COCRPEr7AGyP0QvOQ1xOUxWd1PVLaJHVi7RNI2V--YJAAopwSu_oIadE1CBPxuqiyVmXqeQUXG-q9O6nkjF_2SusBTz_EBh91wRIanZa47Hcwj1zb4DDWOu0nY5E3zFq98iWkTvChnMn1EHKLn-fBMT9X7thbK5g3q4iduJCprRJCLZnLYqHIy03XcgcwR3vZgpA"
    }
  ]
}
```