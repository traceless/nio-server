import koa from 'koa';
import Router from 'koa-router';
import http from 'http';
import bodyparser from 'koa-bodyparser';
const restRouter = new Router();
const app = new koa()
// 模拟IO等待
async function sleep(time) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve();
    }, time || 1000);
  });
}
app.use(
  bodyparser({
    enableTypes: ["json", "form", "text"],
  })
);
let count = 1;
// 测试IO对吞吐量的影响
restRouter.all("/test/:time", async (ctx) => {
  const time = ctx.params.time
  if (time) {
    // 模拟io等待，模拟请求mysql，请求redis，请求淘宝接口，等耗时的IO接口
    await sleep(time);
  }
  // 消耗单线程的CPU时间，把性能下降到一定位置，这样方便测试IO的对性能的影响
  for (let i = 0; i < 700; i++) {
    ("tokentest" + i).match(/^token.*$/g);
  }
  if (count++ % 1000 == 0) {
    console.log(ctx.url, count);
  }
  ctx.body = { success: true, date: new Date() };
});

app.use(restRouter.routes()).use(restRouter.allowedMethods());
// 必须在路由之后
app.use(async (ctx) => {
  console.log("------all------", ctx.req.url, count++);
  ctx.body = { success: "true" };
});
const server = http.createServer(app.callback());
// 设置最大连接数和超时，超时时间单位为毫秒
server.maxConnections = 20000;
server.keepAliveTimeout = 12000;
const port = 7080;
server.listen(port, () => console.log("服务启动成功: " + port));
setInterval(() => {
  console.log('server_connections', server._connections);
}, 1000);
