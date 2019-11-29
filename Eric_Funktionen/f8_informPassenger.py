import json
import boto3

def lambda_handler(event, context):
    message = "Pickup in " + str(event['OptimalPickUp']['inMinutes']) + " minutes at Location: (" + str(event['OptimalPickUp']['x']) + ", " + str(event['OptimalPickUp']['y']) + ")"
    encoded_string = message.encode("utf-8")

    bucket_name = "dhom-distributedsystems-rideoffer"
    s3_path = "passengerInfo.txt"
    s3 = boto3.resource("s3")
    s3.Bucket(bucket_name).put_object(Key=s3_path, Body=encoded_string)
    return "Passenger informed!"