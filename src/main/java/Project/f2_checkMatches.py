import json
import boto3

def getDriver():
    s3 = boto3.resource('s3')
    content_object = s3.Object('ride.offer.dhom', 'driver.json')
    file_content = content_object.get()['Body'].read().decode('utf-8')
    driver = json.loads(file_content)
    return driver
    
def lambda_handler(request, context):

    if request == {'isEmpty': True}:
        return request
    
    driver = getDriver()
    
    A = driver['A']
    B = request['A']
    C = request['B']
    D = driver['B']
    
    avgY = (A['y'] + D['y']) / 2
    
    if (
        A['x'] <= B['x'] and B['x'] <= D['x']
        and avgY + 10 >= B['y'] and avgY - 10 <= B['y']
        and A['x'] <= C['x'] and C['x'] - 10 <= D['x'] 
        and avgY + 10 >= C['y'] and avgY - 5 <= C['y'] 
    ):
        request['isMatch'] = True
    else:
        request['isMatch'] = False

    return request