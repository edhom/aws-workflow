import json
import boto3

def getDriver():
    s3 = boto3.resource('s3')
    content_object = s3.Object('ride.offer.dhom', 'driver.json')
    file_content = content_object.get()['Body'].read().decode('utf-8')
    driver = json.loads(file_content)
    return driver

def distance(A, B):
    return abs(A['x'] - B['x']) + abs(A['y'] - B['y'])

def lambda_handler(request, context):

    if request == {'isEmpty': True} or request['isMatch'] == False:
        return request

    driver = getDriver()
    
    A = driver['A']
    B = request['A']
    C = request['B']
    D = driver['B']
    
    abcd = distance(A, B) + distance(B, C) + distance(C, D)
    ad = distance(A, D)
    overhead = abcd - ad
    request['Overhead'] = overhead
    
    return request