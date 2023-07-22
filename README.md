# killbill-usage-plugin 

This plugin integrates Kill Bill with Amberflo Cloud Metering Services. It allows you to use Amberflo as a metering engine and delegate the billing to Kill Bill.

## Kill Bill compatibility

| Plugin version | Kill Bill version  | 
| -------------: | -----------------: | 
| 1.0.0 		 | 0.24.z 			  | 

## Requirements

The plugin needs an Amberflo API Key. See [https://www.amberflo.io/](https://www.amberflo.io/) for information on creating an account.

## Installation

```
kpm install_java_plugin amberflo --from-source-file target/amberflo-usage-plugin-*.jar --destination /var/tmp/bundles
```

Go to https://www.amberflo.io/ and copy your `API key`.

Then, go to the Kaui plugin configuration page (`/admin_tenants/1?active_tab=PluginConfig`), and configure the `amberflo-usage-plugin` plugin with your key:

```java
org.killbill.billing.plugin.amberflo.usage.apiKey={yourApiKeyHere}
```

Alternatively, you can upload the configuration directly:

```bash
curl -v \
     -X POST \
     -u admin:password \
     -H 'X-Killbill-ApiKey: bob' \
     -H 'X-Killbill-ApiSecret: lazar' \
     -H 'X-Killbill-CreatedBy: admin' \
     -H 'Content-Type: text/plain' \
     -d 'org.killbill.billing.plugin.amberflo.usage.apiKey=xxx' \
     http://127.0.0.1:8080/1.0/kb/tenants/uploadPluginConfig/amberflo-usage-plugin
```

## Creating a User and Subscription

It is assumed you already have a catalog configured with usage-based plans (see https://docs.killbill.io/latest/consumable_in_arrear.html for details).

Navigate to Kaui and create a user. The ``externalKey`` must map to the ``customerId`` in AmberFlo.

Navigate to the Subscription tab and create a new subscription (associated with a usage-based plan).

After creating the user and the subscription, navigate to the top to the tag icons and create a custom field. In here, do the following to map the subscription to the associated meter in Amberflo:

* Set ``Object Type`` to ``SUBSCRIPTION``
* Set ``Name`` to ``measure_name``. 
* Set ``Value`` to the meter name in Amberflo.

To create a Custom Field using the api:

```
curl -v \
    -X POST \
    -u admin:password \
    -H "X-Killbill-ApiKey: bob" \
    -H "X-Killbill-ApiSecret: lazar" \
    -H "Content-Type: application/json" \
    -H "X-Killbill-CreatedBy: {creatorName}" \
    -d '[{ 
            "objectId": "{subscriptionId}",
            "objectType": "SUBSCRIPTION",
            "name": "measure_name",
            "value": "{usage value to obtain}"
    }]' \
    'http://127.0.0.1:8080/1.0/kb/subscriptions/{subscriptionId}/customFields' 
```

Notes:
* When making a call with the API, there are checks in place to make sure a valid start and end date is given. Due to using AmberFlo, a valid start and end date is one that is not in the future, you can only verify previous usage. If a future start date is given, it will be replaced by the start of the current day and if a future end date is given, then it will be replaced by the current date (DateTime.now()). This is different from Kill Bill as Kill Bill allows you to query future usage to preview what a potential bill may look like.

## Latest releases

1.0.0:

Implemented two endpoints as required by KillBill, one which returns the usage for an account given a subscriptionId and another that return the usage for an account given a subscriptionId and a date range in the format yyyy-mm-dd.
