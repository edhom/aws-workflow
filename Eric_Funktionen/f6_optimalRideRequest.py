import json

def score(req):
    return req['Profit'] - req['Overhead'] * 0.30 - req['OverheadInTime'] * 10

def lambda_handler(requests, context):
    
    matchingReqs = list(filter(lambda req: req['match'] == True, requests))
    
    if len(matchingReqs) > 0:
        optimalMatch = matchingReqs[0]
        for req in matchingReqs:
            optimalMatch = req if score(req) > score(optimalMatch) else optimalMatch
    else:
        optimalMatch = None
        
    return optimalMatch