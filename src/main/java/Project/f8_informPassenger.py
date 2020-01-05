import json
import boto3

def lambda_handler(event, context):

    message = ""
    if event == {'isEmpty': True}:
        message = "Can not find any requests"

    elif event is None:
        message = "Can not find a proper request"

    else:
        message = "Pickup in " + str(event['OptimalPickUp']['inMinutes']) + " minutes at Location: (" + str(event['OptimalPickUp']['x']) + ", " + str(event['OptimalPickUp']['y']) + ")"

    encoded_string = message.encode("utf-8")

    bucket_name = "dhom-distributedsystems-rideoffer"
    s3_path = "InformationPassenger.txt"
    s3 = boto3.resource("s3")
    s3.Bucket(bucket_name).put_object(Key=s3_path, Body=encoded_string)
    return "Passenger informed!"