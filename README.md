# killbill-usage-plugin 

This is a plugin to verify the usage of an account that is designed to use amberflo using the following endpoint: https://app.amberflo.io/usage/events

## Kill Bill compatibility

| Plugin version | Kill Bill version  | 
| -------------: | -----------------: | 
| 1.0.0 		 | 0.24.z 			  | 

## Requirements

The plugin needs an amberflo API Key. 
See [https://www.amberflo.io/](https://www.amberflo.io/) for information on creating an account.

A catalog that has subscriptions and details to fetch when associated to an account. (see [killbill-amberflo-catalog-plugin] for more details)

A ``customerId`` in AmberFlo that will be used as the ``externalKey`` in this plugins logic. It also uses a custom field for the subscription to avoid duplicate logic.

## Installation

```
kpm install_java_plugin amberflo --from-source-file target/amberflo-usage-plugin-*-.jar --destination /var/tmp/bundles
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
     -d 'org.killbill.billing.plugin.usage.apiKey=test_XXX' \
     http://127.0.0.1:8080/1.0/kb/tenants/uploadPluginConfig/amberflo-usage-plugin
```

## Creating a User and Subscription

Navigate to Kaui and create a user. When creating a user, it is required to use the tag Auto_Invoicing_Off so that when the subscription is created, the invoice is NOT automatically created so that you can create a custom field for the subscription which is required for the plugin.

Navigate to the Subscription tab and create a new subscription.

* ``Bundle Key``: Optional Field, used for grouping certain subscriptions into a bundle. (See https://killbill.github.io/slate/index.html#bundle-bundle-resource) for more details)
* ``Subscription Key``: input the value that you wish to subscribe the user to.
* ``Plan``: input the plan you wish to use.
* ``Price Override``: Optional field, input a price override.

After creating a user and a subscription, navigate to the top to the tag icons and create a custom field. In here, do the following:

* Set ``Object Type`` to ``SUBSCRIPTION``
* Set ``Name`` to ``measure_name``. 
* Set ``Value`` to the same as the object used in ``Subscription Key`` during the subscription creation process. 

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

## Tags

While custom fields allow you to attach {key, value} pairs to various objects in the system, single values can also be attached to various objects in the system by using tags. To create user tags, one must first create the tag definitions. For instructions see (https://killbill.github.io/slate/#tag-definition)

* System Tags: These are interpreted by the system to change its behavior. Certain tags can only be attached to specific resource types -- e.g Account. In order to distinguish them from the user tags, the system tags are uppercase symbols.

* User Tags: These are not interpreted by the system and can be anything as long as it is a lowercase symbol. For example, good_customer could be a tag that can be attached to a customer Account.
The APIs to manage tags rely on having an existing tag definition

To add tags to an account:
Be sure to replace the ``{accountId}`` in the final line with your accountId.

```
curl -v \
    -X POST \
    -u admin:password \
    -H "X-Killbill-ApiKey: bob" \
    -H "X-Killbill-ApiSecret: lazar" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -H "X-Killbill-CreatedBy: demo" \
    -H "X-Killbill-Reason: demo" \
    -H "X-Killbill-Comment: demo" \
    -d '[ "00000000-0000-0000-0000-000000000002"]' \
    "http://127.0.0.1:8080/1.0/kb/accounts/{accountId}/tags"
```

For more information on tags see (https://killbill.github.io/slate/#account-add-tags-to-account)

## Usage

subscriptionId:
Input your AmberFlo Api key
See [https://www.amberflo.io/](https://www.amberflo.io/) for information on creating an account.

Date:
When making a call with the API, there are checks in place to make sure a valid start and end date is given. Due to using AmberFlo, a valid start and end date is one that is not in the future, you can only verify previous usage. If a future start date is given, it will be replaced by the start of the current day and if a future end date is given, then it will be replaced by the current date (DateTime.now()). This is different from Kill Bill as Kill Bill allows you to query future usage to preview what a potential bill may look like.

Latest releases

1.0.0:

Implemented two endpoints as required by KillBill, one which returns the usage for an account given a subscriptionId and another that return the usage for an account given a subscriptionId and a date range in the format yyyy-mm-dd.
