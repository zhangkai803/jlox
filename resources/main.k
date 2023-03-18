1+1;
1-1;
1*2;
3/1;
print "one";
print true;
print 2 + 1;

var a = 1;
var b = 2;
print a + b;
b = 4;
print b = 4;


var a = "global a";
var b = "global b";
var c = "global c";
{
  var a = "outer a";
  var b = "outer b";
  {
    var a = "inner a";
    print a;
    print b;
    print c;
  }
  print a;
  print b;
  print c;
}
print a;
print b;
print c;

if (1 == 0)
    print "if branch";
else
    print "else branch";

print 1==1;
print 1==0;

var k = 1;
k = k + 1;
print k;

while (k < 3)
    // while 循环体目前只能包含一条语句 识别到分号就退出了
    k = k + 1;
    print k;

print ">> test for loop:";

var a = 0;

for (var i = 0; i < 10; i=i+1) {
    a = a + i;
}
print a;

print ">> test native func lock():";
print lock();
