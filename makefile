jlox:
	javac com/zk/jlox/Jlox.java
	java com.zk.jlox.Jlox ${FILE}

Expr.java:
	javac com/zk/tool/GenerateAst.java
	java com.zk.tool.GenerateAst ./com/zk/jlox
