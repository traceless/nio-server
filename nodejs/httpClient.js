// start
import fs from 'fs';
import http from 'http';
import https from 'https';
const Agent = http.Agent;
const Agents = https.Agent;
// 模拟IO等待
async function sleep(time) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve();
    }, time || 3000);
  });
}

let url = "http://127.0.0.1:8080/test/20";
// url = "http://127.0.0.1:8080/testlog";
// url = "http://127.0.0.1:8080/testAsync/20";
url = "http://127.0.0.1:7080/test/10";

const maxSockets = 2000;
const httpsAgent = new Agents({ maxSockets, maxFreeSockets: maxSockets, keepAlive: true })
const httpAgent = new Agent({ maxSockets, maxFreeSockets: maxSockets, keepAlive: true })
const options = {
  url: url,
  method: "POST",
  headers: {
    // "content-type": "application/x-www-form-urlencoded",
    accessToken: "12345678 ",
    // 'content-type': 'application/json'
  },
  agent: ~url.indexOf("https") > 0 ? httpsAgent : httpAgent,
  json: { "param": "test" }
};

async function request(_options) {
  return new Promise((resolve, reject) => {
    const req = http.request(options.url, options, (res) => {
      res.setEncoding('utf8');
      res.on('data', (chunk) => {
        resolve(chunk)
      });
    });
    req.on('error', (e) => {
      reject(e)
    });
    // 将数据写入请求正文
    req.write(JSON.stringify(_options.json));
    req.end();
  });
}

// 请求总数
const reqCount = 21313;
const start = async function (reqNum = reqCount) {
  const sum = reqNum < reqCount ? reqNum : reqCount;
  let finish = 1;
  let startTime = process.uptime() * 1000;
  console.log("-----start-request----", sum);
  for (let i = 1; i <= sum; i++) {
    request(options).then((body) => {
      if (finish++ >= sum) {
        const spend = process.uptime() * 1000 - startTime;
        const qps = sum / spend * 1000;
        console.log(`start-end:${body} 时间ms:${spend} QPS:${qps}`);
      }
    }).catch((err) => {
      console.log(`err:${err} , statusCode:${err}`);
    });
    if (i % 2000 == 0) {
      console.log("-----request-sum:", i);
      await sleep(100);
    }
  }
};
// 修改第一行注释，可以进行同步
const interval = setInterval((e) => {
  const data = fs.readFileSync("./httpClient.js", "utf-8");
  if (data.startsWith("// start")) {
    clearInterval(interval);
    console.log("-----热身连接----");
    start(maxSockets);
    setTimeout(() => {
      start();
    }, 2900);
  }
  console.log("waiting....");
}, 200);

setTimeout(() => {
  console.log("end....");
}, 22000);