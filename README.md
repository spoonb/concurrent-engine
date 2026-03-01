# concurrent-engine

```mermaid
sequenceDiagram
    participant Client App
    participant Client OS
    participant Server OS
    participant Server App

    Note over Server App: ServerSocket serverSocket = new ServerSocket(8080)

    Server App->>Server OS: 创建监听socket (LISTEN 8080)
    Server OS-->>Server App: 监听成功

    Note over Client App: Socket socket = new Socket(serverIP, 8080)

    Client App->>Client OS: 创建socket
    Client OS->>Server OS: SYN
    Server OS->>Client OS: SYN-ACK
    Client OS->>Server OS: ACK

    Server OS->>Server App: 连接进入等待队列

    Note over Server App: serverSocket.accept()

    Server App->>Server OS: accept()
    Server OS-->>Server App: 返回一个新的 Socket (服务端通信socket)

    Note over Client App,Server App: TCP连接建立完成

    Client App->>Client OS: out.write("Hello")
    Client OS->>Server OS: 发送数据
    Server OS->>Server App: 数据到达
    Server App->>Server App: in.read()

    Server App->>Server OS: out.write("Hi")
    Server OS->>Client OS: 发送数据
    Client OS->>Client App: 数据到达
```

jps -l：获取pid

jstack <pid>监听jvm

内存是否泄露
= 是否需要
\+ 生命周期是否合理
\+ GC是否能回收
\+ 数量是否受控

假泄露

### 1️⃣ 缓存未设置上限

- 看起来像泄漏
- 实际是无界缓存

### 2️⃣ 线程池队列过大

- 队列里任务对象堆积
- GC 无法回收
- 不是泄漏，是“背压失效”

### 3️⃣ ThreadLocal 未清理

- 真泄漏
- 非常隐蔽
