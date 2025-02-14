# Scalable-Distributed-Systems-for-Ski-Resorts-
# SkierClientPart1

## Overview
`SkierClientPart1` is a multi-threaded Java HTTP client designed to send `200,000` POST requests to a ski resort server. The program simulates skier lift ride events and measures throughput, request success rates, and latencies.

## Features
- Generates `200,000` skier events with randomized values.
- Uses an initial batch of `32` threads to send `32,000` requests.
- A thread pool (max `150` threads) processes the remaining `168,000` requests.
- Implements **retry logic** for failed requests (up to `5` retries).
- Records request metadata (start time, latency, response codes) and saves it to `request_records.csv`.
- Provides statistics on successful and failed requests along with system throughput.

## Prerequisites
- **Java 11+** (supports `java.net.http.HttpClient`)
- **Maven** (if you wish to manage dependencies)
- **A running backend server** listening at `http://[EC2-IP]:8080/Assignment1Server_war` (replace `[EC2-IP]` with your actual EC2 instance IP)

## Server Setup
The backend server consists of a Java Servlet (`SkierServlet`) that handles POST requests with skier lift ride data. The servlet:
- Parses the request URL to extract `resortID`, `seasonID`, `dayID`, and `skierID`.
- Reads and validates the JSON payload.
- Responds with `201 Created` on success or `400 Bad Request` on errors.

### Example Server Deployment
1. Ensure Java and Tomcat are installed on your EC2 instance.
2. Deploy the `Assignment1Server_war` file to Tomcat’s `webapps` directory.
3. Start Tomcat and verify that the server is running at `http://[EC2-IP]:8080/Assignment1Server_war`.

## Usage
Run the program using:
```sh
java SkierClientPart1
```

Upon execution, the program:
1. **Generates skier lift ride events**.
2. **Sends requests using 32 initial threads** (`1000` requests each).
3. **Processes remaining requests using a thread pool** (max `150` threads).
4. **Logs statistics** (success rate, failure count, total runtime, throughput).
5. **Writes request logs to `request_records.csv`**.

## Configuration
You can modify constants in the code to change behavior:
- `TOTAL_REQUESTS = 200_000` → Change total number of requests.
- `INITIAL_THREADS = 32` → Change the number of initial threads.
- `MAX_THREAD_POOL_SIZE = 150` → Modify thread pool size.
- `SERVER_BASE_URL = "http://[EC2-IP]:8080/Assignment1Server_war"` → Replace `[EC2-IP]` with your actual EC2 IP.

## Output Example
After execution, you will see output like:
```
Initial 32 threads completed, starting thread pool...
Successful requests: 198765
Failed requests: 1235
Total run time: 12567 ms
Throughput: 15919.6 requests per second
```
A `request_records.csv` file will be generated containing request details:
```
StartTime,RequestType,Latency,ResponseCode
1707856234123,POST,45,201
1707856234125,POST,50,201
...
```

## Troubleshooting
### 1. Server not reachable
- Ensure the backend is running on `http://[EC2-IP]:8080/Assignment1Server_war`.
- Run `curl` to verify:
  ```sh
  curl -X POST "http://[EC2-IP]:8080/Assignment1Server_war/skiers/12/seasons/2019/day/1/skier/130" \
       -H "Content-Type: application/json" \
       -d '{"skierID":130,"resortID":12,"liftID":3,"seasonID":2019,"dayID":1,"time":120}'
  ```
- If running on AWS EC2, ensure **Security Groups** allow outbound requests.

### 2. High failure rate
- Check if the server is overloaded.
- Lower `MAX_THREAD_POOL_SIZE` to reduce concurrent load.
- Increase retry limit `MAX_RETRIES`.

### 3. Out of memory or CPU bottleneck
- Reduce `INITIAL_THREADS` and `MAX_THREAD_POOL_SIZE`.
- Use a larger EC2 instance if running in the cloud.

## License
This project is licensed under the **MIT License**.

