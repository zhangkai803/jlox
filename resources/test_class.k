print ">> test class declaration:";
class DevonshireCream {
  serveOn() {
    print "serveOn in DevonshireCream";
    return "Scones";
  }

  speak() {
    print this;
  }
}
print DevonshireCream; // Prints "DevonshireCream".

print ">> test class intance:";
var ins = DevonshireCream();
print ins;

print ">> test set instance property:";
ins.field = 1;
print ins.field;

print ">> test get instance property:";
print ins.field;

print ">> test get undefined instance property:";
// print ins.undefine_field;

print ">> test class method:";
var res = ins.serveOn();
print res;

print ">> test this pointer:";
ins.speak();

print ">> test this usage outside of class:";
// print this;  // Output: undefined variable this
fun test() {
    print this;
}
// test();  // Output: Get variable fail. Undefined variable 'this'.
