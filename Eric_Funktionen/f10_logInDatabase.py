import json
import boto3

def lambda_handler(event, context):
    encoded_string = json.dumps(event, indent=4).encode("utf-8")

    bucket_name = "dhom-distributedsystems-rideoffer"
    s3_path = "log.txt"
    s3 = boto3.resource("s3")
    s3.Bucket(bucket_name).put_object(Key=s3_path, Body=encoded_string)
    return "Request is logged!"