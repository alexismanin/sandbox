import requests
import concurrent.futures
import time

numberOfThreads = 1000

def getIP():
    res = requests.get("http://localhost:8080/hello")
    return res.text

with concurrent.futures.ThreadPoolExecutor(max_workers=numberOfThreads) as executor:
    futures = []
    before = time.time_ns()
    for _ in range(numberOfThreads):
        futures.append(executor.submit(getIP))
    for future in concurrent.futures.as_completed(futures):
        future.result()
    after = time.time_ns()
    print((after - before) / 1000_000)