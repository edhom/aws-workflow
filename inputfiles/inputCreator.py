import random
import json
import sys

request = {
  "A": {
    "x": 23,
    "y": 1
  },
  "B": {
    "x": 30,
    "y": 5
  },
  "Profit": '',
  "Overhead": '',
  "OverheadInTime": '',
  "OptimalPickUp": {
    "x": '',
    "y": '',
    "inMinutes": '',
  }
}

for i in range (int(sys.argv[1])):
    request['A']['x'] = random.randint(-5, 15)
    request['A']['y'] = random.randint(-5, 15)
    request['B']['x'] = random.randint(-5, 15)
    request['B']['y'] = random.randint(-5, 15)

    with open("input" + str(i) + ".txt", "w") as inputFile:
      json.dump(request, inputFile)
      inputFile.close()