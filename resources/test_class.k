print ">> test class declaration:";
class Class {
  serveOn() {
    print "serveOn in Class";
    return "Scones";
  }

  speak() {
    print this;
  }
}
print Class; // Prints "Class".

print ">> test class intance:";
var ins = Class();
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
    print this;  // [line: 38] Error  at 'this': Can't use this outside of a class.
}
// test();  // Output: Get variable fail. Undefined variable 'this'.

print ">> test init method:";
class InitClass {
    init() {
      print "do init";
      return 1;  // [line: 46] Error  at 'return': Can't return a value from an initializer.
      return;  // no error
      print "after return in init";  // no print
    }
}
InitClass();
