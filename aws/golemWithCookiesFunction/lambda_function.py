import json
import requests
import requests.cookies
import boto3
from botocore.exceptions import ClientError

cookies = dict()

cookies["euconsent-v2"] = "CPIrLfuPIrLfuAGABCENBgCgAP_AAAAAAAYgHaBdBDLNDWEAEXxYStBgGYgU1sQUAiQCABCAAiAFAAGQ8IACk0ACMAQABAAAAQAAgRABAAAEGAFEAACAAAAEAAAkAAQAgAAIAAAEABEBAAIQAAoIAAAAAAAIAAABCQAAmACQA8bCBEAAAIAwQAAAgAAAAIACAgMAQAAAIDIAoATIB9gH4ARwBMQC8xgAEAsQiAKAIYAfgCGwEXgJ2CQJwAEAAVAAyABwAEAAIgAZQBEAEUAJgATwA3gBzAD8AIQAQ0AiACJAEsAKUAYYA1QB7QD7AP0AjQBHACUgFzAL8AYoA2gBuAD0AIbAReAmIBOwChwF5gMGAacEADAAfAF8AQMBKwDIQHbhoA4AXABDAD8AQ2Ai8BOwDGAwAEA2QqAMAEwALgA_ACOAJiAXmOgXAAVAAyABwAEAAIgAZAA-gCIAIoATAAngBcADeAHMAPwAhABDQCIAIkASwAmABSgCxAGGANEAe0A-wD9AIsARwAlIBYgC5gF-AMUAbQA3AB6AENgIvATsAocBeYDBgGJAMYAZYA04BxY4AgAAgAD4AZABfAEDAIiARkAvQB5AEIAJWATEAuIBkIDTQHbkICAAGQARABMAC4AG8AWMA-wD8AI4ASkAuYBfgDFAG0APQAtoBiRAAKAAgAXwBGQCxAJiJQEwAEAAZAA4ACIAIgATAAuACEAENAIgAiQBSgDVAH4ARwAxQBuAEXgLzAZYSACABkAF8ARkBKxSA-ABUADIAHAAQAAiABlAEQARQAmABPADmAH4AQgAhoBEAESAKUAWIA0QBqwD7AP0AiwBHACUgFzANoAbgA9ACLwE7AKHAXmAxgoAIAA-AGQAXwBYgDFAHkATEA00.YAAAAAAAAAAA"
cookies["authId"] = "7bca805c-fdf2-4f26-a717-f3ca11d7efe3"
cookies["golem_account"] = "s%3A69WFBIQiEXeNG1X_4AlGzsSjEQXaBX3R.jk0Q%2FgcUvT1ckgTnhFSqQossNQeukuszBspGEwa4HsA"
cookies["golem_c20date"] = "1625166436"
cookies["golem_consent20"] = "cmp|210630"
cookies[""] = ""
cookies[""] = ""
cookies[""] = ""
cookies[""] = ""
cookies[""] = ""
cookies[""] = ""
cookies[""] = ""
cookies[""] = ""
cookies[""] = ""
cookies[""] = ""
cookies[""] = ""
cookies[""] = ""
cookies[""] = ""


dynamodb = boto3.resource('dynamodb', region_name='eu-central-1')


def lambda_handler(event, context):

    url = event["queryStringParameters"]["url"]

    if url.endswith("-rss.html"):
        url = url[:len(url)-9]

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
        {"queryStringParameters": {"url": "https://www.golem.de/news/telekom-umts-abschaltung-verlief-nach-plan-2107-157809.html"}}, None))


if __name__ == "__main__":
    test()
