print ">> test class declaration:";
class DevonshireCream {
  serveOn() {
    return "Scones";
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
print ins.undefine_field;
