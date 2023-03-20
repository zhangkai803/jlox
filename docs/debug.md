# Debug 日志

抄代码还抄错了这么多 TnT.

## if 语句总是返回 true

```sh
print 1==1;  # true
print 1==0;  # false

if (1 == 0)
    print "if branch";  # 上面输出正常 证明逻辑计算没有出错 可是代码一直执行到这里
else
    print "else branch";
```

原因：在进行`if`条件判断时，没有把条件表达式`condition`传进去，错传成了语句本身`stmt`

```java
if (isTruthy(evaluate(stmt)))
=>
if (isTruthy(evaluate(stmt.condition)))
```

## 函数声明时，如果无参数，会报语法错误

```sh
fun sayHi(first, last) {
  print "Hi, " + first + " " + last + "!";
}

sayHi("Dear", "Reader");

# 上面定义有入参的函数 正常

fun sayHello() {  # 报错
    print "Hello everyone!";
}

sayHello();
```

原因：在函数声明时，遇到左括号以后，准备匹配函数的入参列表时，应该先用`check`检查后面是否跟着右
括号，我写错成`match`，`match`在 token 匹配成功之后，会往前走一步，导致函数的入参声明完成之后，
无法通过括号闭合的检测。

```java
if (!match(TokenType.RIGHT_PAREN))
=>
if (!check(TokenType.RIGHT_PAREN))
```

检测右括号闭合的代码：

```java
consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");
```

## 给实例属性赋值时，总是提示属性不存在

```sh
print ">> test set instance property:";
var ins = Class();
ins.field = 1;  # BUG: Undefined property 'field'.
print ins.field;
```

原因：在`Parser`阶段，设置属性的时候，把`Expr`对象传进去了，实际应该传`Expr.object`，这个才
是在`runtime`需要赋值的对象。

```java
return new Expr.Set(get, get.name, value);
=>
return new Expr.Set(get.object, get.name, value);
```
