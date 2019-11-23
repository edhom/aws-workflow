import json

def score(req):
    return req['Profit'] - req['Overhead'] * 0.30 - req['OverheadInTime'] * 10

def lambda_handler(requests, context):
    
    optimalMatch = requests[0]
    for req in requests:
        optimalMatch = req if score(req) > score(optimalMatch) else optimalMatch
    
    return optimalMatch