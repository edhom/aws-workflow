import json
import boto3

def lambda_handler(event, context):

    if event is None:
        encoded_string = "Can not find any match"

    elif event == {'isEmpty': True}:
        encoded_string = "No requests found"

    else:
        encoded_string = json.dumps(event, indent=4).encode("utf-8")

    bucket_name = "dhom-distributedsystems-rideoffer"

    s3_path = "log.txt"
    s3 = boto3.resource("s3")
    s3.Bucket(bucket_name).put_object(Key=s3_path, Body=encoded_string)
    return "Request is logged!"