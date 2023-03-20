# 问题记录

## Visitor 模式

将`resolver.resolve(statements)`变成了`statement.accept(resolver)`

将`interptreter.execute(statements)`变成了`statement.accept(interpreter)`

如果不用会怎么样？

文中对于这个做法的解释是：Expr 和 Statement 是由 Parser 解析而来的，对于语句的处理，包括前端、后端，Expr 和 Statement 在这过程中承担一个中间角色，而我们不想把前端、后端的处理逻辑耦合到 Expr 或 Statement 中。

如果直接把 Expr Stmt 作为参数传给 Resolver Interpreter 会产生什么问题吗？

## 用 raise 来实现 return

是普遍的做法吗？

## 在访问实例的方法时，每次都要将 实例 bind 到方法上

bind 操作本质上是将 this 语义注入到方法的作用域/上下文中，但是为什么在每次访问时都要重新注入？
仅在实例化时注入一次不行吗？

## 初始化方法的特殊性

如果一个类定义了初始化方法，有以下几个地方需要考虑：

- 类实例化时要传参，所以类的 call 方法要接收这些参数，要修改入参数量的检查
- 类实例化之后，要直接触发一下初始化方法的调用，要修改 Class.call 方法的逻辑
- 初始化方法的返回值要固定是 this，也就是说我们要检测初始化方法的 return 是否合法
  - 如果初始化方法中没有 return 语句，符合预期，但是我们要隐式修改返回值为 this
  - 如果有空 return 语句，代表初始化逻辑只执行到这里，后面的代码不再需要执行，我们仍返回 this
  - 如果 return 了非 this，可以报错给用户，这一步可以在 Resolver 阶段识别
