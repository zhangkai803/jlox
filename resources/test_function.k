print ">> test native func clock():";
print clock();

print ">> test custom func:";
fun sayHi(first, last) {
  print "Hi, " + first + " " + last + "!";
}

sayHi("Dear", "Reader");

print ">> test custom func no parameters";

fun sayHello() {
    print "Hello everyone!";
}

sayHello();

print ">> test return value in func:";

fun fib(n) {
  if (n <= 1) return n;
  return fib(n - 2) + fib(n - 1);
}

for (var i = 0; i < 3; i = i + 1) {
  print fib(i);
}
