import json
import boto3


def getDriver():
    s3 = boto3.resource('s3')
    content_object = s3.Object('ride.offer.dhom', 'driver.json')
    file_content = content_object.get()['Body'].read().decode('utf-8')
    driver = json.loads(file_content)
    return driver

def lambda_handler(event, context):
    bucket_name = "ride.offer.geiger"
    s3_path = "Driver.json"
    s3 = boto3.resource("s3")
    message = ""

    if event is None:
        return event

    elif event == [{'isEmpty': True}]:
        return input

    driver = getDriver()

    xTime = event['A']['x'] - driver['A']['x']
    yTime = event['A']['y'] - driver['A']['y']

    minutes = xTime + yTime

    event['OptimalPickUp']['x'] = event['A']['x']
    event['OptimalPickUp']['y'] = event['A']['y']
    event['OptimalPickUp']['inMinutes'] = minutes

    return event
