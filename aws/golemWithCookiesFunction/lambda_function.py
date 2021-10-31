import json
import requests
import requests.cookies
import boto3
from botocore.exceptions import ClientError

cookies = dict()
cookies["_sp_enable_dfp_personalized_ads"] = "false"
cookies["_sp_v1_consent"] = "1!1:1:-1:-1:-1:-1"
cookies["_sp_v1_csv"] = "null"
cookies["_sp_v1_data"] = "2:274572:1612386409:0:3:0:3:0:0:_:-1"
cookies["_sp_v1_lt"] = "1:"
cookies["_sp_v1_opt"] = "1:login|true:last_id|11:"
cookies["_sp_v1_ss"] = "1:H4sIAAAAAAAAAItWqo5RKimOUbKKBjLyQAyD2lidGKVUEDOvNCcHyC4BK6iurVWKBQAW54XRMAAAAA%3D%3D"
cookies["_sp_v1_uid"] = "1:449:622e9028-5093-4c08-90b4-4c9bd3ba2ad1"
cookies["a2a_consumerId"] = "94c17a8e-cb0b-40bc-9f3f-906d7329db24"
cookies["golem_c20date"] = "1635672127"   # WICHTIG!
cookies["golem_consent20"] = "cmp|211029"  # WICHTIG!
cookies["golem_lp"] = "wzijmip4xsag4n14n4q1jkxq57mk4qtg"
cookies["Golem_rngnaf"] = "true"
cookies["golem_testcookie"] = "1"
cookies["ima_data_8caf03772d2e0ce26fd3cd41a1358210dca9a6a8"] = "Vl%2BGhIYxw%2BVZRv5DGaTJ5w%3D%3DogHFGMCG0Zc4ZkIB0AsgNjqRWHO2oMH9LG46RbpU4azbYKvykFLDJLP5sHro3FpRHmhsMjHj%2B4JURrt1O8CV5inWAYqrFUe98X%2FXqIkr1L7BbHM7PwOfUKqxcubMEizt%2FNC2KRxW4NQCLeSwBMjC5NxL8EhujQ1PJeQwV6ZjbQ81EWPLUUMzz4zdw4bEgRjWrAFXUyN9Op8wmD5%2FB5LJjy3YkneGHYrCwXEpXjUbMqpgN1TC31nVxpAyu293sMDcB%2B7EME6IXoyiUsvTdMAX26%2FemKDbxjIUWDUM2RizMndpbHABQvHP90qAAkgfCZpW2Tr%2BfiKgsLCbB22hXKFf4uDMztlKdp%2FOoJvNZXLmeWyYgOj4QbtUMEKnQr%2BOOAE5ce1EV%2FqqANC%2F97MzluEAXw%3D%3D"
cookies["ima_data_checksum_8caf03772d2e0ce26fd3cd41a1358210dca9a6a8"] = "124c67bb1119a019808b750b4330ed5892b63610"
cookies["xdefccpm"] = "no"
cookies["xdefcc"] = "G421224b019a87bd1c854e42f56d6ce4c3"

dynamodb = boto3.resource('dynamodb', region_name='eu-central-1')


def lambda_handler(event, context):

    url = event["queryStringParameters"]["url"]
    body = loadUrl(url)

    return {
        'statusCode': 200,
        'body': body
    }


def loadUrl(url):
    html = loadUrlFromDatabase(url)
    if not html:
        html = loadUrlFromWeb(url)
        saveUrlToDatabase(url, html)

    return html


def loadUrlFromWeb(url):
    s = requests.Session()
    s.cookies = requests.cookies.cookiejar_from_dict(cookies)
    r = s.get(url)
    return r.text


def loadUrlFromDatabase(url):
    table = dynamodb.Table('GolemHtml')
    try:
        response = table.get_item(Key={'URL': url})
    except ClientError as e:
        return None
    else:
        if "Item" in response.keys():
            return response['Item']["Html"]
        else:
            return None


def saveUrlToDatabase(url, html):
    table = dynamodb.Table('GolemHtml')
    return table.put_item(Item={"URL": url, "Html": html})


def test():
    print(lambda_handler(
        {"queryStringParameters": {"url": "https://www.golem.de/news/tesla-elon-musk-ist-wieder-in-der-produktionshoelle-2102-153907.html"}}, None))


if __name__ == "__main__":
    test()
